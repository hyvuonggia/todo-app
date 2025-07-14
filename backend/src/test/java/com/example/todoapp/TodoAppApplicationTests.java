package com.example.todoapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TodoAppApplicationTests {

	@Test
	void contextLoads() {
		// This test ensures that the Spring application context loads successfully
		// It's a basic smoke test to verify the application configuration is correct
	}

	@Test
	void mainApplicationStarts() {
		// This test verifies the main application can start without errors
		// If there are any configuration issues, this test will fail
	}
}
