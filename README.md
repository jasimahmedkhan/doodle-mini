# mini-doodle

A meeting scheduling backend built with Spring Boot 4, Java 25, and PostgreSQL 16. Mini-doodle lets users manage time slots, book meetings with participants, and query merged availability. It is a modular monolith with hexagonal boundaries inside each module.

## Run the service

Requirements: Docker with the Compose plugin. The application image is built in a Java 25 build stage and copied into a smaller Java 25 runtime image.

```bash
docker compose up --build
```

The first start builds the application, starts PostgreSQL 16, waits for it to become healthy, and then starts mini-doodle. Flyway creates and validates the database schema.

| Resource | URL |
|---|---|
| REST API | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Health | `http://localhost:8080/actuator/health` |
| Prometheus metrics | `http://localhost:8080/actuator/prometheus` |

Configuration can be supplied through environment variables. Defaults are suitable for local development.

| Variable | Default | Purpose |
|---|---|---|
| `DB_NAME` | `minidoodle` | PostgreSQL database |
| `DB_USER` | `minidoodle` | PostgreSQL user |
| `DB_PASSWORD` | `minidoodle` | PostgreSQL password |
| `DB_PORT` | `5432` | Datasource port when running the app outside Compose |
| `APP_PORT` | `8080` | Host port for the API |
| `SPRING_PROFILES_ACTIVE` | `prod` in Compose | Active Spring configuration profile |
| `LOG_FORMAT` | `logstash` | Structured console-log format used by the `prod` profile |

For example:

```bash
DB_PASSWORD=local-secret APP_PORT=8081 docker compose up --build
```

### Configuration profiles

The application has separate logging behavior for development and production:

- `dev` is the default profile and leaves Spring Boot's standard console logging unchanged.
- `prod` emits structured JSON to stdout for ingestion by a production logging platform. The Compose app activates this profile by default.

Shared settings remain in `application.yaml`. Secrets and deployment-specific addresses still come from environment variables rather than profile files. Future production-only security settings can be added to `application-prod.yaml` without changing local development behavior.

Stop the stack without deleting its named database volume:

```bash
docker compose down
```

## Architecture

Each feature is organized module-first, then split into API, application, domain, and infrastructure packages. Domain code is plain Java. API and persistence details point inward; feature modules communicate through public application contracts.

```text
+----------------+    SlotOperations /        +----------------+
| meeting module |    SlotAvailabilityQuery   |  slot module   |
|                | --------------------------> |                |
+----------------+                             |                |
                                               | public app     |
+---------------------+ SlotAvailabilityQuery  | contracts      |
| availability module | ---------------------> |                |
+---------------------+                        +----------------+
          |
          | GetUserUseCase
          v
+----------------+
|  user module   |
+----------------+

slot, meeting, availability, user
                  |
                  | import shared value types
                  v
        +-----------------------+
        |     shared/domain     |
        | IDs, TimeRange, base  |
        | domain exception      |
        +-----------------------+
```

The essential dependency direction inside every feature is:

```text
REST adapter -> application use case -> domain
                    ^                    ^
                    |                    |
             application wiring   persistence adapter
                                        |
                                  domain repository port
```

### Slot state machine

```text
           book
  +------+ -------------> +--------+
  | FREE |                | BOOKED |
  +------+ <------------- +--------+
     |       cancel meeting
     |
     | mark busy
     v
  +------+
  | BUSY |
  +------+
     |
     | mark free
     +-------------------> FREE
```

Only `FREE` slots can be booked or marked busy. A `BUSY` slot can be marked free. A `BOOKED` slot cannot be modified, deleted, or marked free; cancelling its meeting releases it to `FREE` in the same transaction.

## API

All timestamps are ISO-8601 instants in UTC, such as `2030-01-15T09:00:00Z`. Requests and responses use DTOs; domain and persistence types are never exposed.

