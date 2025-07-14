package com.example.todoapp.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.todoapp.model.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setName("Test User");
        testUser.setProvider("local");
    }

    @Test
    void findByEmail_Success() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void findByEmail_NotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_Success() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void findByUsername_NotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistentuser");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_CaseInsensitive() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("TEST@EXAMPLE.COM");

        // Then
        // Note: This test depends on database configuration for case sensitivity
        // H2 is case-insensitive by default for emails
        assertFalse(found.isPresent()); // Expecting case-sensitive behavior
    }

    @Test
    void save_Success() {
        // When
        User saved = userRepository.save(testUser);

        // Then
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
        assertEquals("test@example.com", saved.getEmail());
        assertEquals("local", saved.getProvider());
    }

    @Test
    void save_UniqueConstraintViolation() {
        // Given
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // Same username
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPassword("password456");
        duplicateUser.setName("Different User");
        duplicateUser.setProvider("local");

        // When & Then
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    void save_EmailUniqueConstraintViolation() {
        // Given
        entityManager.persistAndFlush(testUser);
        entityManager.clear();

        User duplicateEmailUser = new User();
        duplicateEmailUser.setUsername("differentuser");
        duplicateEmailUser.setEmail("test@example.com"); // Same email
        duplicateEmailUser.setPassword("password456");
        duplicateEmailUser.setName("Different User");
        duplicateEmailUser.setProvider("local");

        // When & Then
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateEmailUser);
        });
    }

    @Test
    void findAll_MultipleUsers() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setName("User One");
        user1.setProvider("local");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setName("User Two");
        user2.setProvider("local");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        Iterable<User> users = userRepository.findAll();

        // Then
        assertNotNull(users);
        assertEquals(2, ((java.util.List<User>) users).size());
    }

    @Test
    void deleteById_Success() {
        // Given
        User saved = entityManager.persistAndFlush(testUser);
        Long userId = saved.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }
}
