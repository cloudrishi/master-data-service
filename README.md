# master-data-service

Production-grade Spring Boot microservice implementing user registration, authentication, and master data management with JWT security, BCrypt password hashing, GitHub OAuth2 social login, and PostgreSQL schema isolation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Temurin LTS) |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| OAuth2 | GitHub OAuth2 with custom token response client |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16 |
| Connection Pool | HikariCP |
| Build Tool | Maven |
| Boilerplate | Lombok |
| Validation | Jakarta Bean Validation |
| Container | Docker |

---

## Architecture

```
Client (HTTP)
    │
    ▼
JwtAuthFilter              ← validates JWT on every request
    │
    ▼
SecurityFilterChain        ← public vs protected route rules
    │
    ▼
Controller Layer           ← accepts Request DTOs, returns Response DTOs
    │
    ▼
Service Layer              ← business logic, BCrypt, JWT generation
    │
    ▼
Repository Layer           ← JpaRepository, auto-generated SQL
    │
    ▼
Entity Layer               ← JPA entities, DB table mapping
    │
    ▼
PostgreSQL                 ← devdb / masterdata schema
```

### Key Design Decisions

- **DTO separation** — Request and Response DTOs are separate from entities. Entities never exposed directly through the API.
- **Schema isolation** — All tables live in the `masterdata` schema inside a shared `devdb` database, keeping projects isolated without separate DB instances.
- **Stateless JWT** — No server-side sessions. Every request carries a signed JWT. Spring Security validates the signature on each request.
- **BCrypt hashing** — Passwords never stored in plain text. BCrypt with salt ensures same password hashes differently every time.
- **Separate credentials table** — `user_credentials` is separate from `users`. Security data is isolated from identity data.
- **Soft-deletable account status** — Users move through `PENDING_VERIFICATION → ACTIVE → INACTIVE/LOCKED/DELETED` states. Hard deletes never used.
- **Custom OAuth2 token client** — GitHub's OAuth2 implementation predates RFC 6749. A custom token response client handles GitHub's non-standard token exchange format.

---

## Project Structure

```
src/main/java/com/rish/masterdata/
├── controller/
│   ├── AuthController.java           ← /api/v1/auth/**
│   └── MasterDataController.java     ← /api/v1/master-data/**
├── dto/
│   ├── RegistrationRequest.java
│   ├── LoginRequest.java
│   ├── AddressRequest.java
│   ├── AuthResponse.java
│   ├── UserResponse.java
│   ├── AddressResponse.java
│   └── ErrorResponse.java
├── entity/
│   ├── User.java
│   ├── Address.java
│   ├── UserCredentials.java
│   ├── AccountStatus.java            ← enum
│   ├── AddressType.java              ← enum
│   ├── AuthProvider.java             ← enum (LOCAL, GITHUB, GOOGLE)
│   └── Role.java                     ← enum
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── DuplicateEmailException.java
│   ├── UserNotFoundException.java
│   ├── InvalidCredentialsException.java
│   └── AccountStatusException.java
├── repository/
│   ├── UserRepository.java
│   ├── AddressRepository.java
│   └── UserCredentialsRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   ├── SecurityConfig.java
│   ├── OAuth2SuccessHandler.java
│   ├── GitHubOAuth2TokenResponseClient.java
│   └── HttpCookieOAuth2AuthorizationRequestRepository.java
└── service/
    ├── UserService.java
    └── JwtService.java
```

---

## API Endpoints

### Auth (Public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register new user with email/password |
| POST | `/api/v1/auth/login` | Login, returns JWT |
| GET | `/oauth2/authorization/github` | Initiate GitHub OAuth2 login |
| GET | `/api/v1/auth/oauth2/success` | OAuth2 callback — returns JWT |

### Master Data (Protected — requires JWT)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/master-data/me` | Get current user profile |
| GET | `/api/v1/master-data/users/{userId}` | Get user by ID |

---

## Authentication Flows

### Flow 1 — Email / Password

```
POST /api/v1/auth/register  ← email, password, profile
        │
        ▼
BCrypt hashes password
User saved to DB
JWT generated
        │
        ▼
AuthResponse → token + userId + email + role
```

### Flow 2 — GitHub OAuth2

```
GET /oauth2/authorization/github
        │
        ▼
Redirected to GitHub login page
        │
        ▼
User authorizes app
        │
        ▼
GitHub redirects to callback URL
Custom token client exchanges code for token
GitHub profile fetched
User created/found in DB
JWT generated
        │
        ▼
Redirected to /api/v1/auth/oauth2/success?token=...
AuthResponse → token + userId + email + role
```

---

## Request / Response Examples

### Register

```json
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "Rishi",
  "lastName": "P",
  "email": "rishi@example.com",
  "password": "Test@1234",
  "timezone": "America/Chicago",
  "preferredLanguage": "en",
  "addresses": [
    {
      "addressType": "HOME",
      "street": "123 Main St",
      "city": "Round Rock",
      "state": "TX",
      "zip": "78664",
      "country": "USA",
      "isDefault": true
    }
  ]
}
```

Response `201 Created`:
```json
{
  "token": "eyJhbGci...",
  "userId": "uuid-123",
  "email": "rishi@example.com",
  "firstName": "Rishi",
  "lastName": "P",
  "role": "USER"
}
```

### Login

```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "rishi@example.com",
  "password": "Test@1234"
}
```

### Protected Request

```
GET /api/v1/master-data/me
Authorization: Bearer eyJhbGci...
```

---

## Database Schema

