# Config Package — How It Works

This package contains all the security and application configuration for the **H-Phsar API**.

---

## Overview — Request Flow

Every HTTP request goes through these layers in order:

```
Browser / Mobile App
        │
        ▼
① CorsFilterConfiguration     →  Is this domain allowed to call the API?
        │
        ▼
② JwtRequestFilter             →  Does the request have a valid JWT token?
   └── JwtTokenUtil             →  Decode + validate the token
        │
        ├── No / Invalid token  →  ③ JwtAuthenticationEntryPoint → 401 JSON response
        │
        ▼ Valid token
④ SecurityConfig                →  Does this user have the right ROLE for this endpoint?
   └── DaoAuthenticationProvider
   └── BeanConfig (PasswordEncoder)
        │
        ├── Wrong role          →  403 Forbidden
        │
        ▼ Authorized
⑤ Controller                   →  Business logic runs
```

---

## File Descriptions

### ① `CorsFilterConfiguration.java`
**What:** Handles Cross-Origin Resource Sharing (CORS).

**Why:** Browsers block requests from one domain to another by default.
For example, a frontend on `http://localhost:3000` cannot call an API on `http://localhost:8080` without CORS permission.

**What it allows:**
- Any domain (`*`) to call the API
- Methods: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`
- Headers: `Authorization`, `Content-Type`, and more

> ⚠️ For production, change `*` to your actual frontend domain for security.

---

### ② `JwtRequestFilter.java`
**What:** Intercepts every HTTP request to check for a valid JWT token.

**How it works:**
1. Reads the `Authorization: Bearer <token>` header
2. Calls `JwtTokenUtil` to extract the user's email from the token
3. Loads the user from the database
4. If token is valid → sets the user as authenticated in Spring Security
5. If token is invalid → returns `401 Unauthorized` immediately

---

### `JwtTokenUtil.java`
**What:** A helper component that creates and reads JWT tokens.

**Token structure:**
```
eyJhbGciOiJIUzUxMiJ9   ← Header  (algorithm: HS512)
.eyJzdWIiOiJ1c2VyQG1haWwuY29tIn0  ← Payload (email, issued time, expiry)
.SflKxwRJSMeKKF2QT4fwp...         ← Signature (proves token is not tampered)
```

**Key methods:**
| Method | Purpose |
|---|---|
| `generateToken(user)` | Creates a new token after login (valid for 24 hours) |
| `validateToken(token, user)` | Checks token belongs to user and is not expired |
| `getUsernameFromToken(token)` | Extracts the user's email from the token |

**Secret key:** Configured in `application.properties` → `jwt.secret`

---

### ③ `JwtAuthenticationEntryPoint.java`
**What:** Returns a clean JSON `401 Unauthorized` error when a request has no valid token.

**Why:** Without this, Spring Security returns an HTML error page — not useful for mobile/frontend apps.

**Example response:**
```json
{
  "status": 401,
  "message": "Unauthorized access",
  "error": "Full authentication is required. Please provide a valid Bearer token."
}
```

---

### ④ `SecurityConfig.java`
**What:** The main security rulebook — defines who can access what.

**Endpoint access rules:**
| Endpoint | Access |
|---|---|
| `/authorization/**` | Public — no token needed (login, register, OTP) |
| `/api/v1/files/**` | Public — no token needed (view uploaded files) |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public — API documentation |
| `/api/v1/buyers/**` | Requires `BUYER` role |
| `/api/v1/suppliers/**` | Requires `SUPPLIER` role |
| `/api/v1/admin/**` | Requires `ADMIN` role (supplier/buyer account management) |
| Everything else | Requires any valid token |

**Other settings:**
- CSRF disabled → safe because we use stateless JWT, not cookies
- Sessions disabled → `STATELESS` mode, every request must carry its own token
- Registers `JwtRequestFilter` to run before the default Spring auth filter
- Registers `JwtAuthenticationEntryPoint` to handle unauthenticated errors

---

### `BeanConfig.java`
**What:** Registers shared beans used across the whole application.

| Bean | Purpose | Used By |
|---|---|---|
| `PasswordEncoder` | BCrypt hashing for passwords | `SecurityConfig`, user registration |
| `ModelMapper` | Converts Entity ↔ DTO objects | Service layer |

---

### `DatabaseInitializer.java`
**What:** Runs SQL on startup to ensure the database schema is ready.

**When:** Runs once automatically after Spring starts (`@PostConstruct`), before any request is handled.

**What it creates/adds:**
| SQL | Purpose |
|---|---|
| `CREATE TABLE IF NOT EXISTS tb_supplier_otp` | OTP table for supplier email verification |
| `CREATE TABLE IF NOT EXISTS tb_buyer_otp` | OTP table for buyer email verification |
| `ALTER TABLE tb_store ADD COLUMN IF NOT EXISTS is_active` | Soft-delete flag for stores |
| `ALTER TABLE tb_store ADD COLUMN IF NOT EXISTS phone` | Phone number for stores |

> All statements use `IF NOT EXISTS` — safe to run on every startup.

---

### `WebMvcConfig.java`
**What:** Maps uploaded files on disk to a public HTTP URL.

**Example:**
```
File saved to  →  ./uploads/photo.jpg
Accessible via →  GET /api/v1/files/photo.jpg
```

**Config in `application.properties`:**
```properties
file.upload-dir=uploads/
```

---

## How The Config Files Depend On Each Other

```
BeanConfig
  └── PasswordEncoder  ──────────────────────► SecurityConfig (DaoAuthenticationProvider)
  └── ModelMapper      ──────────────────────► Service Layer

JwtTokenUtil ─────────────────────────────────► JwtRequestFilter (decode/validate token)
                                              ► JwtAuthenticationController (generate token on login)

JwtAuthenticationEntryPoint ──────────────────► SecurityConfig (registered as 401 handler)

JwtRequestFilter ─────────────────────────────► SecurityConfig (registered as pre-auth filter)

CorsFilterConfiguration ──────────────────────► Runs at servlet level (before SecurityConfig)

WebMvcConfig ─────────────────────────────────► SecurityConfig (files/** marked as permitAll)

DatabaseInitializer ──────────────────────────► Runs on startup, independent of the above
```