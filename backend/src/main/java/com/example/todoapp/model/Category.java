package com.example.todoapp.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entity class representing a category for todo items.
 * Categories allow users to organize and filter their todos.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Entity
@Table(name = "categories")
@EqualsAndHashCode(exclude = "todos")
@ToString(exclude = "todos")
public class Category {
    
    /**
     * Unique identifier for the category.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the category.
     * Brief description of the category.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Color associated with the category for visual distinction.
     * Stored as a hex color code (e.g., "#FF5722").
     */
    private String color;

    /**
     * Optional description for the category.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The user who owns this category.
     * Many-to-one relationship with User entity.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * List of todos that belong to this category.
     * One-to-many relationship with Todo entity.
     */
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Todo> todos;

    /**
     * The date and time when this category was created.
     * Automatically set when the entity is first persisted.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * The date and time when this category was last modified.
     * Automatically updated whenever the entity is modified.
     */
    @UpdateTimestamp
    private LocalDateTime lastModified;
}
