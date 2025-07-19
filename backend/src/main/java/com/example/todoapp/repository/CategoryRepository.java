package com.example.todoapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todoapp.model.Category;

/**
 * Repository interface for Category entity data access operations.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Finds all categories belonging to a specific user.
     * 
     * @param userId the ID of the user whose categories to retrieve
     * @return a list of categories belonging to the specified user
     */
    List<Category> findByUserId(Long userId);

    /**
     * Finds a category by name and user ID.
     * 
     * @param name the name of the category
     * @param userId the ID of the user
     * @return the category if found, null otherwise
     */
    Category findByNameAndUserId(String name, Long userId);
}
