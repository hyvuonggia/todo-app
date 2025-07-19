# Todo App

This is a simple Todo application built with Angular for the frontend and Spring Boot for the backend. The application was developed with the assistance of the Gemini CLI.

## Features

*   User registration and login
*   Create, read, update, and delete Todos
*   JWT-based authentication

## Technologies Used

### Frontend:
* **Core Framework:**
  * Angular 20.1.0
  * TypeScript 5.8
  * RxJS 7.8

* **UI Components & Styling:**
  * Angular Material 20.1.0
  * SCSS for styling
  * Material Icons
  * Quill.js for rich text editing

* **Development Tools:**
  * Angular CLI 20.1.0
  * Node.js and npm
  * Karma and Jasmine for testing
  * ESLint for code quality

### Backend:
* **Core Framework:**
  * Spring Boot 3.5.3
  * Java 17
  * Maven for build management

* **Security:**
  * Spring Security
  * JWT (JSON Web Tokens) with JJWT 0.11.5
  * BCrypt for password hashing

* **Database & ORM:**
  * MySQL (Production)
  * H2 Database (Testing)
  * Spring Data JPA
  * Hibernate as JPA provider

* **Testing:**
  * JUnit 5
  * Mockito
  * Spring Boot Test
  * Spring Security Test

* **Development Tools:**
  * Spring Boot DevTools
  * Lombok for boilerplate reduction

### DevOps & Infrastructure:
* **Containerization:**
  * Docker
  * Docker Compose
  * Nginx (for frontend serving)

* **API Documentation:**
  * Comprehensive Javadoc
  * REST API documentation

* **Development Features:**
  * Hot reload for both frontend and backend
  * Environment-specific configurations
  * CORS configuration for local development

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