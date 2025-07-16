package com.example.todoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Todo Application.
 * This class serves as the entry point for the Spring Boot application.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class TodoAppApplication {

	/**
	 * Main method to start the Spring Boot application.
	 * 
	 * @param args command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(TodoAppApplication.class, args);
	}

}