| Method | Path | Success | Expected errors |
|---|---|---:|---|
| `POST` | `/api/v1/users` | `201` | `422` invalid input or duplicate email |
| `GET` | `/api/v1/users/{id}` | `200` | `404` user not found |
| `POST` | `/api/v1/slots` | `201` | `404` user; `422` invalid or overlapping range |
| `GET` | `/api/v1/slots/{id}` | `200` | `400` malformed ID; `404` slot not found |
| `GET` | `/api/v1/users/{id}/slots?from=...&to=...` | `200` | `400` invalid window; `404` user not found |
| `PATCH` | `/api/v1/slots/{id}` | `200` | `404` slot; `409` booked slot |
| `DELETE` | `/api/v1/slots/{id}` | `204` | `404` slot; `409` booked slot |
| `POST` | `/api/v1/slots/{id}/busy` | `200` | `404` slot; `409` invalid state |
| `POST` | `/api/v1/slots/{id}/free` | `200` | `404` slot; `409` invalid state |
| `POST` | `/api/v1/slots/{id}/meetings` | `201` | `404` slot; `409` conflict; `422` invalid input |
| `GET` | `/api/v1/users/{id}/meetings?from=...&to=...` | `200` | `400` invalid window; `404` user not found |
| `DELETE` | `/api/v1/meetings/{id}` | `204` | `404` meeting |
| `GET` | `/api/v1/users/{id}/availability?from=...&to=...` | `200` | `400` invalid window; `404` user |

Errors have one consistent shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "title: must not be blank",
  "timestamp": "2030-01-15T08:00:00Z",
  "path": "/api/v1/slots/00000000-0000-0000-0000-000000000000/meetings"
}
```

### Full lifecycle with curl

The following example uses `jq` to carry generated IDs between calls.

Register a user:

```bash
BASE_URL=http://localhost:8080/api/v1

USER_ID=$(curl --fail --silent \
  --request POST "$BASE_URL/users" \
  --header 'Content-Type: application/json' \
  --data '{"name":"Alice","email":"alice@example.com"}' \
  | jq --raw-output '.id')

echo "$USER_ID"
```

Create a free slot:

```bash
SLOT_ID=$(curl --fail --silent \
  --request POST "$BASE_URL/slots" \
  --header 'Content-Type: application/json' \
  --data "{\"ownerId\":\"$USER_ID\",\"start\":\"2030-01-15T09:00:00Z\",\"end\":\"2030-01-15T10:00:00Z\"}" \
  | jq --raw-output '.id')

echo "$SLOT_ID"
```

Retrieve the individual slots available for management in a bounded window. Unlike availability,
this response is not merged, so every item retains the ID needed for updates, state changes, or booking:

```bash
curl --fail --silent \
  "$BASE_URL/users/$USER_ID/slots?from=2030-01-15T08:00:00Z&to=2030-01-15T12:00:00Z" \
  | jq
```

Book the slot as a meeting. Participants are value objects and do not need registered accounts:

```bash
MEETING_ID=$(curl --fail --silent \
  --request POST "$BASE_URL/slots/$SLOT_ID/meetings" \
  --header 'Content-Type: application/json' \
  --data '{
    "title":"Roadmap planning",
    "description":"Plan the next release",
    "participants":[
      {"name":"Alice","email":"alice@example.com"},
      {"name":"Bob","email":"bob@example.com"}
    ]
  }' \
  | jq --raw-output '.id')

echo "$MEETING_ID"
```

List meetings in the user's bounded window. Each item includes the meeting ID required for cancellation
and the start and end of its slot:

```bash
curl --fail --silent \
  "$BASE_URL/users/$USER_ID/meetings?from=2030-01-15T08:00:00Z&to=2030-01-15T12:00:00Z" \
  | jq
```

Query the complete merged availability window; the range is now `BOOKED`:

```bash
curl --fail --silent \
  "$BASE_URL/users/$USER_ID/availability?from=2030-01-15T08:00:00Z&to=2030-01-15T12:00:00Z" \
  | jq
```

Cancel the meeting, then query again; the slot is `FREE`:

```bash
curl --fail --silent \
  --request DELETE "$BASE_URL/meetings/$MEETING_ID"

curl --fail --silent \
  "$BASE_URL/users/$USER_ID/availability?from=2030-01-15T08:00:00Z&to=2030-01-15T12:00:00Z" \
  | jq
