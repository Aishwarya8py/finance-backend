# Finance Backend — Spring Boot

A production-structured REST API for a finance dashboard system built with
Spring Boot 3, Spring Security 6, JPA/Hibernate, and H2 (file-based SQLite-like database).

---

## Tech Stack

| Layer            | Technology                                  |
|------------------|---------------------------------------------|
| Language         | Java 21                                     |
| Framework        | Spring Boot 3.2                             |
| Web              | Spring MVC (REST Controllers)               |
| Security         | Spring Security 6 + JWT (jjwt 0.12)         |
| ORM              | Spring Data JPA + Hibernate 6               |
| Database         | H2 file-based (`data/financedb`)            |
| Validation       | Jakarta Bean Validation                     |
| Boilerplate      | Lombok                                      |
| Build            | Maven 3.8+                                  |

---

## Project Structure

```
src/main/java/com/finance/
├── FinanceApplication.java          # Entry point (@SpringBootApplication)
│
├── config/
│   ├── SecurityConfig.java          # Filter chain, CORS, RBAC, JWT wiring
│   └── DataSeeder.java              # CommandLineRunner — seeds demo data on startup
│
├── controller/
│   ├── AuthController.java          # POST /auth/login  GET /auth/me
│   ├── UserController.java          # CRUD /users  (admin only)
│   ├── TransactionController.java   # CRUD /transactions  (role-gated per method)
│   └── DashboardController.java     # GET /dashboard/*  (all authenticated)
│
├── service/
│   ├── UserService.java
│   ├── UserDetailsServiceImpl.java  # Spring Security integration
│   ├── TransactionService.java
│   └── DashboardService.java        # Aggregation logic
│
├── repository/
│   ├── UserRepository.java
│   ├── TransactionRepository.java   # + native SQL aggregation queries
│   └── TransactionSpec.java         # JPA Specification for dynamic filtering
│
├── entity/
│   ├── User.java                    # Implements UserDetails
│   └── Transaction.java             # @SQLRestriction for transparent soft-delete
│
├── dto/
│   ├── AuthDto.java
│   ├── UserDto.java
│   ├── TransactionDto.java
│   ├── DashboardDto.java
│   └── PagedResponse.java           # Generic paginated wrapper
│
├── enums/
│   ├── Role.java                    # VIEWER | ANALYST | ADMIN
│   └── TransactionType.java         # INCOME | EXPENSE
│
├── exception/
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice — maps all exceptions to JSON
│
└── security/
    ├── JwtUtil.java                 # Token generation & validation
    └── JwtAuthFilter.java           # OncePerRequestFilter — extracts + validates JWT
```

---

## Quick Start

### Prerequisites
- **Java 21+**
- **Maven 3.8+**

```bash
# 1. Build
mvn clean package -DskipTests

# 2. Run
java -jar target/finance-backend-1.0.0.jar
```

Server starts on **http://localhost:8080**

On first launch `DataSeeder` runs automatically and prints:

```
✔  Seeded 3 demo users
─────────────────────────────────────────────
  admin@finance.dev    / admin123
  analyst@finance.dev  / analyst123
  viewer@finance.dev   / viewer123
─────────────────────────────────────────────
```

