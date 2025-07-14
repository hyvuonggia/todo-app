package com.example.todoapp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class TodoControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up existing data
        todoRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setProvider("local");
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTodo_IntegrationTest() throws Exception {
        // Given
        Todo newTodo = new Todo();
        newTodo.setTitle("Integration Test Todo");
        newTodo.setDescription("This is a test todo");
        newTodo.setCompleted(false);

        // When & Then
        mockMvc.perform(post("/api/todos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Test Todo"))
                .andExpect(jsonPath("$.description").value("This is a test todo"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.id").exists());

        // Verify todo was actually saved to database
        var savedTodos = todoRepository.findByUserId(testUser.getId());
        assert savedTodos.size() == 1;
        assert savedTodos.get(0).getTitle().equals("Integration Test Todo");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTodos_IntegrationTest() throws Exception {
        // Given - Create some test todos
        Todo todo1 = new Todo();
        todo1.setTitle("First Todo");
        todo1.setDescription("First Description");
        todo1.setCompleted(false);
        todo1.setUser(testUser);

        Todo todo2 = new Todo();
        todo2.setTitle("Second Todo");
        todo2.setDescription("Second Description");
        todo2.setCompleted(true);
        todo2.setUser(testUser);

        todoRepository.save(todo1);
        todoRepository.save(todo2);

        // When & Then
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodo_IntegrationTest() throws Exception {
        // Given - Create a todo first
        Todo existingTodo = new Todo();
        existingTodo.setTitle("Original Title");
        existingTodo.setDescription("Original Description");
        existingTodo.setCompleted(false);
        existingTodo.setUser(testUser);
        existingTodo = todoRepository.save(existingTodo);

        // Prepare update request
        Todo updateRequest = new Todo();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCompleted(true);

        // When & Then
        mockMvc.perform(put("/api/todos/" + existingTodo.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        // Verify update in database
        var updatedTodo = todoRepository.findById(existingTodo.getId());
        assert updatedTodo.isPresent();
        assert updatedTodo.get().getTitle().equals("Updated Title");
        assert updatedTodo.get().isCompleted();
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodo_IntegrationTest() throws Exception {
        // Given - Create a todo first
        Todo existingTodo = new Todo();
        existingTodo.setTitle("To Be Deleted");
        existingTodo.setDescription("This will be deleted");
        existingTodo.setCompleted(false);
        existingTodo.setUser(testUser);
        existingTodo = todoRepository.save(existingTodo);
        Long todoId = existingTodo.getId();

        // When & Then
        mockMvc.perform(delete("/api/todos/" + todoId)
                .with(csrf()))
                .andExpect(status().isOk());

        // Verify deletion in database
        var deletedTodo = todoRepository.findById(todoId);
        assert deletedTodo.isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodo_NotFound_IntegrationTest() throws Exception {
        // Given
        Todo updateRequest = new Todo();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCompleted(true);

        // When & Then
        mockMvc.perform(put("/api/todos/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodo_NotFound_IntegrationTest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/todos/999")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "anotheruser")
    void updateTodo_Unauthorized_IntegrationTest() throws Exception {
        // Given - Create a todo for testuser
        Todo existingTodo = new Todo();
        existingTodo.setTitle("Another User's Todo");
        existingTodo.setDescription("This belongs to testuser");
        existingTodo.setCompleted(false);
        existingTodo.setUser(testUser);
        existingTodo = todoRepository.save(existingTodo);

        // Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password456"));
        anotherUser.setName("Another User");
        anotherUser.setProvider("local");
        userRepository.save(anotherUser);

        // Prepare update request
        Todo updateRequest = new Todo();
        updateRequest.setTitle("Unauthorized Update");
        updateRequest.setDescription("This should fail");
        updateRequest.setCompleted(true);

        // When & Then
        mockMvc.perform(put("/api/todos/" + existingTodo.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        // Verify todo was not updated
        var unchangedTodo = todoRepository.findById(existingTodo.getId());
        assert unchangedTodo.isPresent();
        assert unchangedTodo.get().getTitle().equals("Another User's Todo");
    }

    @Test
    @WithMockUser(username = "anotheruser")
    void deleteTodo_Unauthorized_IntegrationTest() throws Exception {
        // Given - Create a todo for testuser
        Todo existingTodo = new Todo();
        existingTodo.setTitle("Protected Todo");
        existingTodo.setDescription("This belongs to testuser");
        existingTodo.setCompleted(false);
        existingTodo.setUser(testUser);
        existingTodo = todoRepository.save(existingTodo);

        // Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password456"));
        anotherUser.setName("Another User");
        anotherUser.setProvider("local");
        userRepository.save(anotherUser);

        // When & Then
        mockMvc.perform(delete("/api/todos/" + existingTodo.getId())
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        // Verify todo still exists
        var stillExistsTodo = todoRepository.findById(existingTodo.getId());
        assert stillExistsTodo.isPresent();
    }

    @Test
    void getTodos_Unauthorized_IntegrationTest() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTodo_EmptyTitle_IntegrationTest() throws Exception {
        // Given
        Todo newTodo = new Todo();
        newTodo.setTitle(""); // Empty title
        newTodo.setDescription("Valid description");
        newTodo.setCompleted(false);

        // When & Then - This should still work as validation is not implemented
        mockMvc.perform(post("/api/todos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isOk());
    }
}
