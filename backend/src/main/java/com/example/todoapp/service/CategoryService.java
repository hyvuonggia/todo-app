package com.example.todoapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.todoapp.model.Category;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.CategoryRepository;
import com.example.todoapp.repository.UserRepository;

/**
 * Service class for managing Category operations.
 * Provides business logic for category-related operations including CRUD operations.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves all categories for the currently authenticated user.
     * 
     * @return List of categories belonging to the authenticated user
     */
    public List<Category> getCategoriesForUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return categoryRepository.findByUserId(user.getId());
    }

    /**
     * Creates a new category for the authenticated user.
     * 
     * @param category the Category object to be created
     * @return the created Category object with generated ID and timestamps
     */
    public Category createCategory(Category category) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if category with same name already exists for this user
        Category existingCategory = categoryRepository.findByNameAndUserId(category.getName(), user.getId());
        if (existingCategory != null) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        category.setUser(user);
        return categoryRepository.save(category);
    }

    /**
     * Updates an existing category by its ID.
     * The category must belong to the authenticated user to be updated.
     * 
     * @param id the unique identifier of the category to update
     * @param category the Category object containing updated information
     * @return the updated Category object
     * @throws RuntimeException if the category is not found or doesn't belong to the user
     */
    public Category updateCategory(Long id, Category category) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!existingCategory.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("User not authorized to update this category");
        }
        
        // Check if new name conflicts with existing category (excluding current one)
        if (!existingCategory.getName().equals(category.getName())) {
            Category nameConflict = categoryRepository.findByNameAndUserId(category.getName(), user.getId());
            if (nameConflict != null && !nameConflict.getId().equals(id)) {
                throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
            }
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setColor(category.getColor());
        existingCategory.setDescription(category.getDescription());
        
        return categoryRepository.save(existingCategory);
    }

    /**
     * Deletes a category by its ID.
     * The category must belong to the authenticated user to be deleted.
     * 
     * @param id the unique identifier of the category to delete
     * @throws RuntimeException if the category is not found or doesn't belong to the user
     */
    public void deleteCategory(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("User not authorized to delete this category");
        }
        
        categoryRepository.delete(category);
    }

    /**
     * Retrieves a category by its ID.
     * The category must belong to the authenticated user.
     * 
     * @param id the unique identifier of the category to retrieve
     * @return the Category object
     * @throws RuntimeException if the category is not found or doesn't belong to the user
     */
    public Category getCategoryById(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("User not authorized to access this category");
        }
        
        return category;
    }
}
