package com.example.todoapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity class representing a user in the Todo application.
 * This class maps to the "users" table in the database and contains
 * user authentication and profile information.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Entity
@Table(name = "users")
public class User {
    
    /**
     * Unique identifier for the user.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for the user.
     * Used for authentication and must be unique across all users.
     */
    @Column(unique = true)
    private String username;

    /**
     * Display name of the user.
     * This is the friendly name shown in the application.
     */
    private String name;

    /**
     * Email address of the user.
     * Must be unique across all users and used for account management.
     */
    @Column(unique = true)
    private String email;

    /**
     * Encoded password for the user.
     * Stored as a BCrypt hash for security.
     */
    private String password;

    /**
     * Authentication provider for the user.
     * Indicates how the user was registered (e.g., "local", "google", "facebook").
     */
    private String provider;
}
