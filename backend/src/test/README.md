# Todo Application Backend - Test Suite

This directory contains comprehensive test cases for the Todo Application backend, covering all layers of the application architecture.

## Test Structure

The test suite is organized into the following categories:

### 1. Unit Tests

#### Controller Tests
- **`AuthControllerTest`**: Tests for authentication endpoints
  - User registration functionality
  - User login and JWT token generation
  - Error handling for invalid credentials
  - Input validation testing

- **`TodoControllerTest`**: Tests for todo management endpoints
  - Creating new todos
  - Retrieving user todos
  - Updating existing todos
  - Deleting todos
  - Authorization checks

#### Service Tests
- **`UserServiceTest`**: Tests for user service layer
  - User registration with password encoding
  - Error handling for invalid inputs
  - Database interaction validation

- **`TodoServiceTest`**: Tests for todo service layer
  - CRUD operations for todos
  - Security context validation
  - User authorization checks
  - Error handling for non-existent resources

- **`CustomUserDetailsServiceTest`**: Tests for authentication service
  - User details loading
  - Username not found scenarios
  - Error handling for database issues

#### Repository Tests
- **`UserRepositoryTest`**: Tests for user data access
  - Finding users by username and email
  - Unique constraint validation
  - Database operations (save, delete, find)

- **`TodoRepositoryTest`**: Tests for todo data access
  - Finding todos by user ID
  - CRUD operations
  - Data integrity checks

#### Utility Tests
- **`JwtUtilTest`**: Tests for JWT token management
  - Token generation and validation
  - Token expiration handling
  - Signature verification
  - Claims extraction

### 2. Integration Tests

#### Controller Integration Tests
- **`AuthControllerIntegrationTest`**: End-to-end authentication testing
  - Full registration and login flow
  - Database persistence verification
  - Duplicate user handling

- **`TodoControllerIntegrationTest`**: End-to-end todo management testing
  - Complete CRUD operations with database
  - User authorization in real scenarios
  - Cross-user access prevention

### 3. Application Tests
- **`TodoAppApplicationTests`**: Spring context and application startup tests
- **`AllTestsSuite`**: Documentation and test runner overview

## Test Configuration

### Test Properties
- **`application-test.properties`**: Test-specific configuration
  - H2 in-memory database for testing
  - JWT configuration for tests
  - Logging configuration for debugging

### Test Database
- Uses H2 in-memory database for isolation
- Database recreated for each test class
- No external dependencies required

## Running Tests

### All Tests
```bash
mvn test
```

### Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Specific test class
mvn test -Dtest="UserServiceTest"
```

### Test Reports
Test results and reports are generated in:
- `target/surefire-reports/` - Individual test results
- `target/site/jacoco/` - Code coverage reports (if configured)

## Test Coverage

The test suite provides comprehensive coverage across:

1. **Controller Layer**: All REST endpoints tested with various scenarios
2. **Service Layer**: Business logic validation and error handling
3. **Repository Layer**: Data access and database constraints
4. **Security Layer**: Authentication and authorization
5. **Utility Classes**: JWT token management and validation
6. **Integration**: End-to-end workflow testing

## Key Testing Features

- **Mocking**: Extensive use of Mockito for isolated unit testing
- **Spring Test Context**: Integration tests with full Spring context
- **Security Testing**: Authentication and authorization scenarios
- **Database Testing**: H2 in-memory database for fast, isolated tests
- **Error Handling**: Comprehensive error scenario testing
- **Edge Cases**: Boundary condition and null value testing

## Test Best Practices Implemented

1. **Isolation**: Each test is independent and doesn't affect others
2. **Clarity**: Descriptive test names and clear arrange-act-assert structure
3. **Coverage**: Tests cover both happy path and error scenarios
4. **Performance**: Fast execution with in-memory database
5. **Maintainability**: Well-organized test structure and configuration

## Dependencies

The test suite uses the following testing frameworks:
- JUnit 5 for test structure and assertions
- Mockito for mocking dependencies
- Spring Boot Test for integration testing
- H2 Database for in-memory testing
- Spring Security Test for authentication testing

## Notes

- Some deprecation warnings may appear for MockBean annotations in newer Spring Boot versions
- Tests are designed to be compatible with the current Spring Boot 3.5.3 version
- All sensitive test data uses test-specific values, not production secrets
