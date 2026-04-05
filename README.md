# Finance Backend API

> A RESTful backend system for a finance dashboard application built with **Spring Boot 3.5** and **Java 21**. The system manages financial records and user access through a role-based permission model with JWT authentication, dynamic filtering, and aggregated dashboard analytics.

---

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Layer-by-Layer Breakdown](#layer-by-layer-breakdown)
  - [1. Entity Layer](#1-entity-layer)
  - [2. Repository Layer](#2-repository-layer)
  - [3. Service Layer](#3-service-layer)
  - [4. Controller Layer](#4-controller-layer)
  - [5. Security Layer](#5-security-layer)
  - [6. Exception Layer](#6-exception-layer)
- [Role Permissions](#role-permissions)
- [Setup and Installation](#setup-and-installation)
- [Demo Credentials](#demo-credentials)
- [Testing with Swagger UI](#testing-with-swagger-ui)
- [Testing with Postman](#testing-with-postman)
- [API Reference](#api-reference)
- [Error Responses](#error-responses)
- [Design Decisions](#design-decisions)

---

## Project Description

This project is a backend system for a finance dashboard that allows different users to interact with financial records based on their assigned role. It was built as a backend engineering assignment to demonstrate API design, data modeling, business logic, and access control.

**Key capabilities:**
- Three-tier role system — Viewer, Analyst, Admin — each with clearly defined permissions
- Full CRUD operations for financial transactions (income and expense records)
- Dynamic filtering by date range, category, type, and keyword search
- Dashboard analytics — total income, total expenses, net balance, monthly trends, and category breakdowns
- JWT-based authentication with token expiry
- Soft delete — records are never physically removed, preserving audit history
- Auto-seeded demo users on first startup — no manual setup needed
- Swagger UI for live API testing and documentation

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Web | Spring MVC — REST Controllers |
| Security | Spring Security 6 + JWT (jjwt 0.12) |
| ORM | Spring Data JPA + Hibernate 6 |
| Database | H2 file-based database |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |
| Documentation | Springdoc OpenAPI 3.1 (Swagger UI) |
| Build Tool | Maven 3.8+ |

---

## Project Structure

```
finance-spring/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/com/finance/
        │   ├── FinanceApplication.java
        │   │
        │   ├── config/
        │   │   ├── SecurityConfig.java
        │   │   ├── OpenApiConfig.java
        │   │   └── DataSeeder.java
        │   │
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── UserController.java
        │   │   ├── TransactionController.java
        │   │   └── DashboardController.java
        │   │
        │   ├── service/
        │   │   ├── UserService.java
        │   │   ├── UserDetailsServiceImpl.java
        │   │   ├── TransactionService.java
        │   │   └── DashboardService.java
        │   │
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── TransactionRepository.java
        │   │   └── TransactionSpec.java
        │   │
        │   ├── entity/
        │   │   ├── User.java
        │   │   └── Transaction.java
        │   │
        │   ├── dto/
        │   │   ├── AuthDto.java
        │   │   ├── UserDto.java
        │   │   ├── TransactionDto.java
        │   │   ├── DashboardDto.java
        │   │   └── PagedResponse.java
        │   │
        │   ├── enums/
        │   │   ├── Role.java
        │   │   └── TransactionType.java
        │   │
        │   ├── exception/
        │   │   └── GlobalExceptionHandler.java
        │   │
        │   └── security/
        │       ├── JwtUtil.java
        │       └── JwtAuthFilter.java
        │
        └── resources/
            └── application.properties
```

---

## Layer-by-Layer Breakdown

### 1. Entity Layer
**Location:** `src/main/java/com/finance/entity/`

This is the data model layer. Each class maps directly to a database table.

**`User.java`**
- Fields: `id`, `name`, `email`, `passwordHash`, `role`, `isActive`, `createdAt`, `updatedAt`
- Implements Spring Security's `UserDetails` interface directly — no separate adapter needed
- Role is stored as an enum: `VIEWER`, `ANALYST`, or `ADMIN`
- `isActive` flag allows deactivating a user without deleting them

**`Transaction.java`**
- Fields: `id`, `userId`, `amount`, `type`, `category`, `date`, `notes`, `isDeleted`, `createdAt`, `updatedAt`
- `@SQLRestriction("is_deleted = false")` — Hibernate automatically excludes deleted records from every query
- Type is an enum: `INCOME` or `EXPENSE`
- Amount uses `BigDecimal` for precision

---

### 2. Repository Layer
**Location:** `src/main/java/com/finance/repository/`

This layer handles all database communication using Spring Data JPA.

**`UserRepository.java`**
- Extends `JpaRepository` — provides save, find, delete out of the box
- Custom method: `findByEmail()` for login lookup
- Custom method: `existsByEmail()` for duplicate check on registration

**`TransactionRepository.java`**
- Extends `JpaRepository` + `JpaSpecificationExecutor` — enables dynamic filtering
- Native SQL query for category totals grouped by category name
- Native SQL query for monthly trends grouped by year-month

**`TransactionSpec.java`**
- Builds dynamic WHERE clauses using JPA Criteria API
- Each filter (type, category, dateFrom, dateTo, search) is only applied if the value is provided
- Prevents SQL injection — no string concatenation anywhere

---

### 3. Service Layer
**Location:** `src/main/java/com/finance/service/`

This layer contains all business logic. Controllers call services, services call repositories.

**`UserService.java`**
- `listAll()` — paginated list of all users
- `findById()` — fetch one user, throws 404 if not found
- `create()` — hashes password with BCrypt, saves user, throws 409 on duplicate email
- `update()` — partial update of name, role, or active status

**`TransactionService.java`**
- `list()` — paginated list with dynamic filters passed to `TransactionSpec`
- `findById()` — fetch one transaction, throws 404 if not found
- `create()` — links transaction to the authenticated user
- `update()` — partial update, only provided fields are changed
- `softDelete()` — sets `isDeleted = true`, never physically removes the record

**`DashboardService.java`**
- `getSummary()` — calculates total income, total expenses, net balance, and count
- `getCategoryTotals()` — groups transactions by category, sorted by total descending
- `getMonthlyTrends()` — monthly income vs expense for the last N months
- `getRecentActivity()` — latest N transactions sorted by date

**`UserDetailsServiceImpl.java`**
- Implements Spring Security's `UserDetailsService`
- Called by the security filter to load a user by email during token validation

---

### 4. Controller Layer
**Location:** `src/main/java/com/finance/controller/`

This layer exposes HTTP endpoints. Each controller is thin — it validates input, calls a service, and returns a response.

**`AuthController.java`**
- `POST /auth/login` — authenticates credentials, returns JWT token
- `GET /auth/me` — returns the currently logged-in user's profile

**`UserController.java`** — Admin only (`@PreAuthorize("hasRole('ADMIN')")` on class)
- `GET /users` — list all users with pagination
- `GET /users/{id}` — get one user
- `POST /users` — create a new user with a role
- `PATCH /users/{id}` — update name, role, or active status

**`TransactionController.java`** — Role-gated per method
- `GET /transactions` — all roles can read, supports all filters
- `GET /transactions/{id}` — all roles can read
- `POST /transactions` — Analyst and Admin only
- `PATCH /transactions/{id}` — Analyst and Admin only
- `DELETE /transactions/{id}` — Admin only (soft delete)

**`DashboardController.java`** — All authenticated users
- `GET /dashboard/summary` — totals with optional date range
- `GET /dashboard/categories` — category breakdown with optional type filter
- `GET /dashboard/trends` — monthly trends for last N months
- `GET /dashboard/recent` — latest N transactions

---

### 5. Security Layer
**Location:** `src/main/java/com/finance/security/` and `src/main/java/com/finance/config/SecurityConfig.java`

**`JwtUtil.java`**
- Generates JWT tokens on login with 8-hour expiry
- Validates tokens — checks signature and expiry
- Extracts email from token to identify the user

**`JwtAuthFilter.java`**
- Runs on every request before it hits the controller
- Reads the `Authorization: Bearer <token>` header
- Validates the token and sets the authenticated user in Spring Security's context
- If no token or invalid token — the request continues unauthenticated (401 is returned by the endpoint)

**`SecurityConfig.java`**
- Disables CSRF (stateless REST API — not needed)
- Sets session management to STATELESS (JWT handles sessions)
- Permits: `/auth/login`, `/health`, `/swagger-ui/**`, `/v3/api-docs/**`
- Requires authentication for all other routes
- Registers the JWT filter before Spring's default auth filter
- Returns JSON 401/403 responses instead of HTML redirects

---

### 6. Exception Layer
**Location:** `src/main/java/com/finance/exception/GlobalExceptionHandler.java`

Single class annotated with `@RestControllerAdvice` that catches every exception thrown anywhere in the application and converts it to a clean JSON response.

| Exception | HTTP Status | When it happens |
|---|---|---|
| `ResourceNotFoundException` | 404 | User or transaction ID not found |
| `ConflictException` | 409 | Email already registered |
| `BadCredentialsException` | 401 | Wrong email or password |
| `AccessDeniedException` | 403 | Correct token but wrong role |
| `MethodArgumentNotValidException` | 422 | Failed bean validation |
| `Exception` (catch-all) | 500 | Unexpected server error |

---

## Role Permissions

| Action | Viewer | Analyst | Admin |
|---|---|---|---|
| Login and view own profile | ✅ | ✅ | ✅ |
| Read all transactions | ✅ | ✅ | ✅ |
| Filter and search transactions | ✅ | ✅ | ✅ |
| View dashboard summaries | ✅ | ✅ | ✅ |
| Create transactions | ❌ | ✅ | ✅ |
| Update transactions | ❌ | ✅ | ✅ |
| Delete transactions | ❌ | ❌ | ✅ |
| View all users | ❌ | ❌ | ✅ |
| Create new users | ❌ | ❌ | ✅ |
| Change user roles | ❌ | ❌ | ✅ |
| Deactivate users | ❌ | ❌ | ✅ |

---

## Setup and Installation

### Prerequisites
- Java 21 or higher
- Maven 3.8 or higher

### Step 1 — Clone the repository
```bash
git clone https://github.com/Aishwarya8py/finance-backend.git
cd finance-backend/finance-spring
```

### Step 2 — Build the project
```bash
mvn clean package -DskipTests
```

### Step 3 — Run the server
```bash
java -jar target/finance-backend-1.0.0.jar
```

Server starts at **http://localhost:8080**

On first startup, `DataSeeder` automatically creates three demo users and ten sample transactions. You will see this in the console:

```
✔  Seeded 3 demo users
─────────────────────────────────────
  admin@finance.dev    / admin123
  analyst@finance.dev  / analyst123
  viewer@finance.dev   / viewer123
─────────────────────────────────────
```

### H2 Database Console
Access the database browser at **http://localhost:8080/h2-console**
```
JDBC URL:  jdbc:h2:file:./data/financedb
Username:  sa
Password:  (leave empty)
```

---

## Demo Credentials

| Role | Email | Password |
|---|---|---|
| Admin | admin@finance.dev | admin123 |
| Analyst | analyst@finance.dev | analyst123 |
| Viewer | viewer@finance.dev | viewer123 |

---

## Testing with Swagger UI

Swagger UI is built into the project. No setup needed.

### Step 1 — Start the server
Run the project in IntelliJ or via terminal as shown above.

### Step 2 — Open Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### Step 3 — Login and get a token
1. Find the **auth-controller** section
2. Click `POST /auth/login`
3. Click **Try it out**
4. Enter the request body:
```json
{
  "email": "admin@finance.dev",
  "password": "admin123"
}
```
5. Click **Execute**
6. Copy the `token` value from the response

### Step 4 — Authorize Swagger
1. Click the **Authorize** button (🔒) at the top right of the page
2. Paste the token in the **Value** field
3. Click **Authorize** then **Close**

All subsequent requests will automatically include the token.

### Step 5 — Test any endpoint
1. Find the endpoint you want to test
2. Click **Try it out**
3. Fill in any parameters or request body
4. Click **Execute**
5. View the response below

### Testing role restrictions
1. Login as viewer (`viewer@finance.dev / viewer123`)
2. Copy the viewer token
3. Click Authorize and paste the viewer token
4. Try `POST /transactions`
5. You will receive `403 Forbidden` — proving role-based access control works

---

## Testing with Postman

### Step 1 — Login
```
Method:  POST
URL:     http://localhost:8080/auth/login
Headers: Content-Type: application/json
Body (raw JSON):
{
  "email": "admin@finance.dev",
  "password": "admin123"
}
```
Copy the `token` from the response.

### Step 2 — Set the token
In every subsequent request, add this header:
```
Key:   Authorization
Value: Bearer <paste your token here>
```

**Tip:** In Postman go to the **Tests** tab of your login request and paste:
```javascript
pm.environment.set("token", pm.response.json().token);
```
Then use `{{token}}` in all other requests instead of pasting manually every time.

### Step 3 — Test all endpoints

**Create a transaction**
```
Method:  POST
URL:     http://localhost:8080/transactions
Body:
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-06-01",
  "notes": "June salary"
}
Expected: 201 Created
```

**Get all transactions with filters**
```
Method:  GET
URL:     http://localhost:8080/transactions?type=EXPENSE&dateFrom=2024-01-01&dateTo=2024-12-31
Expected: 200 OK with paginated list
```

**Update a transaction**
```
Method:  PATCH
URL:     http://localhost:8080/transactions/1
Body:
{
  "amount": 5500.00,
  "notes": "Updated amount"
}
Expected: 200 OK with updated record
```

**Delete a transaction (admin only)**
```
Method:  DELETE
URL:     http://localhost:8080/transactions/1
Expected: 200 OK — { "message": "Transaction deleted successfully." }
```

**Get dashboard summary**
```
Method:  GET
URL:     http://localhost:8080/dashboard/summary
Expected:
{
  "totalIncome": 12300.00,
  "totalExpenses": 3480.00,
  "netBalance": 8820.00,
  "transactionCount": 10
}
```

**Get monthly trends**
```
Method:  GET
URL:     http://localhost:8080/dashboard/trends?months=6
Expected: Array of monthly income vs expense objects
```

**Create a new user (admin only)**
```
Method:  POST
URL:     http://localhost:8080/users
Body:
{
  "name": "John Doe",
  "email": "john@finance.dev",
  "password": "password123",
  "role": "ANALYST"
}
Expected: 201 Created
```

**Deactivate a user (admin only)**
```
Method:  PATCH
URL:     http://localhost:8080/users/2
Body:
{
  "active": false
}
Expected: 200 OK with updated user showing active: false
```

---

## API Reference

### Auth Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Login and get JWT token |
| GET | `/auth/me` | All roles | Get current user profile |

### Transaction Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/transactions` | All roles | List all with filters and pagination |
| GET | `/transactions/{id}` | All roles | Get single transaction |
| POST | `/transactions` | Analyst, Admin | Create new transaction |
| PATCH | `/transactions/{id}` | Analyst, Admin | Partial update |
| DELETE | `/transactions/{id}` | Admin only | Soft delete |

**Available filters for GET /transactions:**

| Parameter | Type | Example |
|---|---|---|
| `type` | `INCOME` or `EXPENSE` | `?type=EXPENSE` |
| `category` | string | `?category=Salary` |
| `dateFrom` | YYYY-MM-DD | `?dateFrom=2024-01-01` |
| `dateTo` | YYYY-MM-DD | `?dateTo=2024-12-31` |
| `search` | string | `?search=rent` |
| `page` | integer | `?page=1` |
| `limit` | integer (max 100) | `?limit=10` |

### Dashboard Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/dashboard/summary` | All roles | Total income, expenses, net balance |
| GET | `/dashboard/categories` | All roles | Totals grouped by category |
| GET | `/dashboard/trends` | All roles | Monthly income vs expense |
| GET | `/dashboard/recent` | All roles | Latest N transactions |

### User Endpoints — Admin Only

| Method | Endpoint | Description |
|---|---|---|
| GET | `/users` | List all users |
| GET | `/users/{id}` | Get one user |
| POST | `/users` | Create user |
| PATCH | `/users/{id}` | Update role or status |

---

## Error Responses

| Status | Meaning |
|---|---|
| 200 | Success |
| 201 | Created successfully |
| 401 | Missing token or wrong credentials |
| 403 | Token valid but role insufficient |
| 404 | Resource not found |
| 409 | Conflict — email already exists |
| 422 | Validation failed — check details field |
| 500 | Unexpected server error |

**Validation error response format:**
```json
{
  "error": "Validation failed",
  "details": {
    "amount": "must be greater than 0",
    "date": "must not be null",
    "email": "must be a well-formed email address"
  }
}
```

---

## Design Decisions

**Spring Boot 3.5 with Java 21** was chosen for its production-grade ecosystem, built-in dependency injection, and seamless integration with Spring Security and JPA. It follows industry-standard patterns making the code maintainable and easy to extend.

**H2 file-based database** was used for simplicity and zero-configuration setup. The JPA layer is database-agnostic — switching to PostgreSQL or MySQL only requires changing the dependency and two lines in `application.properties` with no code changes.

**JWT authentication** was chosen over session-based auth because it is stateless, scales horizontally, and is the standard approach for REST APIs consumed by frontends or mobile apps.

**Role hierarchy enforced at two levels** — route level via `SecurityConfig` (who needs a token at all) and method level via `@PreAuthorize` annotations (which role can call which method). This makes permissions explicit and visible exactly where each action is defined.

**Soft delete pattern** — transactions are never physically removed. Setting `is_deleted = true` preserves audit history and makes recovery straightforward. Hibernate's `@SQLRestriction` applies the filter automatically to every query so developers cannot accidentally retrieve deleted records.

**JPA Specifications for filtering** — dynamic query building using the Criteria API avoids string concatenation, prevents SQL injection, and keeps the filter logic type-safe and testable.

**`GlobalExceptionHandler`** with `@RestControllerAdvice` — all exceptions are caught and serialised to JSON in one place. No try/catch blocks in controllers. Each exception class maps to a clear HTTP status code.

**`DataSeeder` as `CommandLineRunner`** — seeds idempotently on startup by checking `count > 0`, so restarting the server never creates duplicate demo data.
