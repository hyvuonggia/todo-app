package com.example.todoapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todoapp.model.Category;
import com.example.todoapp.service.CategoryService;

/**
 * REST controller for managing Category operations.
 * This controller handles HTTP requests related to Category items including
 * creating, reading, updating, and deleting categories for the authenticated user.
 *
 * @author Todo App Team
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Retrieves all categories for the currently authenticated user.
     * This endpoint fetches categories specific to the user making the request,
     * ensuring data isolation between different users.
     *
     * @return List of Category objects belonging to the authenticated user
     */
    @GetMapping
    public List<Category> getCategories() {
        return categoryService.getCategoriesForUser();
    }

    /**
     * Creates a new category for the authenticated user.
     * The category will be automatically associated with the currently logged-in user.
     *
     * @param category the Category object to be created, containing name, color, and description
     * @return the created Category object with generated ID and timestamps
     */
    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    /**
     * Updates an existing category by its ID.
     * The category must belong to the authenticated user to be updated.
     *
     * @param id the unique identifier of the category to update
     * @param category the Category object containing updated information
     * @return the updated Category object with modified fields
     * @throws RuntimeException if the category is not found or doesn't belong to the user
     */
    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    /**
     * Deletes a category by its ID.
     * The category must belong to the authenticated user to be deleted.
     *
     * @param id the unique identifier of the category to delete
     * @return ResponseEntity with OK status if deletion is successful
     * @throws RuntimeException if the category is not found or doesn't belong to the user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves a specific category by its ID.
     * The category must belong to the authenticated user.
     *
     * @param id the unique identifier of the category to retrieve
     * @return the Category object
     * @throws RuntimeException if the category is not found or doesn't belong to the user
     */
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    /**
     * Exception handler for RuntimeExceptions thrown by service methods.
     * Maps specific error messages to appropriate HTTP status codes.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.contains("not authorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
        } else if (message.contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }
}
