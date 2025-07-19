package com.example.todoapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todoapp.model.Todo;

/**
 * Repository interface for Todo entity data access operations.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    /**
     * Finds all todo items belonging to a specific user.
     * 
     * @param userId the ID of the user whose todos to retrieve
     * @return a list of todo items belonging to the specified user
     */
    List<Todo> findByUserId(Long userId);

    /**
     * Finds all todo items belonging to a specific user and category.
     * 
     * @param userId the ID of the user whose todos to retrieve
     * @param categoryId the ID of the category to filter by
     * @return a list of todo items belonging to the specified user and category
     */
    List<Todo> findByUserIdAndCategoryId(Long userId, Long categoryId);

    /**
     * Finds all todo items belonging to a specific user with no category.
     * 
     * @param userId the ID of the user whose todos to retrieve
     * @return a list of todo items belonging to the specified user with no category
     */
    List<Todo> findByUserIdAndCategoryIsNull(Long userId);
}
