package com.minidoodle.schedular.api.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public final class ApiTestClient {

    private final MockMvc mvc;

    public ApiTestClient(MockMvc mvc) {
        this.mvc = Objects.requireNonNull(mvc, "mvc must not be null");
    }

    public String createUser(String email) throws Exception {
        String response = registerUser(email)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/users/")))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    public ResultActions registerUser(String email) throws Exception {
        return mvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson(email)));
    }

    public ResultActions getUser(String userId) throws Exception {
        return mvc.perform(get("/api/v1/users/{id}", userId));
    }

    public String createSlot(String userId, String start, String end) throws Exception {
        String response = requestSlotCreation(userId, start, end)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    public ResultActions requestSlotCreation(String userId, String start, String end) throws Exception {
        return mvc.perform(post("/api/v1/slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(slotJson(userId, start, end)));
    }

    public ResultActions updateSlot(String slotId, String start, String end) throws Exception {
        return mvc.perform(patch("/api/v1/slots/{id}", slotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rangeJson(start, end)));
    }

    public ResultActions getSlot(String slotId) throws Exception {
        return mvc.perform(get("/api/v1/slots/{id}", slotId));
    }

    public ResultActions getUserSlots(String userId, String from, String to) throws Exception {
        return mvc.perform(get("/api/v1/users/{id}/slots", userId)
                .queryParam("from", from)
                .queryParam("to", to));
    }

    public ResultActions deleteSlot(String slotId) throws Exception {
        return mvc.perform(delete("/api/v1/slots/{id}", slotId));
    }

    public ResultActions markSlotBusy(String slotId) throws Exception {
        return mvc.perform(post("/api/v1/slots/{id}/busy", slotId));
    }

    public ResultActions markSlotFree(String slotId) throws Exception {
        return mvc.perform(post("/api/v1/slots/{id}/free", slotId));
    }

    public String bookMeeting(String slotId) throws Exception {
        String response = requestMeetingBooking(slotId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotId").value(slotId))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    public ResultActions requestMeetingBooking(String slotId) throws Exception {
        return mvc.perform(post("/api/v1/slots/{id}/meetings", slotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(meetingJson()));
    }

    public int meetingBookingStatus(String slotId) throws Exception {
        return requestMeetingBooking(slotId).andReturn().getResponse().getStatus();
    }

    public ResultActions cancelMeeting(String meetingId) throws Exception {
        return mvc.perform(delete("/api/v1/meetings/{id}", meetingId));
    }

    public ResultActions getAvailability(String userId, String from, String to) throws Exception {
        return mvc.perform(get("/api/v1/users/{id}/availability", userId)
                .queryParam("from", from)
                .queryParam("to", to));
    }

    public ResultActions getUserMeetings(String userId, String from, String to) throws Exception {
        return mvc.perform(get("/api/v1/users/{id}/meetings", userId)
                .queryParam("from", from)
                .queryParam("to", to));
    }

    public ResultActions getOpenApi() throws Exception {
        return mvc.perform(get("/v3/api-docs"));
    }

    public ResultActions getSwaggerUi() throws Exception {
        return mvc.perform(get("/swagger-ui.html"));
    }

    private static String userJson(String email) {
        return """
                {"name":"Alice","email":"%s"}
                """.formatted(email);
    }

    private static String slotJson(String userId, String start, String end) {
        return """
                {"ownerId":"%s","start":"%s","end":"%s"}
                """.formatted(userId, start, end);
    }

    private static String rangeJson(String start, String end) {
        return """
                {"start":"%s","end":"%s"}
                """.formatted(start, end);
    }

    private static String meetingJson() {
        return """
                {
                  "title":"Planning",
                  "description":"Roadmap",
                  "participants":[{"name":"Alice","email":"alice@example.com"}]
                }
                """;
    }
}
