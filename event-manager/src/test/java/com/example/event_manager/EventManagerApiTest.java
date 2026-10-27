package com.example.event_manager;

import com.example.event_manager.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * End-to-end tests of the REST API (security, validation, business rules) against an in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EventManagerApiTest {

    private static final String ORGANIZER_CODE = "test-organizer-code";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // No real emails are sent in tests.
    @MockitoBean
    private EmailService emailService;

    // ---------- helpers ----------

    private String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    private String registerBody(String username, String role, String organizerCode) throws Exception {
        var node = objectMapper.createObjectNode()
                .put("username", username)
                .put("email", username + "@example.com")
                .put("password", "secret123")
                .put("role", role);
        if (organizerCode != null) {
            node.put("organizerCode", organizerCode);
        }
        return objectMapper.writeValueAsString(node);
    }

    private String registerAndGetToken(String username, String role, String organizerCode) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, role, organizerCode)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String organizerToken() throws Exception {
        return registerAndGetToken(unique("org"), "ROLE_ORGANIZER", ORGANIZER_CODE);
    }

    private String userToken() throws Exception {
        return registerAndGetToken(unique("usr"), "ROLE_USER", null);
    }

    private MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    private long createEvent(String organizerToken, int spots) throws Exception {
        String body = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("title", "Test Event")
                .put("description", "Description")
                .put("location", "Bengaluru")
                .put("eventDate", "2099-01-01T10:00:00")
                .put("availableSpots", spots));
        MvcResult result = mockMvc.perform(withToken(post("/api/events"), organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    // ---------- authentication / authorization ----------

    @Test
    void registerAsAttendeeReturnsToken() throws Exception {
        registerAndGetToken(unique("usr"), "ROLE_USER", null);
    }

    @Test
    void registerAsOrganizerWithoutInviteCodeIsForbidden() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(unique("evil"), "ROLE_ORGANIZER", null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerAsOrganizerWithWrongInviteCodeIsForbidden() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(unique("evil"), "ROLE_ORGANIZER", "wrong-code")))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateUsernameIsConflict() throws Exception {
        String username = unique("dup");
        registerAndGetToken(username, "ROLE_USER", null);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, "ROLE_USER", null)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        String username = unique("login");
        registerAndGetToken(username, "ROLE_USER", null);
        String body = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("username", username).put("password", "not-the-password"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/my-registrations")).andExpect(status().isUnauthorized());
    }

    @Test
    void attendeeCannotCreateEvent() throws Exception {
        String body = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("title", "x").put("description", "x").put("location", "x")
                .put("eventDate", "2099-01-01T10:00:00").put("availableSpots", 5));
        mockMvc.perform(withToken(post("/api/events"), userToken())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    // ---------- events ----------

    @Test
    void anyoneCanBrowseEvents() throws Exception {
        long id = createEvent(organizerToken(), 5);
        mockMvc.perform(get("/api/events")).andExpect(status().isOk());
        mockMvc.perform(get("/api/events/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    @Test
    void unknownEventIsNotFound() throws Exception {
        mockMvc.perform(get("/api/events/99999999")).andExpect(status().isNotFound());
    }

    @Test
    void organizerCanUpdateOwnEventButNotSomeoneElses() throws Exception {
        String owner = organizerToken();
        String other = organizerToken();
        long id = createEvent(owner, 5);

        String body = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("title", "Renamed").put("description", "d").put("location", "l")
                .put("eventDate", "2099-02-02T10:00:00").put("availableSpots", 7));

        mockMvc.perform(withToken(put("/api/events/" + id), owner)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renamed"));

        mockMvc.perform(withToken(put("/api/events/" + id), other)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerCanDeleteOwnEventButNotSomeoneElses() throws Exception {
        String owner = organizerToken();
        String other = organizerToken();
        long id = createEvent(owner, 5);

        mockMvc.perform(withToken(delete("/api/events/" + id), other)).andExpect(status().isForbidden());
        mockMvc.perform(withToken(delete("/api/events/" + id), owner)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/events/" + id)).andExpect(status().isNotFound());
    }

    // ---------- registration rules ----------

    @Test
    void registeringTakesASpotAndSecondAttemptIsRejected() throws Exception {
        long id = createEvent(organizerToken(), 2);
        String user = userToken();

        mockMvc.perform(withToken(post("/api/events/" + id + "/register"), user))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/events/" + id))
                .andExpect(jsonPath("$.availableSpots").value(1));

        mockMvc.perform(withToken(post("/api/events/" + id + "/register"), user))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/events/" + id))
                .andExpect(jsonPath("$.availableSpots").value(1));
    }

    @Test
    void fullEventRejectsFurtherRegistrations() throws Exception {
        long id = createEvent(organizerToken(), 1);

        mockMvc.perform(withToken(post("/api/events/" + id + "/register"), userToken()))
                .andExpect(status().isCreated());
        mockMvc.perform(withToken(post("/api/events/" + id + "/register"), userToken()))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/events/" + id))
                .andExpect(jsonPath("$.availableSpots").value(0));
    }

    // ---------- tickets and check-in ----------

    @Test
    void organizerCanCheckInTicketOnlyOnce() throws Exception {
        String organizer = organizerToken();
        long id = createEvent(organizer, 5);
        String user = userToken();

        mockMvc.perform(withToken(post("/api/events/" + id + "/register"), user))
                .andExpect(status().isCreated());

        MvcResult tickets = mockMvc.perform(withToken(get("/api/my-registrations"), user))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode first = objectMapper.readTree(tickets.getResponse().getContentAsString()).get(0);
        String ticketCode = first.get("qrCodeData").asText();
        assertEquals(36, ticketCode.length(), "ticket code should be a random UUID");

        String checkInBody = objectMapper.writeValueAsString(
                objectMapper.createObjectNode().put("qrCodeData", ticketCode));

        mockMvc.perform(withToken(post("/api/organizer/my-events/" + id + "/check-in"), organizer)
                        .contentType(MediaType.APPLICATION_JSON).content(checkInBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(true));

        mockMvc.perform(withToken(post("/api/organizer/my-events/" + id + "/check-in"), organizer)
                        .contentType(MediaType.APPLICATION_JSON).content(checkInBody))
                .andExpect(status().isConflict());

        mockMvc.perform(withToken(get("/api/organizer/my-events/" + id + "/attendees"), organizer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checkedIn").value(true));
    }

    @Test
    void attendeeCannotUseOrganizerEndpoints() throws Exception {
        mockMvc.perform(withToken(get("/api/organizer/my-events"), userToken()))
                .andExpect(status().isForbidden());
    }
}
