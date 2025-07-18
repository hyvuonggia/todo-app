package com.example.todoapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.todoapp.model.User;
import com.example.todoapp.repository.UserRepository;

/**
 * Service class for user-related business logic.
 * Handles user registration and management operations.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     * Encodes the user's password using BCrypt and sets the provider to "local".
     * 
     * @param user the user entity containing registration information
     * @return the saved user entity with encoded password and generated ID
     * @throws RuntimeException if user registration fails
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProvider("local");
        return userRepository.save(user);
    }
    
    /**
     * Finds a user by username.
     * 
     * @param username the username to search for
     * @return the user entity if found
     * @throws RuntimeException if user is not found
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}
