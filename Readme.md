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

## Project Structure

```
src/main/java/com/dxc/iotmonitor/
├── auth/
│   ├── controller/
│   ├── dto/
│   └── service/
├── config/
│   ├── CorsConfig.java
│   └── SecurityConfig.java
├── exception/
│   └── GlobalExceptionHandler.java
├── security/
├── user/
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/
└── IotmonitorApplication.java
```

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, sprint-reviewed code only |
| `develop` | Integration branch — all features merge here first |
| `feature/auth-signup` | User Story 1 |
| `feature/auth-signin` | User Story 2 |
| `feature/user-profile` | User Story 3 |

Feature branches are opened off `develop` and merged back via Pull Request after unit tests pass and the backend tester signs off. `develop` is merged into `main` at end of sprint only.

## Sprint 1 Scope

| User Story | Description |
|---|---|
| User Story 1 — Sign Up | User provides email, first name, last name, profile picture, and password. All fields mandatory. New user record created in DB on success. |
| User Story 2 — Sign In | User provides email and password. Returns JWT on success. Appropriate error returned on invalid credentials. |
| User Story 3 — Profile Page | Authenticated user can view and update their profile. Supports changing profile picture and password. |
