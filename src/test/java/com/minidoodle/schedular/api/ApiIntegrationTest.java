package com.minidoodle.schedular.api;

import com.minidoodle.schedular.api.support.ApiTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiIntegrationTest {

    private static final String T09 = "2026-08-14T09:00:00Z";
    private static final String T0930 = "2026-08-14T09:30:00Z";
    private static final String T10 = "2026-08-14T10:00:00Z";
    private static final String T1030 = "2026-08-14T10:30:00Z";
    private static final String T11 = "2026-08-14T11:00:00Z";
    private static final String T12 = "2026-08-14T12:00:00Z";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("minidoodle")
            .withUsername("minidoodle")
            .withPassword("minidoodle");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiTestClient api;

    @BeforeEach
    void prepareTest() {
        jdbcTemplate.execute("TRUNCATE TABLE meeting_participant, meeting, time_slot, users CASCADE");
        api = new ApiTestClient(mvc);
    }

    @Test
    void returnsTheRegisteredUser() throws Exception {
        String userId = api.createUser("alice@example.com");

        api.getUser(userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void rejectsDuplicateAndInvalidUserEmails() throws Exception {
        api.createUser("alice@example.com");

        api.registerUser("alice@example.com")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/users"));

        api.registerUser("invalid")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsOverlappingSlotsAndUnknownOwners() throws Exception {
        String userId = api.createUser("alice@example.com");
        api.createSlot(userId, T09, T10);

        api.requestSlotCreation(userId, T0930, T1030)
                .andExpect(status().isUnprocessableEntity());

        api.requestSlotCreation(UUID.randomUUID().toString(), T10, T11)
                .andExpect(status().isNotFound());
    }

    @Test
    void changesAFreeSlotToBusyAndBackToFree() throws Exception {
        String userId = api.createUser("alice@example.com");
        String slotId = api.createSlot(userId, T09, T10);

        api.markSlotBusy(slotId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BUSY"));

        api.markSlotFree(slotId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    void rejectsInvalidSlotRanges() throws Exception {
        String userId = api.createUser("alice@example.com");
        String slotId = api.createSlot(userId, T09, T10);

        api.updateSlot(slotId, T10, T09)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void retrievesOneSlotAndListsTheUsersRawSlotsForAWindow() throws Exception {
        String userId = api.createUser("alice@example.com");
        String laterSlotId = api.createSlot(userId, T10, T11);
        String earlierSlotId = api.createSlot(userId, T09, T10);
        api.createSlot(userId, T11, T12);

        String otherUserId = api.createUser("bob@example.com");
        api.createSlot(otherUserId, T09, T10);

        api.getSlot(earlierSlotId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(earlierSlotId))
                .andExpect(jsonPath("$.ownerId").value(userId))
                .andExpect(jsonPath("$.status").value("FREE"));

        api.getUserSlots(userId, T09, T11)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(earlierSlotId))
                .andExpect(jsonPath("$[1].id").value(laterSlotId));
    }

    @Test
    void slotReadsRejectInvalidWindowsAndUnknownResources() throws Exception {
        String userId = api.createUser("alice@example.com");

        api.getUserSlots(userId, T11, T09)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        api.getUserSlots(UUID.randomUUID().toString(), T09, T11)
                .andExpect(status().isNotFound());

        api.getSlot(UUID.randomUUID().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsBookingABusySlot() throws Exception {
        String userId = api.createUser("alice@example.com");
        String slotId = api.createSlot(userId, T11, T12);
        api.markSlotBusy(slotId).andExpect(status().isOk());

        api.requestMeetingBooking(slotId)
                .andExpect(status().isConflict());
    }

    @Test
    void cancellingAnUnknownMeetingReturnsNotFound() throws Exception {
        api.cancelMeeting(UUID.randomUUID().toString())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void fullLifecycleChangesAvailabilityFromBookedBackToFree() throws Exception {
        String userId = api.createUser("alice@example.com");
        String slotId = api.createSlot(userId, T09, T10);

        String meetingId = api.bookMeeting(slotId);
        expectAvailabilityStatus(userId, "BOOKED");

        api.updateSlot(slotId, T10, T11)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
        api.deleteSlot(slotId).andExpect(status().isConflict());
        api.markSlotFree(slotId).andExpect(status().isConflict());

        api.cancelMeeting(meetingId).andExpect(status().isNoContent());
        expectAvailabilityStatus(userId, "FREE");
        api.deleteSlot(slotId).andExpect(status().isNoContent());
    }

    @Test
    void onlyOneConcurrentBookingSucceeds() throws Exception {
        String userId = api.createUser("alice@example.com");
        String slotId = api.createSlot(userId, T09, T10);
        CyclicBarrier startTogether = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Callable<Integer> bookMeeting = () -> {
                startTogether.await();
                return api.meetingBookingStatus(slotId);
            };

            Future<Integer> firstBooking = executor.submit(bookMeeting);
            Future<Integer> secondBooking = executor.submit(bookMeeting);

            List<Integer> statuses = List.of(
                            firstBooking.get(5, TimeUnit.SECONDS),
                            secondBooking.get(5, TimeUnit.SECONDS)
                    ).stream()
                    .sorted()
                    .toList();

            assertEquals(List.of(201, 409), statuses);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void mergesAdjacentFreeSlotsAcrossTheRequestedWindow() throws Exception {
        String userId = api.createUser("alice@example.com");
        api.createSlot(userId, T09, T10);
        api.createSlot(userId, T10, T11);

        api.getAvailability(userId, T09, T11)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.ranges.length()").value(1))
                .andExpect(jsonPath("$.ranges[0].start").value(T09))
                .andExpect(jsonPath("$.ranges[0].end").value(T11))
                .andExpect(jsonPath("$.ranges[0].status").value("FREE"));
    }

    @Test
    void rejectsInvalidAvailabilityRequestsAndUnknownUsers() throws Exception {
        String userId = api.createUser("alice@example.com");

        api.getAvailability(userId, T11, T09)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        api.getAvailability(userId, "not-an-instant", T11)
                .andExpect(status().isBadRequest());

        api.getAvailability(UUID.randomUUID().toString(), T09, T11)
                .andExpect(status().isNotFound());
    }

    @Test
    void openApiContainsVersionedPathsWithoutForbiddenApiTerminology() throws Exception {
        api.getOpenApi()
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/v1/users")))
                .andExpect(content().string(containsString("/api/v1/slots")))
                .andExpect(content().string(containsString("/api/v1/users/{userId}/slots")))
                .andExpect(content().string(containsString("/api/v1/slots/{id}")))
                .andExpect(content().string(not(containsStringIgnoringCase("calendar"))));

        api.getSwaggerUi().andExpect(status().is3xxRedirection());
    }

    private void expectAvailabilityStatus(String userId, String expectedStatus) throws Exception {
        api.getAvailability(userId, T09, T10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranges.length()").value(1))
                .andExpect(jsonPath("$.ranges[0].status").value(expectedStatus));
    }
}
