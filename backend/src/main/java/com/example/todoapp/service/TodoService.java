package com.example.todoapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.todoapp.model.Category;
import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.CategoryRepository;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;

/**
 * Service class for todo-related business logic.
 * Handles CRUD operations for todo items with user authorization checks.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Retrieves all todo items for the currently authenticated user.
     * 
     * @return a list of todo items belonging to the current user
     * @throws RuntimeException if the current user is not found
     */
    public List<Todo> getTodosForUser() {
        User user = getCurrentUser();
        return todoRepository.findByUserId(user.getId());
    }

    /**
     * Retrieves todo items filtered by category for the currently authenticated user.
     * 
     * @param categoryId the ID of the category to filter by, or null for uncategorized todos
     * @return a list of todo items belonging to the current user and category
     * @throws RuntimeException if the current user is not found
     */
    public List<Todo> getTodosByCategory(Long categoryId) {
        User user = getCurrentUser();
        if (categoryId == null) {
            return todoRepository.findByUserIdAndCategoryIsNull(user.getId());
        } else {
            return todoRepository.findByUserIdAndCategoryId(user.getId(), categoryId);
        }
    }

    /**
     * Creates a new todo item for the currently authenticated user.
     * 
     * @param todo the todo item to create
     * @return the saved todo item with generated ID and user association
     * @throws RuntimeException if the current user is not found
     */
    public Todo createTodo(Todo todo) {
        User user = getCurrentUser();
        todo.setUser(user);
        
        // Handle category assignment
        if (todo.getCategory() != null && todo.getCategory().getId() != null) {
            Category category = categoryRepository.findById(todo.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("You are not authorized to use this category");
            }
            todo.setCategory(category);
        }
        
        Todo savedTodo = todoRepository.save(todo);
        System.out.println("Saved Todo: " + savedTodo); // Add this line
        return savedTodo;
    }

    /**
     * Updates an existing todo item.
     * Only the owner of the todo item can update it.
     * 
     * @param id the ID of the todo item to update
     * @param todoDetails the updated todo information
     * @return the updated todo item
     * @throws RuntimeException if todo is not found, user is not found, or user is not authorized
     */
    public Todo updateTodo(Long id, Todo todoDetails) {
        User user = getCurrentUser();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this todo");
        }
        todo.setTitle(todoDetails.getTitle());
        todo.setDescription(todoDetails.getDescription());
        todo.setCompleted(todoDetails.isCompleted());
        
        // Handle category assignment
        if (todoDetails.getCategory() != null) {
            Category category = categoryRepository.findById(todoDetails.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("You are not authorized to use this category");
            }
            todo.setCategory(category);
        } else {
            todo.setCategory(null);
        }
        
        return todoRepository.save(todo);
    }

    /**
     * Deletes a todo item.
     * Only the owner of the todo item can delete it.
     * 
     * @param id the ID of the todo item to delete
     * @throws RuntimeException if todo is not found, user is not found, or user is not authorized
     */
    public void deleteTodo(Long id) {
        User user = getCurrentUser();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this todo");
        }
        todoRepository.delete(todo);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * 
     * @return the current authenticated user
     * @throws RuntimeException if the current user is not found in the database
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}