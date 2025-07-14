package com.example.todoapp.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;

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

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void getTodosForUser_Success() {
        // Given
        List<Todo> expectedTodos = Arrays.asList(testTodo);
        mockSecurityContext();
        when(todoRepository.findByUserId(1L)).thenReturn(expectedTodos);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            List<Todo> result = todoService.getTodosForUser();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Todo", result.get(0).getTitle());
            verify(todoRepository).findByUserId(1L);
        }
    }

    @Test
    void createTodo_Success() {
        // Given
        Todo newTodo = new Todo();
        newTodo.setTitle("New Todo");
        newTodo.setDescription("New Description");
        newTodo.setCompleted(false);

        Todo savedTodo = new Todo();
        savedTodo.setId(2L);
        savedTodo.setTitle("New Todo");
        savedTodo.setDescription("New Description");
        savedTodo.setCompleted(false);
        savedTodo.setUser(testUser);

        mockSecurityContext();
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Todo result = todoService.createTodo(newTodo);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals("New Todo", result.getTitle());
            assertEquals(testUser, result.getUser());
            verify(todoRepository).save(any(Todo.class));
        }
    }

    @Test
    void updateTodo_Success() {
        // Given
        Todo updateDetails = new Todo();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        Todo updatedTodo = new Todo();
        updatedTodo.setId(1L);
        updatedTodo.setTitle("Updated Title");
        updatedTodo.setDescription("Updated Description");
        updatedTodo.setCompleted(true);
        updatedTodo.setUser(testUser);

        mockSecurityContext();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            Todo result = todoService.updateTodo(1L, updateDetails);

            // Then
            assertNotNull(result);
            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated Description", result.getDescription());
            assertTrue(result.isCompleted());
            verify(todoRepository).findById(1L);
            verify(todoRepository).save(any(Todo.class));
        }
    }

    @Test
    void updateTodo_NotFound() {
        // Given
        mockSecurityContext();
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                todoService.updateTodo(999L, new Todo());
            });

            assertEquals("Todo not found", exception.getMessage());
            verify(todoRepository).findById(999L);
            verify(todoRepository, never()).save(any(Todo.class));
        }
    }

    @Test
    void updateTodo_Unauthorized() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");

        Todo anotherUserTodo = new Todo();
        anotherUserTodo.setId(1L);
        anotherUserTodo.setTitle("Another User's Todo");
        anotherUserTodo.setUser(anotherUser);

        mockSecurityContext();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(anotherUserTodo));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                todoService.updateTodo(1L, new Todo());
            });

            assertEquals("You are not authorized to update this todo", exception.getMessage());
            verify(todoRepository).findById(1L);
            verify(todoRepository, never()).save(any(Todo.class));
        }
    }

    @Test
    void deleteTodo_Success() {
        // Given
        mockSecurityContext();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            todoService.deleteTodo(1L);

            // Then
            verify(todoRepository).findById(1L);
            verify(todoRepository).delete(testTodo);
        }
    }

    @Test
    void deleteTodo_NotFound() {
        // Given
        mockSecurityContext();
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                todoService.deleteTodo(999L);
            });

            assertEquals("Todo not found", exception.getMessage());
            verify(todoRepository).findById(999L);
            verify(todoRepository, never()).delete(any(Todo.class));
        }
    }

    @Test
    void deleteTodo_Unauthorized() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");

        Todo anotherUserTodo = new Todo();
        anotherUserTodo.setId(1L);
        anotherUserTodo.setTitle("Another User's Todo");
        anotherUserTodo.setUser(anotherUser);

        mockSecurityContext();
        when(todoRepository.findById(1L)).thenReturn(Optional.of(anotherUserTodo));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                todoService.deleteTodo(1L);
            });

            assertEquals("You are not authorized to delete this todo", exception.getMessage());
            verify(todoRepository).findById(1L);
            verify(todoRepository, never()).delete(any(Todo.class));
        }
    }

    @Test
    void getCurrentUser_UserNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("nonexistentuser");
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                todoService.getTodosForUser();
            });

            assertEquals("User not found", exception.getMessage());
        }
    }

    @Test
    void getCurrentUser_PrincipalAsString() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("testuser"); // String instead of UserDetails
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTodo));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            List<Todo> result = todoService.getTodosForUser();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository).findByUsername("testuser");
        }
    }
}
