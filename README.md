# Todo App

This is a simple Todo application built with Angular for the frontend and Spring Boot for the backend. The application was developed with the assistance of the Gemini CLI.

## Features

*   User registration and login
*   Create, read, update, and delete Todos
*   JWT-based authentication

## Technologies Used

*   **Frontend:**
    *   Angular
    *   Angular Material
    *   TypeScript
*   **Backend:**
    *   Spring Boot
    *   Spring Security
    *   JPA (Hibernate)
    *   H2 Database
    *   Maven

## Getting Started

### Prerequisites

*   Java 21 or later
*   Node.js and npm

### Installation and Running

1.  **Clone the repository:**

    ```bash
    git clone <repository-url>
    cd todo-app
    ```

2.  **Run the backend:**

    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```

    The backend will be running on `http://localhost:8080`.

3.  **Run the frontend:**

    In a new terminal, navigate to the `frontend` directory:

    ```bash
    cd frontend
    npm install
    npm start
    ```

    The frontend will be running on `http://localhost:4200`.

## API Endpoints

*   `POST /api/auth/register`: Register a new user.
*   `POST /api/auth/login`: Authenticate a user and receive a JWT token.
*   `GET /api/todos`: Get all Todos for the authenticated user.
*   `POST /api/todos`: Create a new Todo.
*   `PUT /api/todos/{id}`: Update an existing Todo.
*   `DELETE /api/todos/{id}`: Delete a Todo.

---

*This project was developed with the assistance of the Gemini CLI.*