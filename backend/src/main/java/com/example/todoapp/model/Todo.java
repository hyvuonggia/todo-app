package com.example.todoapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

/**
 * Entity class representing a todo item in the Todo application.
 * This class maps to the "todo" table in the database and contains
 * todo item information with a many-to-one relationship to User.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Entity
public class Todo {
    
    /**
     * Unique identifier for the todo item.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the todo item.
     * Brief description of what needs to be done.
     */
    private String title;

    /**
     * Detailed description of the todo item.
     * Optional field providing more context about the task.
     */
    private String description;

    /**
     * Completion status of the todo item.
     * True if the task has been completed, false otherwise.
     */
    private boolean completed;

    /**
     * The user who owns this todo item.
     * Many-to-one relationship with User entity.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
