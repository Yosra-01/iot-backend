# IoT Monitor — Backend

## Project Setup

| Field | Value |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build Tool | Maven |
| Packaging | Jar |
| Group | com.dxc |
| Artifact | iotmonitor |
| Base Package | com.dxc.iotmonitor |

## Dependencies

| Dependency | Purpose |
|---|---|
| Spring Web | REST API layer |
| Spring Data JPA | Database ORM |
| MySQL Driver | MySQL connectivity |
| H2 Database | In-memory DB for testing |
| Spring Security | Authentication and access control |
| Lombok | Boilerplate reduction |
| Validation | Request field validation |
| Spring Boot DevTools | Live reload during development |
| jjwt-api / jjwt-impl / jjwt-jackson 0.12.6 | JWT token generation and validation |
| Bucket4j 8.10.1 | Token bucket rate limiting |

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, sprint-reviewed code only |
| `dev` | Integration branch — all features merge here first |
| `feature/auth` | User Story 1 & 2 |
| `feature/user-profile` | User Story 3 |

Feature branches are opened off `dev` and merged back via Pull Request after unit tests pass and the backend tester signs off. `dev` is merged into `main` at end of sprint only.

## Prerequisites

Before running the project, make sure you have the following installed:

| Tool | Version |
|---|---|
| Java JDK | 21 |
| Apache Maven | 3.9.11 |
| MySQL Server | 8.0.40 |
| Git | Any recent version |

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Yosra-01/iot-backend.git
cd iot-backend
```

### 2. Create the database

Open your MySQL client and run:

```sql
CREATE DATABASE iot_db;
```

### 3. Configure the application

Copy the example properties file and fill in your values:

```bash
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
```

Open `application-dev.properties` and update the following fields:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/iot_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

jwt.secret=your_jwt_secret_minimum_32_characters
jwt.expiration=86400000
```

> **Note:** `jwt.secret` must be at least 32 characters long. You can generate one at [generate-secret.vercel.app/32](https://generate-secret.vercel.app/32) and copy the output.

### 4. Build the project

```bash
mvn clean install
```

### 5. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### 6. Verify the application is running

You should see the following in the console:
```
Started IotmonitorApplication in X seconds
```

## Running Tests

```bash
mvn test
```

### Test Coverage

| Test Class | Tests | Coverage |
|---|---|---|
| `AuthServiceTest` | 5 | Auth service — register and login logic |
| `RateLimitServiceTest` | 6 | Rate limiting — bucket behavior and thresholds |
| `UserServiceTest` | 6 | User service — profile, password, and delete logic |

All unit tests should pass. No Spring context is loaded — tests run fast and in isolation.

## Security

### Authentication

All protected endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Tokens are issued on registration and login. Expired, missing, or tampered tokens return `401 Unauthorized`.

### Rate Limiting

Applied to auth endpoints at the **filter level** — before request validation runs. Keyed by client IP address.

| Endpoint | Limit |
|---|---|
| `POST /api/auth/register` | 10 requests per minute per IP |
| `POST /api/auth/login` | 10 requests per minute per IP |

Applied to profile endpoints at the **controller level** — after authentication. Keyed by authenticated user email, shared across all three profile endpoints.

| Endpoints                                                                                   | Limit |
|---------------------------------------------------------------------------------------------|---|
| `GET /api/user/profile`<br/>`PATCH /api/user/profile/picture`<br/>`PATCH /api/user/profile/password` | 10 requests per minute per user |

Exceeding the limit on any endpoint returns `429 Too Many Requests`.

### Passwords

Stored using BCrypt hashing. Plain text passwords are never persisted.

## Testing Utilities

### `DELETE /api/user/delete`

This endpoint is intentionally unauthenticated. It exists solely to allow the backend tester to clean up users created during test runs without needing a JWT token. It is declared in `permitAll()` in `SecurityConfig` and is not part of the application feature set. It would be removed or secured before any production deployment.

## Sprint 1 Scope

| User Story | Description |
|---|---|
| User Story 1 — Sign Up | User provides email, first name, last name, profile picture, and password. All fields mandatory. New user record created in DB on success. |
| User Story 2 — Sign In | User provides email and password. Returns JWT on success. Appropriate error returned on invalid credentials. |
| User Story 3 — Profile Page | Authenticated user can view and update their profile. Supports changing profile picture and password. |