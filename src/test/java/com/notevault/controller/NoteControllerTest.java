package com.notevault.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notevault.dto.NoteRequest;
import com.notevault.dto.RegisterRequest;
import com.notevault.repository.NoteRepository;
import com.notevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    private String userAToken;
    private String userBToken;

    @BeforeEach
    void setUp() throws Exception {
        noteRepository.deleteAll();
        userRepository.deleteAll();

        // Register User A
        RegisterRequest userA = new RegisterRequest("User Alpha", "alpha@example.com", "password123");
        MvcResult resA = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userA)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonA = objectMapper.readTree(resA.getResponse().getContentAsString());
        userAToken = jsonA.path("data").path("token").asText();

        // Register User B
        RegisterRequest userB = new RegisterRequest("User Beta", "beta@example.com", "password123");
        MvcResult resB = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userB)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonB = objectMapper.readTree(resB.getResponse().getContentAsString());
        userBToken = jsonB.path("data").path("token").asText();
    }

    @Test
    @DisplayName("GET /api/notes - Unauthenticated access returns 401 Unauthorized")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/notes - Creates note for authenticated user")
    void shouldCreateNoteSuccessfully() throws Exception {
        NoteRequest request = new NoteRequest("Java Streams", "Learn how to use stream(), filter(), and map()");

        mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Java Streams"))
                .andExpect(jsonPath("$.data.content").value("Learn how to use stream(), filter(), and map()"));
    }

    @Test
    @DisplayName("GET /api/notes - Retrieves all notes for authenticated user only")
    void shouldGetAllNotesForCaller() throws Exception {
        // Create 2 notes for User A
        createNoteHelper(userAToken, "Note 1", "Content 1");
        createNoteHelper(userAToken, "Note 2", "Content 2");

        // Create 1 note for User B
        createNoteHelper(userBToken, "Note B", "Content B");

        // User A should only see their 2 notes
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        // User B should only see their 1 note
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Note B"));
    }

    @Test
    @DisplayName("GET /api/notes?search=java - Searches notes by title")
    void shouldSearchNotesByTitle() throws Exception {
        createNoteHelper(userAToken, "Java 21 Virtual Threads", "Concurrent programming in Java");
        createNoteHelper(userAToken, "Spring Boot Security", "JWT authentication and RBAC");
        createNoteHelper(userAToken, "Java Streams and Lambdas", "Functional processing");

        mockMvc.perform(get("/api/notes?search=java")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/notes/{id} - Fetches note by ID")
    void shouldGetNoteById() throws Exception {
        Long noteId = createNoteHelper(userAToken, "Single Note", "Some note text");

        mockMvc.perform(get("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(noteId))
                .andExpect(jsonPath("$.data.title").value("Single Note"));
    }

    @Test
    @DisplayName("PUT /api/notes/{id} - Updates existing note")
    void shouldUpdateNote() throws Exception {
        Long noteId = createNoteHelper(userAToken, "Original Title", "Original Content");

        NoteRequest updateReq = new NoteRequest("Updated Title", "Updated Content");
        mockMvc.perform(put("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.content").value("Updated Content"));
    }

    @Test
    @DisplayName("DELETE /api/notes/{id} - Deletes note")
    void shouldDeleteNote() throws Exception {
        Long noteId = createNoteHelper(userAToken, "To Delete", "Delete me");

        mockMvc.perform(delete("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Note deleted successfully")));

        // Verify note no longer exists
        mockMvc.perform(get("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Data Privacy: User B cannot access User A's note")
    void shouldPreventCrossUserDataAccess() throws Exception {
        Long userANoteId = createNoteHelper(userAToken, "Private Note", "User A secret data");

        // User B attempts to read User A's note
        mockMvc.perform(get("/api/notes/" + userANoteId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        // User B attempts to edit User A's note
        NoteRequest updateReq = new NoteRequest("Hacked Title", "Hacked Content");
        mockMvc.perform(put("/api/notes/" + userANoteId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // User B attempts to delete User A's note
        mockMvc.perform(delete("/api/notes/" + userANoteId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }

    private Long createNoteHelper(String token, String title, String content) throws Exception {
        NoteRequest req = new NoteRequest(title, content);
        MvcResult res = mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.path("data").path("id").asLong();
    }
}