```

## Tests

Java 25 and a running Docker daemon are required. Integration and API tests use disposable PostgreSQL 16 containers through Testcontainers; the Compose stack does not need to be running.

```bash
./mvnw test
```

The suite includes pure domain and application tests, PostgreSQL persistence tests, full HTTP lifecycle and concurrency tests, and ArchUnit checks for module boundaries.

## Observability

Spring Boot Actuator exposes health and Prometheus endpoints. Micrometer also provides standard JVM, process, HTTP server, HikariCP, and database-pool metrics.

With the `prod` profile, the app emits one Logstash-compatible JSON object per line to stdout so production log collectors can index fields such as timestamp, level, logger, and message. The default `dev` profile uses readable text. Set `LOG_FORMAT` to another Spring Boot structured format, such as `ecs`, when required by the target logging platform.

Booking has two business-specific metrics:

| Prometheus metric | Meaning |
|---|---|
| `minidoodle_meeting_booking_duration_seconds` | Timer summary for every booking attempt |
| `minidoodle_meeting_booking_conflicts_total` | Counter for concurrent database write conflicts |

No user ID, slot ID, email, or other unbounded value is used as a metric tag. Additional metrics should be added only in response to an operational question. Useful next candidates are a low-cardinality booking outcome counter, availability-query latency, and slot-overlap rejections. The built-in HTTP and database metrics already cover general traffic and dependency health.

## Design decisions

### Modular monolith and public contracts

The system is deployed as one process and one database, which keeps transactions and operations simple, while feature modules provide explicit ownership boundaries. Cross-module calls use public application contracts such as `SlotOperations` and `SlotAvailabilityQuery`. Repository ports are deliberately not cross-module APIs: exposing them would let another module bypass the owning module's use cases, state transitions, and transaction semantics.

### Domain ports and infrastructure adapters

Repository interfaces live in each module's domain because they express what that domain needs, without Spring or JPA. Infrastructure adapters implement those ports using Spring Data and map between persistence entities and domain objects. The domain remains pure Java and can be tested without a framework or database.

### Three slot states and one booking link

`FREE`, `BUSY`, and `BOOKED` distinguish a manually unavailable period from a scheduled meeting. `TimeSlot` stores only the status and never a meeting reference. The relationship is owned solely by the unique, non-null `meeting.slot_id` foreign key. Booking and cancellation write both aggregates in one transaction, so `BOOKED` corresponds to exactly one meeting row without two competing relationship fields.

### Concurrency and overlap protection

Slot rows use optimistic locking because booking contention is expected to be rare: uncontended writes do not pay for database locks, and a stale writer becomes HTTP `409`. The unique constraint on `meeting.slot_id` is the final one-meeting-per-slot guard. If the workload changed to sustained, high contention on the same slots, pessimistic row locking could replace this choice to serialize contenders earlier, at the cost of blocking, deadlock management, and lower concurrency.

User slot ranges are checked in the domain/application flow for a useful error and protected again by PostgreSQL's GiST exclusion constraint for race-safe enforcement. Adjacent ranges are allowed because `[start, end)` boundaries do not overlap.

### Derived availability view

`UserCalendar` is a derived domain view over slots, not an entity or table. The API uses the term availability, and the database has no calendar schema object. This avoids a second persisted representation that could drift from the slots that actually own availability state.

Availability returns the full requested window as an ordered, clipped, merged read model. Adjacent ranges with the same status are combined. It is intentionally not paginated: paginating raw slots could split adjacent ranges across page boundaries and make the merged result depend on page size or retrieval order.

### Time handling

The API accepts `Instant`, persistence uses PostgreSQL `timestamptz`, and examples use UTC `Z` timestamps. This keeps comparisons and overlap rules independent of server or caller time zones.

## Out of scope

The following capabilities are deliberately excluded from this challenge, but are natural production improvements:

- **Authentication and authorization:** Add an identity provider or JWT-based security, then verify that callers can modify only slots and meetings they own.
- **Notifications and invitations:** Publish meeting events through an outbox and deliver email or calendar invitations asynchronously, without extending booking transactions.
- **Recurring slots and meetings:** Introduce recurrence rules, exceptions, and bounded occurrence generation rather than storing an unlimited series eagerly.
- **Participant accounts:** Participants currently remain name/email value objects. Account linkage could later support invitation responses and personal meeting views without making registration mandatory.
- **Dependency resilience:** Map database outages to a consistent `503`, configure connection timeouts, and test recovery. Write retries would require idempotency to avoid duplicate or ambiguous bookings.
- **Request tracing:** Add correlation and trace identifiers to structured logs so a request can be followed across API, database, and any future asynchronous integrations.
