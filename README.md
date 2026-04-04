# master-data-service

Production-grade Spring Boot microservice implementing user registration, authentication, and master data management with JWT security, BCrypt password hashing, and PostgreSQL schema isolation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Temurin LTS) |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt 0.12.3) |
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
JwtAuthFilter          ← validates JWT on every request
    │
    ▼
SecurityFilterChain    ← public vs protected route rules
    │
    ▼
Controller Layer       ← accepts Request DTOs, returns Response DTOs
    │
    ▼
Service Layer          ← business logic, BCrypt, JWT generation
    │
    ▼
Repository Layer       ← JpaRepository, auto-generated SQL
    │
    ▼
Entity Layer           ← JPA entities, DB table mapping
    │
    ▼
PostgreSQL             ← devdb / masterdata schema
```

### Key Design Decisions

- **DTO separation** — Request and Response DTOs are separate from entities. Entities never exposed directly through the API.
- **Schema isolation** — All tables live in the `masterdata` schema inside a shared `devdb` database, keeping projects isolated without separate DB instances.
- **Stateless JWT** — No server-side sessions. Every request carries a signed JWT. Spring Security validates the signature on each request.
- **BCrypt hashing** — Passwords never stored in plain text. BCrypt with salt ensures same password hashes differently every time.
- **Separate credentials table** — `user_credentials` is separate from `users`. Security data is isolated from identity data.
- **Soft-deletable account status** — Users move through `PENDING_VERIFICATION → ACTIVE → INACTIVE/LOCKED/DELETED` states. Hard deletes never used.

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
│   ├── AuthProvider.java             ← enum
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
│   └── SecurityConfig.java
└── service/
    ├── UserService.java
    └── JwtService.java
```

---

## API Endpoints

### Auth (Public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login, returns JWT |

### Master Data (Protected — requires JWT)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/master-data/me` | Get current user profile |
| GET | `/api/v1/master-data/users/{userId}` | Get user by ID |

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
├── auth_provider       VARCHAR (LOCAL | GOOGLE | GITHUB)
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
- **Stateless sessions** — `SessionCreationPolicy.STATELESS`. No server-side session storage.
- **CSRF disabled** — Safe for stateless JWT REST APIs.

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

### 3. Set Environment Variable

```bash
export JWT_SECRET=<your-base64-secret>

# Generate a secret
openssl rand -base64 32
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

---

## Error Responses

All errors return a consistent structure:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "timestamp": "2026-04-04T08:00:00"
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

## Author

Rishi — Senior Backend Engineer  
Austin / Round Rock, TX  
[github.com/cloudrishi](https://github.com/cloudrishi)
