# WonWire 💸

A full-stack fintech money transfer platform built as a portfolio project.
Users can register, fund a wallet, and send money to other users in real time.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Running with Docker](#running-with-docker)
- [Environment Variables](#environment-variables)
- [Running Tests](#running-tests)
- [Security](#security)

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT (HS256) |
| Database | PostgreSQL 16 (Docker image `postgres:16`) |
| Cache / Rate limiting | Redis 7 (Docker image `redis:7`) |
| ORM | Spring Data JPA / Hibernate |
| Email | Mailtrap (SMTP sandbox) |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Build tool | Maven |
| Tests | JUnit 5, Mockito, MockMvc, H2 (in-memory), embedded Redis |

### Frontend
| Layer | Technology |
|---|---|
| Language | JavaScript (ES Modules) |
| Framework | React 19 + Vite 8 |
| Routing | React Router v7 |
| HTTP client | Axios |
| Icons | Lucide React |
| Package manager | npm |

### Infrastructure
| | |
|---|---|
| Containerization | Docker + Docker Compose |
| Reverse proxy | Nginx (inside frontend container) |

---

## Features

- **Auth** — register, login, logout (JWT blacklist via Redis)
- **Password reset** — forgot password flow via email token (Mailtrap sandbox)
- **Wallet** — one wallet per user, auto-created on registration (KRW)
- **Deposit** — fund your own wallet
- **Transfer** — send money to another user by email, with idempotency key to prevent duplicate transactions
- **History** — paginated transaction history (sent + received)
- **Profile** — view and update profile, change password
- **Rate limiting** — Redis-backed per-IP rate limiting on auth endpoints
- **Swagger UI** — interactive API docs at `/swagger-ui.html`

---

## Architecture

```
Browser
  │
  ▼
┌──────────────────────────────────┐
│  Frontend container (Nginx :80)  │
│  React SPA — static files        │
│  /api/* → proxy → backend:8080   │
└──────────────────────────────────┘
  │ /api/*
  ▼
┌──────────────────────────────────┐
│  Backend container (Spring :8080)│
│                                  │
│  RateLimitFilter (Redis)         │
│  → JwtAuthenticationFilter       │
│  → Spring Security               │
│  → Controllers → Services        │
│  → JPA → PostgreSQL              │
└──────────────────────────────────┘
  │               │
  ▼               ▼
PostgreSQL      Redis
(data)          (JWT blacklist + rate limit counters)
```

---

## Prerequisites

| Tool | Minimum version | Check |
|---|---|---|
| Docker | 24+ | `docker -v` |
| Docker Compose | v2 (plugin) | `docker compose version` |

> **Note on Java version:** The Dockerfile uses `eclipse-temurin:25`. If that image is unavailable on your machine, change the `FROM` line in `backend/Dockerfile` to `eclipse-temurin:21-jdk` and update `<java.version>` to `21` in `pom.xml`.

---

## Running with Docker

This builds and runs all 4 services (PostgreSQL, Redis, backend, frontend) with a single command. No local Java, Node, or database installation required.

### 1. Clone the repo

```bash
git clone https://github.com/tommydll/wonwire.git
cd wonwire
```

### 2. Configure the backend environment

```bash
cp backend/.env.example backend/.env.docker
```

Open `backend/.env.docker` and fill in all values:

```env
DB_URL=jdbc:postgresql://postgres:5432/wonwire
DB_USERNAME=wonwire
DB_PASSWORD=wonwire

# Generate a strong secret: openssl rand -hex 64
JWT_SECRET=your-secret-key-at-least-256-bits-long
JWT_EXPIRATION=86400000

REDIS_HOST=redis
REDIS_PORT=6379

# Sign up at mailtrap.io and get your SMTP credentials (free tier works)
MAIL_USERNAME=your-mailtrap-username
MAIL_PASSWORD=your-mailtrap-password
```

### 3. Build and start everything

```bash
# From the repo root (where compose.yaml lives)
docker compose up --build
```

Once all containers are healthy:
- **Frontend:** `http://localhost`
- **Backend API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

### Useful commands

```bash
# Run in background
docker compose up --build -d

# Follow logs for a specific service
docker compose logs -f backend

# Stop everything
docker compose down

# Stop and delete database volume (full reset)
docker compose down -v

# Rebuild only the backend after a code change
docker compose up --build backend
```

---

## Environment Variables

Docker Compose reads `backend/.env.docker` via the `env_file:` property in `compose.yaml`. This file is gitignored, you must create it from `.env.example` as shown above.

### Backend (`backend/.env.docker`)

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection string | `jdbc:postgresql://postgres:5432/wonwire` |
| `DB_USERNAME` | PostgreSQL username | `wonwire` |
| `DB_PASSWORD` | PostgreSQL password | `wonwire` |
| `JWT_SECRET` | HMAC-SHA256 signing key (min 256 bits) | `openssl rand -hex 64` |
| `JWT_EXPIRATION` | Token lifetime in milliseconds | `86400000` (24 hours) |
| `REDIS_HOST` | Redis hostname | `redis` (Docker service name) |
| `REDIS_PORT` | Redis port | `6379` |
| `MAIL_USERNAME` | Mailtrap SMTP username | from mailtrap.io dashboard |
| `MAIL_PASSWORD` | Mailtrap SMTP password | from mailtrap.io dashboard |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins | http://localhost:5173 |

> **Postman collection:** A collection for testing the API manually is available at `postman/WonWireAPI.postman_collection.json`.

---

## Running Tests

Tests use H2 (in-memory database) and embedded Redis. No external dependencies needed.

```bash
cd backend

# Run all tests
./mvnw test

# Run only a specific test class
./mvnw test -Dtest=AuthControllerTest

# Run only unit tests (service layer)
./mvnw test -Dtest="*ServiceTest"

# Run only integration tests (controller layer)
./mvnw test -Dtest="*ControllerTest"
```

---

## Security

- **Password hashing** — BCrypt (Spring Security default)
- **JWT** — HS256 signed, 24h expiration, `jti` (UUID) claim for precise blacklisting on logout
- **JWT blacklist** — stored in Redis by `jti`, not full token string
- **Rate limiting** — per-IP, per-endpoint sliding window via Redis on auth routes
- **SQL injection prevention** — JPA/Hibernate with prepared statements only
- **XSS prevention** — React escapes all JSX values by default
- **Idempotency keys** — transfers include a UUID idempotency key to prevent duplicate transactions
- **Pessimistic locking** — wallet rows are locked during transfers to prevent race conditions
- **Stateless sessions** — no server-side sessions, JWT only
- **CSRF disabled** — JWT is sent in the `Authorization` header, not cookies, so CSRF is not applicable