```
masterdata.users
├── id                  UUID (PK)
├── email               VARCHAR UNIQUE NOT NULL
├── first_name          VARCHAR NOT NULL
├── last_name           VARCHAR NOT NULL
├── timezone            VARCHAR
├── preferred_language  VARCHAR
├── account_status      VARCHAR (PENDING_VERIFICATION | ACTIVE | INACTIVE | LOCKED | SUSPENDED | DELETED)
├── role                VARCHAR (USER | ADMIN)
├── created_at          TIMESTAMP NOT NULL
└── updated_at          TIMESTAMP NOT NULL

masterdata.addresses
├── id                  UUID (PK)
├── user_id             UUID (FK → users.id)
├── address_type        VARCHAR (HOME | WORK | SCHOOL | BILLING | SHIPPING | OTHER)
├── street              VARCHAR NOT NULL
├── street2             VARCHAR
├── city                VARCHAR NOT NULL
├── state               VARCHAR NOT NULL
├── zip                 VARCHAR NOT NULL
├── country             VARCHAR NOT NULL
├── is_default          BOOLEAN
├── created_at          TIMESTAMP NOT NULL
└── updated_at          TIMESTAMP NOT NULL

masterdata.user_credentials
├── id                  UUID (PK)
├── user_id             UUID (FK → users.id) UNIQUE
├── hashed_password     VARCHAR NOT NULL
├── auth_provider       VARCHAR (LOCAL | GITHUB | GOOGLE)
├── provider_user_id    VARCHAR
├── failed_login_attempts INTEGER
├── last_login_at       TIMESTAMP
├── password_changed_at TIMESTAMP
├── locked_at           TIMESTAMP
├── created_at          TIMESTAMP NOT NULL
└── updated_at          TIMESTAMP NOT NULL
```

---

## Security

- **JWT** — Signed with HMAC SHA256. Expires in 24 hours. Carries userId, email, and role.
- **BCrypt** — All passwords hashed with BCrypt before storage. Plain text never persisted.
- **Ambiguous errors** — Login returns the same error for wrong email or wrong password. Prevents user enumeration.
- **Account status enforcement** — Users in `PENDING_VERIFICATION`, `LOCKED`, or `INACTIVE` state cannot login.
- **Stateless sessions** — `SessionCreationPolicy.STATELESS` for JWT flow. OAuth2 uses `IF_REQUIRED` for state management during the authorization code exchange.
- **CSRF disabled** — Safe for stateless JWT REST APIs.
- **Custom OAuth2 token client** — GitHub's OAuth2 implementation predates RFC 6749. The default Spring Security token converter fails with GitHub's response format. A custom `GitHubOAuth2TokenResponseClient` handles the token exchange directly.
- **Cookie-based OAuth2 state** — `HttpCookieOAuth2AuthorizationRequestRepository` stores OAuth2 state in HttpOnly cookies instead of session, compatible with stateless architecture.

---

## GitHub OAuth2 Setup

1. Go to `github.com/settings/developers`
2. Click **OAuth Apps → New OAuth App**
3. Fill in:

```
Application name           : master-data-service
Homepage URL               : http://localhost:8080
Authorization callback URL : http://localhost:8080/login/oauth2/code/github
```

4. Click **Register Application**
5. Copy **Client ID**
6. Click **Generate a new client secret** → copy immediately
7. Add both to IntelliJ environment variables

---

## Setup & Run

### Prerequisites

- Java 21 (Temurin)
- Docker Desktop
- Maven

### 1. Start PostgreSQL

From your central Docker Compose file:

```bash
cd ~/working
docker-compose up -d
```

### 2. Create Schema And User

```bash
docker exec -it postgres-dev psql -U postgres -d devdb -c "CREATE SCHEMA masterdata;"
docker exec -it postgres-dev psql -U postgres -d devdb -c "
  CREATE USER masterdata_user WITH PASSWORD 'masterdata123';
  GRANT ALL PRIVILEGES ON SCHEMA masterdata TO masterdata_user;
  ALTER USER masterdata_user SET search_path TO masterdata;
"
```

### 3. Set Environment Variables

```bash
# Generate JWT secret
openssl rand -base64 32
```

In IntelliJ Run Configuration → Environment Variables:

```
JWT_SECRET           = <generated secret>
GITHUB_CLIENT_ID     = <from GitHub OAuth App>
GITHUB_CLIENT_SECRET = <from GitHub OAuth App>
```

### 4. Run

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8080`

---

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `JWT_SECRET` | Base64 encoded HMAC SHA256 signing key (min 32 bytes) | Yes |
| `GITHUB_CLIENT_ID` | GitHub OAuth App Client ID | Yes |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth App Client Secret | Yes |

---

## Error Responses

All errors return a consistent structure:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "timestamp": "2026-04-05T08:00:00"
}
```

| Status | Scenario |
|---|---|
| 400 | Validation failure (@Valid) |
| 401 | Invalid credentials |
| 403 | Account locked / inactive |
| 404 | User not found |
| 409 | Duplicate email |
| 500 | Unexpected server error |

---

## Technical Notes

### Why A Custom GitHub OAuth2 Token Client?

GitHub's OAuth2 implementation was built in 2010 — two years before RFC 6749 was finalized. Their token endpoint response format has historically been inconsistent with the standard that Spring Security expects. Rather than fighting framework internals, a custom `GitHubOAuth2TokenResponseClient` calls GitHub's token endpoint directly with the correct headers and parses the response explicitly. This is a known gap between GitHub's implementation and the OAuth2 spec that neither GitHub nor Spring Security has fully resolved in over 14 years.

---

## Author

Rishi — Senior Backend Engineer
Austin / Round Rock, TX
[github.com/cloudrishi](https://github.com/cloudrishi)