### H2 Console (dev only)
Browse the database at: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:file:./data/financedb`
- Username: `sa` | Password: *(empty)*

---

## Demo Credentials

| Role    | Email                  | Password     |
|---------|------------------------|--------------|
| Admin   | admin@finance.dev      | admin123     |
| Analyst | analyst@finance.dev    | analyst123   |
| Viewer  | viewer@finance.dev     | viewer123    |

---

## Role Permissions Matrix

| Action                         | Viewer | Analyst | Admin |
|--------------------------------|--------|---------|-------|
| Login / view own profile       | ✅     | ✅      | ✅    |
| Read transactions              | ✅     | ✅      | ✅    |
| Read dashboard summaries       | ✅     | ✅      | ✅    |
| Create / update transactions   | ❌     | ✅      | ✅    |
| Delete transactions (soft)     | ❌     | ❌      | ✅    |
| List / manage users            | ❌     | ❌      | ✅    |

---

## API Reference

All protected routes require:
```
Authorization: Bearer <token>
```

---

### Auth

#### `POST /auth/login`
```json
{ "email": "admin@finance.dev", "password": "admin123" }
```
**Response:**
```json
{
  "token": "eyJhbGci...",
  "user": { "id": 1, "name": "Alice Admin", "email": "...", "role": "ADMIN" }
}
```

#### `GET /auth/me`
Returns the currently authenticated user's profile.

---

### Transactions

#### `GET /transactions`
Query parameters (all optional):

| Param      | Type                    | Description                            |
|------------|-------------------------|----------------------------------------|
| `type`     | `INCOME` \| `EXPENSE`   | Filter by type                         |
| `category` | string                  | Filter by category (case-insensitive)  |
| `dateFrom` | `YYYY-MM-DD`            | Start of date range                    |
| `dateTo`   | `YYYY-MM-DD`            | End of date range                      |
| `search`   | string                  | Search notes and category              |
| `page`     | integer                 | Page number (default: 1)               |
| `limit`    | integer (max 100)       | Results per page (default: 20)         |

**Response:**
```json
{
  "data": [ ...transactions ],
  "pagination": { "page": 1, "limit": 20, "total": 10, "pages": 1 }
}
```

#### `GET /transactions/{id}`

#### `POST /transactions` — analyst, admin
```json
{
  "amount": 1500.00,
  "type": "INCOME",
  "category": "Freelance",
  "date": "2024-06-01",
  "notes": "Optional"
}
```

#### `PATCH /transactions/{id}` — analyst, admin
Send only the fields to update (all optional).

#### `DELETE /transactions/{id}` — admin only
Soft deletes the record (`is_deleted = true`). Never physically removed.

---

### Dashboard

#### `GET /dashboard/summary?dateFrom=YYYY-MM-DD&dateTo=YYYY-MM-DD`
```json
{
  "totalIncome": 12300.00,
  "totalExpenses": 4560.00,
  "netBalance": 7740.00,
  "transactionCount": 18
}
```

#### `GET /dashboard/categories?type=EXPENSE`
Totals per category, sorted by amount descending.
```json
[
  { "category": "Rent",      "total": 1200.00, "count": 1 },
  { "category": "Groceries", "total": 500.00,  "count": 3 }
]
```

#### `GET /dashboard/trends?months=12`
Monthly income vs expense for the last N months.
```json
[
  { "month": "2024-01", "income": 5000.00, "expense": 1630.00, "net": 3370.00 },
  { "month": "2024-02", "income": 2500.00, "expense": 600.00,  "net": 1900.00 }
]
```

#### `GET /dashboard/recent?limit=10`
Most recent transactions for an activity feed.

---

### Users — admin only

#### `GET /users?page=1&limit=20`
#### `GET /users/{id}`

#### `POST /users`
```json
{
  "name": "Dana",
  "email": "dana@example.com",
  "password": "securepass",
  "role": "ANALYST"
}
```

#### `PATCH /users/{id}`
```json
{ "role": "ADMIN", "active": false }
```

---

## Error Responses

| Status | Meaning                              |
|--------|--------------------------------------|
| 401    | Missing or invalid/expired token     |
| 403    | Authenticated but insufficient role  |
| 404    | Resource not found                   |
| 409    | Conflict (e.g. duplicate email)      |
| 422    | Validation failed                    |
| 500    | Unexpected server error              |

Validation errors return structured details:
```json
{
  "error": "Validation failed",
  "details": {
    "amount": "must be greater than 0",
    "date": "must not be null"
  }
}
```

---

## Design Decisions

**`User` implements `UserDetails`** — Rather than a separate adapter, the User entity
directly implements Spring Security's `UserDetails`. This keeps the user model as the
single source of truth and avoids an unnecessary translation layer.

**`@PreAuthorize` per method** — Role guards live on the controller methods themselves
(`@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")`), making permissions visible exactly
where the action is defined. No separate permission config file to maintain.

**`JpaSpecificationExecutor` + `TransactionSpec`** — Dynamic filtering (any combination
of type, category, date range, search) is handled via the JPA Criteria API. This avoids
string concatenation and keeps queries type-safe.

**`@SQLRestriction("is_deleted = false")`** — Applied at the entity level in Hibernate 6.
Every query on `Transaction` automatically excludes soft-deleted rows without any
developer effort or risk of forgetting the filter.

**H2 file-based** — Behaves identically to a full relational DB for this use case.
Swapping to PostgreSQL or MySQL requires only changing the dependency and
`application.properties` connection string — zero code changes.

**`DataSeeder` as `CommandLineRunner`** — Seeds idempotently (checks `count > 0`),
so restarting the server never duplicates demo data.

**`GlobalExceptionHandler`** — All exceptions are caught and serialised to JSON in one
place. No `try/catch` in controllers. Each custom exception maps to a clear HTTP status.
