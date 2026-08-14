# doodle-mini

A meeting scheduling backend built with Spring Boot 4, Java 25, and PostgreSQL 16. Mini-doodle lets users manage time slots, book meetings with participants, and query merged availability. It is a modular monolith with hexagonal boundaries inside each module.

### Configuration profiles

The application has separate logging behavior for development and production:

- `dev` is the default profile and leaves Spring Boot's standard console logging unchanged.
- `prod` emits structured JSON to stdout for ingestion by a production logging platform. The Compose app activates this profile by default.

Shared settings remain in `application.yaml`. Secrets and deployment-specific addresses still come from environment variables rather than profile files. Future production-only security settings can be added to `application-prod.yaml` without changing local development behavior.


## Architecture

For this project i will the Layered + Hexagonal architecture where each feature is organized module-first, then split into API, application, domain, and infrastructure packages. Domain code is plain Java. 
API and persistence details point inward; feature modules communicate through public application contracts.


```text
┌────────────────────────────────────────────┐
│  API LAYER (Controllers)                   │
│  HTTP in/out, DTOs, validation             │
└───────────────────┬────────────────────────┘
                    ▼
┌────────────────────────────────────────────┐
│  APPLICATION LAYER (Use Cases)             │
│  orchestration, transaction boundary       │
└───────────────────┬────────────────────────┘
                    ▼
┌────────────────────────────────────────────┐
│  DOMAIN LAYER (core)                       │
│  entities, business rules,                 │
│  repository INTERFACES (ports)             │
│  depends on nothing                        │
└───────────────────────_────────────────────┘
                    ▲
                    │ implements
┌───────────────────┴────────────────────────┐
│  INFRASTRUCTURE LAYER (adapters)           │
│  JPA repositories, database, config        │
└────────────────────────────────────────────┘
```