package com.example.todoapp.controller;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Todo testTodo;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setCompleted(false);
        testTodo.setUser(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTodos_Success() throws Exception {
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoService.getTodosForUser()).thenReturn(todos);

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Todo"))
                .andExpect(jsonPath("$[0].description").value("Test Description"))
                .andExpect(jsonPath("$[0].completed").value(false));

        verify(todoService).getTodosForUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTodo_Success() throws Exception {
        when(todoService.createTodo(any(Todo.class))).thenReturn(testTodo);

        Todo newTodo = new Todo();
        newTodo.setTitle("New Todo");
        newTodo.setDescription("New Description");
        newTodo.setCompleted(false);

        mockMvc.perform(post("/api/todos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(todoService).createTodo(any(Todo.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodo_Success() throws Exception {
        Todo updatedTodo = new Todo();
        updatedTodo.setId(1L);
        updatedTodo.setTitle("Updated Todo");
        updatedTodo.setDescription("Updated Description");
        updatedTodo.setCompleted(true);
        updatedTodo.setUser(testUser);

        when(todoService.updateTodo(eq(1L), any(Todo.class))).thenReturn(updatedTodo);

        Todo updateRequest = new Todo();
        updateRequest.setTitle("Updated Todo");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCompleted(true);

        mockMvc.perform(put("/api/todos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Todo"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(todoService).updateTodo(eq(1L), any(Todo.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodo_NotFound() throws Exception {
        when(todoService.updateTodo(eq(999L), any(Todo.class)))
                .thenThrow(new RuntimeException("Todo not found"));

        Todo updateRequest = new Todo();
        updateRequest.setTitle("Updated Todo");

        mockMvc.perform(put("/api/todos/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        verify(todoService).updateTodo(eq(999L), any(Todo.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodo_Success() throws Exception {
        doNothing().when(todoService).deleteTodo(1L);

        mockMvc.perform(delete("/api/todos/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(todoService).deleteTodo(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodo_NotFound() throws Exception {
        doThrow(new RuntimeException("Todo not found")).when(todoService).deleteTodo(999L);

        mockMvc.perform(delete("/api/todos/999")
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(todoService).deleteTodo(999L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodo_Unauthorized() throws Exception {
        doThrow(new RuntimeException("You are not authorized to delete this todo"))
                .when(todoService).deleteTodo(1L);

        mockMvc.perform(delete("/api/todos/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(todoService).deleteTodo(1L);
    }

    @Test
    void getTodos_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).getTodosForUser();
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTodo_EmptyTitle() throws Exception {
        Todo invalidTodo = new Todo();
        invalidTodo.setDescription("Valid description");
        invalidTodo.setCompleted(false);

        when(todoService.createTodo(any(Todo.class))).thenReturn(testTodo);

        mockMvc.perform(post("/api/todos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTodo)))
                .andExpect(status().isOk()); // Note: Validation would need to be added to the model

        verify(todoService).createTodo(any(Todo.class));
    }
}
