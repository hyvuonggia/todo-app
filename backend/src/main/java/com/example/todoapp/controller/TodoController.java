package com.example.todoapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todoapp.model.Todo;
import com.example.todoapp.service.TodoService;

/**
 * REST controller for managing Todo operations.
 * This controller handles HTTP requests related to Todo items including
 * creating, reading, updating, and deleting todos for the authenticated user.
 *
 * @author Todo App Team
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    /**
     * Retrieves all todos for the currently authenticated user.
     * This endpoint fetches todos specific to the user making the request,
     * ensuring data isolation between different users.
     *
     * @return List of Todo objects belonging to the authenticated user
     */
    @GetMapping
    public List<Todo> getTodos() {
        return todoService.getTodosForUser();
    }

    /**
     * Creates a new todo item for the authenticated user.
     * The todo will be automatically associated with the currently logged-in user.
     *
     * @param todo the Todo object to be created, containing title, description, and other details
     * @return the created Todo object with generated ID and timestamps
     */
    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        System.out.println("Received request to create todo: " + todo.getTitle()); // Add this line
        return todoService.createTodo(todo);
    }

    /**
     * Updates an existing todo item by its ID.
     * The todo must belong to the authenticated user to be updated.
     *
     * @param id   the unique identifier of the todo to update
     * @param todo the Todo object containing updated information
     * @return the updated Todo object with modified fields
     * @throws RuntimeException if the todo is not found or doesn't belong to the user
     */
    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        return todoService.updateTodo(id, todo);
    }

    /**
     * Deletes a todo item by its ID.
     * The todo must belong to the authenticated user to be deleted.
     *
     * @param id the unique identifier of the todo to delete
     * @return ResponseEntity with OK status if deletion is successful
     * @throws RuntimeException if the todo is not found or doesn't belong to the user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.ok().build();
    }
}