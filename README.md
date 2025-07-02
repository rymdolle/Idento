# Idento

A Spring Boot application for managing user identity and authentication.

## Overview

Idento is a user management and authentication service built with Spring Boot and Kotlin. It provides role-based access control and configurable user permissions.

The API supports the following endpoints:

- `POST /api/v1/auth/login`
  - Basic authentication to get a JWT token
  - Return:
    - `200 OK` with JWT
      - `{"token": "<jwt>"}`
    - `401 Unauthorized` if credentials are invalid
- `GET /api/v1/auth/verify`
  - Verify JWT token sent in the header `Authorization: Bearer <jwt>`
  - Return:
    - `200 OK` if token is valid
    - `401 Unauthorized` if token is invalid or expired
- `GET /.well-known/jwks.json`
  - Retrieve JSON Web Key Set (JWKS) for public keys
- `GET /api/v1/auth/key/{id}`
  - Retrieve public key by ID
  - Return:
    - `200 OK` with public key in JWK format
    - `404 Not Found` if key does not exist

Tokens are by default set to expire after 30 minutes. To change the token expiration, set `app.security.token.ttl` in the properties file.

The application supports both in-memory and persistent storage for user data. To change from default h2 set `spring.datasource` in the properties file.

## Prerequisites

- JDK 21 or higher
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

By default, the application runs on port 8080.

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
2. Select Spring Boot application
3. Add `dev` to the list of active profiles
4. Click Apply and OK

### Private Configuration

A private configuration is for sensitive data like credentials and keys:

1. Create `application-private.yml` in `src/main/resources/`
2. Add sensitive configuration to this file
3. Git will ignore this file to keep it private

Example `application-private.yml`:
```yaml
app:
  security:
    users:
      - username: admin
        password: secure-password
        roles:
          - ADMIN
        authorities:
          - READ
          - WRITE
          - DELETE
  jwt:
    keys:
      - kid: key-id
        kty: EC
        crv: P-256
        d: caNy0...
        x: 9aykLQn...
        y: AqpPj...
      - kid: another-key-id
        file: path/to/private/key.pem
```

If no key is provided, the application will generate a new key on startup.

A key can be generated from generateECKey() in [IdentoApplicationTests](src/test/kotlin/com/rymdis/idento/IdentoApplicationTests.kt)

## H2 Database

In dev profile, H2 console can be accessed at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:idento`
- Username: `sa`

## HTTP Client

The project includes HTTP client configurations for testing the API:
- dev environment: http://localhost:8080
