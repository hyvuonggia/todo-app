package com.example.todoapp.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;

@DataJpaTest
class TodoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoRepository todoRepository;

    private User testUser;
    private User anotherUser;
    private Todo testTodo1;
    private Todo testTodo2;
    private Todo anotherUserTodo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setName("Test User");
        testUser.setProvider("local");

        anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password456");
        anotherUser.setName("Another User");
        anotherUser.setProvider("local");

        testTodo1 = new Todo();
        testTodo1.setTitle("First Todo");
        testTodo1.setDescription("First Description");
        testTodo1.setCompleted(false);

        testTodo2 = new Todo();
        testTodo2.setTitle("Second Todo");
        testTodo2.setDescription("Second Description");
        testTodo2.setCompleted(true);

        anotherUserTodo = new Todo();
        anotherUserTodo.setTitle("Another User's Todo");
        anotherUserTodo.setDescription("Another Description");
        anotherUserTodo.setCompleted(false);
    }

    @Test
    void findByUserId_Success() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        User savedAnotherUser = entityManager.persistAndFlush(anotherUser);

        testTodo1.setUser(savedUser);
        testTodo2.setUser(savedUser);
        anotherUserTodo.setUser(savedAnotherUser);

        entityManager.persistAndFlush(testTodo1);
        entityManager.persistAndFlush(testTodo2);
        entityManager.persistAndFlush(anotherUserTodo);

        // When
        List<Todo> userTodos = todoRepository.findByUserId(savedUser.getId());

        // Then
        assertNotNull(userTodos);
        assertEquals(2, userTodos.size());
        
        // Verify both todos belong to the correct user
        for (Todo todo : userTodos) {
            assertEquals(savedUser.getId(), todo.getUser().getId());
        }
        
        // Verify the todos are the correct ones
        assertTrue(userTodos.stream().anyMatch(todo -> "First Todo".equals(todo.getTitle())));
        assertTrue(userTodos.stream().anyMatch(todo -> "Second Todo".equals(todo.getTitle())));
    }

    @Test
    void findByUserId_NoTodos() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        List<Todo> userTodos = todoRepository.findByUserId(savedUser.getId());

        // Then
        assertNotNull(userTodos);
        assertTrue(userTodos.isEmpty());
    }

    @Test
    void findByUserId_NonExistentUser() {
        // When
        List<Todo> userTodos = todoRepository.findByUserId(999L);

        // Then
        assertNotNull(userTodos);
        assertTrue(userTodos.isEmpty());
    }

    @Test
    void save_Success() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        testTodo1.setUser(savedUser);

        // When
        Todo savedTodo = todoRepository.save(testTodo1);

        // Then
        assertNotNull(savedTodo.getId());
        assertEquals("First Todo", savedTodo.getTitle());
        assertEquals("First Description", savedTodo.getDescription());
        assertFalse(savedTodo.isCompleted());
        assertEquals(savedUser.getId(), savedTodo.getUser().getId());
    }

    @Test
    void findById_Success() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        testTodo1.setUser(savedUser);
        Todo savedTodo = entityManager.persistAndFlush(testTodo1);

        // When
        Optional<Todo> found = todoRepository.findById(savedTodo.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("First Todo", found.get().getTitle());
        assertEquals("First Description", found.get().getDescription());
        assertEquals(savedUser.getId(), found.get().getUser().getId());
    }

    @Test
    void findById_NotFound() {
        // When
        Optional<Todo> found = todoRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void update_Success() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        testTodo1.setUser(savedUser);
        Todo savedTodo = entityManager.persistAndFlush(testTodo1);

        // When
        savedTodo.setTitle("Updated Title");
        savedTodo.setDescription("Updated Description");
        savedTodo.setCompleted(true);
        Todo updatedTodo = todoRepository.save(savedTodo);

        // Then
        assertEquals("Updated Title", updatedTodo.getTitle());
        assertEquals("Updated Description", updatedTodo.getDescription());
        assertTrue(updatedTodo.isCompleted());
        assertEquals(savedUser.getId(), updatedTodo.getUser().getId());
    }

    @Test
    void delete_Success() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        testTodo1.setUser(savedUser);
        Todo savedTodo = entityManager.persistAndFlush(testTodo1);
        Long todoId = savedTodo.getId();

        // When
        todoRepository.delete(savedTodo);
        entityManager.flush();

        // Then
        Optional<Todo> found = todoRepository.findById(todoId);
        assertFalse(found.isPresent());
    }

    @Test
    void findByUserId_OrderingTest() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // Create multiple todos with different creation times
        Todo todo1 = new Todo();
        todo1.setTitle("A Todo");
        todo1.setDescription("First created");
        todo1.setCompleted(false);
        todo1.setUser(savedUser);

        Todo todo2 = new Todo();
        todo2.setTitle("B Todo");
        todo2.setDescription("Second created");
        todo2.setCompleted(false);
        todo2.setUser(savedUser);

        Todo todo3 = new Todo();
        todo3.setTitle("C Todo");
        todo3.setDescription("Third created");
        todo3.setCompleted(true);
        todo3.setUser(savedUser);

        entityManager.persistAndFlush(todo1);
        entityManager.persistAndFlush(todo2);
        entityManager.persistAndFlush(todo3);

        // When
        List<Todo> userTodos = todoRepository.findByUserId(savedUser.getId());

        // Then
        assertNotNull(userTodos);
        assertEquals(3, userTodos.size());
        
        // Note: The order depends on the database implementation
        // This test verifies that all todos are returned, regardless of order
        assertTrue(userTodos.stream().anyMatch(todo -> "A Todo".equals(todo.getTitle())));
        assertTrue(userTodos.stream().anyMatch(todo -> "B Todo".equals(todo.getTitle())));
        assertTrue(userTodos.stream().anyMatch(todo -> "C Todo".equals(todo.getTitle())));
    }

    @Test
    void findByUserId_CompletedAndIncomplete() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        testTodo1.setUser(savedUser);
        testTodo1.setCompleted(false);
        
        testTodo2.setUser(savedUser);
        testTodo2.setCompleted(true);

        entityManager.persistAndFlush(testTodo1);
        entityManager.persistAndFlush(testTodo2);

        // When
        List<Todo> userTodos = todoRepository.findByUserId(savedUser.getId());

        // Then
        assertNotNull(userTodos);
        assertEquals(2, userTodos.size());
        
        long completedCount = userTodos.stream().mapToInt(todo -> todo.isCompleted() ? 1 : 0).sum();
        long incompleteCount = userTodos.stream().mapToInt(todo -> !todo.isCompleted() ? 1 : 0).sum();
        
        assertEquals(1, completedCount);
        assertEquals(1, incompleteCount);
    }
}
