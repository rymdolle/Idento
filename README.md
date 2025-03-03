# Idento

A Spring Boot application for managing user identity and authentication.

## Overview

Idento is a user management and authentication service built with Spring Boot and Kotlin. It provides role-based access control and configurable user permissions.

## Prerequisites

- JDK 11 or higher
- Gradle

## Getting Started

### Clone the repository

```bash
git clone https://github.com/rymdolle/Idento.git
cd Idento
```

### Build the project

```bash
gradle build
```

### Run the application

```bash
gradle bootRun
```

By default, the application runs on port 8000.

## Configuration

### Profiles

Idento supports different configuration profiles:

- **default**: Uses in-memory H2 database
- **dev**: Enables H2 console and debug logging (port 8080)

To specify a profile when running:

```bash
gradle bootRun --args='--spring.profiles.active=dev'
```

### IntelliJ IDEA Configuration

To set the active profile in IntelliJ:

1. Go to `Run > Edit Configurations...`
2. Select your Spring Boot application
3. Add `-Dspring.profiles.active=dev` in VM options
4. Click Apply and OK

### Private Configuration

For sensitive data like passwords and credentials:

1. Create `application-private.yml` in `src/main/resources/`
2. Add your sensitive configuration to this file
3. The file is automatically ignored by Git

Example `application-private.yml`:
```yaml
app:
  security:
    users:
      - username: admin
        password: your-secure-password
        roles:
          - ADMIN
  jwt:
    keys:
      - kid: your-key-id
        kty: EC
        crv: P-256
        d: caNy0...
        x: 9aykLQn...
        y: AqpPj...
```

## H2 Database

In development mode, you can access the H2 console at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:idento`
- Username: `sa`

## Default Users

The development configuration includes these users:

1. **Admin**
    - Username: `admin`
    - Password: `nimda`
    - Roles: ADMIN
    - Authorities: READ, WRITE, DELETE

2. **User**
    - Username: `user`
    - Password: `resu`
    - Roles: USER
    - Authorities: READ, WRITE

## HTTP Client

The project includes HTTP client configurations for testing the API:
- Local environment: http://localhost:8080
- Dev environment: https://idento.rymdis.com
