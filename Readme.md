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


## Branch Strategy

| Branch | Purpose                                            |
|---|----------------------------------------------------|
| `main` | Stable, sprint-reviewed code only                  |
| `dev` | Integration branch — all features merge here first |
| `feature/auth` | User Story 1 & 2                                   |
| `feature/user-profile` | User Story 3                                       |

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

> **Note:** `jwt.secret` must be at least 32 characters long. You can generate one:
> **Go to [generate-secret.vercel.app/32](https://generate-secret.vercel.app/32) and copy the output.**

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
Started IotmonitorApplication in X seconds

## Running Tests

```bash
mvn test
```

All 6 unit tests in `AuthServiceTest` should pass.

## Sprint 1 Scope

| User Story | Description |
|---|---|
| User Story 1 — Sign Up | User provides email, first name, last name, profile picture, and password. All fields mandatory. New user record created in DB on success. |
| User Story 2 — Sign In | User provides email and password. Returns JWT on success. Appropriate error returned on invalid credentials. |
| User Story 3 — Profile Page | Authenticated user can view and update their profile. Supports changing profile picture and password. |

