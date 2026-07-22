# H-Phsar API

> **H-Phsar** (H = Hen Heang · Phsar = ផ្សារ Market) — A Cambodian B2B marketplace connecting suppliers and business buyers.

**Build with** Spring Boot

## PROJECT ADDRESS
http://localhost:8080/swagger-ui/index.html


## Installation


****To Get started:****

1. Clone repo
    ```bash
    git clone https://github.com/Hen-Heang/h-phsar-api-full.git
    ```

2. Set up your local environment and database
   - See [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md) — copy `.env.example`, provide your own database/mail/JWT values, no secret is hardcoded or defaulted anymore
   - Run the project (`.\mvnw.cmd spring-boot:run` or your IDE's run button, with the environment variables loaded)
   - Open browser and type http://localhost:8080/swagger-ui/index.html

3. Read the coding standards before adding features
   - See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for patterns, conventions, and common mistakes

> **H-Phsar** is a Cambodian B2B online marketplace (ផ្សារ) for business between Supplier and Buyer. Business owners can trade more efficiently with lower operational complexity.

### What we have done

- **API authorization** (/authorization)
  http://localhost:8080/authorization
  - Sign up : register an account.
  - Log in : log in your account if verified
  - Change password: Change to new password
  - Forget Password: By combining OTP endpoint, we can request new password if we forget our password
- **Generate OTP** (/authorization/api/v1/otp/)
  http://localhost:8080/authorization/api/v1/otp/
  - Generate OTP : generate a new 4 digits OTP code and send it to your email.
  - Veryfy OTP : Verify your account

**There are 2 types of user in the system**

*Supplier — owns stores, manages catalog, inventory and incoming orders*

- **Supplier Profile Controller** (/api/v1/suppliers/profiles/)
  http://localhost:8080/api/v1/suppliers/profiles
  - Add user profile : create a user profile for supplier.
  - Get user profile : get details of user profile
  - Update user profile : update supplier user profile
- **Supplier Store Controller** (/api/v1/suppliers/stores/)
  http://localhost:8080/api/v1/suppliers/stores
  - Setup new store : Setup new store for supplier
  - Get store detail : view detail of store
  - Edit store : update store detail
  - Disable store : delist store from public view
  - Enable store : list store back to view back
  - Delete store : remove store completely
- **Supplier Category Controller** (/api/v1/suppliers/categories/)
  http://localhost:8080/api/v1/suppliers/categories
  - Create new category : create new category for the store
  - Get category by id : get category detail using the id
  - Get all category : get every category in the store
  - Search category by name : filter category by searching the name
  - Edit category : update category info
  - Delete category : remove category from store
- **Supplier Product Controller** (/api/v1/suppliers/products/)
  http://localhost:8080/api/v1/suppliers/products
  - Create new product : Create new product for the store
  - Import product : restock product and set new price for the product
  - Get all product :  get product listing with sorting. can sort by created_date, qty, name, price, and product_id
  - Get product by id : get product detail by id
  - Edit product : update product detail
  - Unpublish product : delist product from public
  - Publish product : list product in public
  - Delete product : remove product permanently
- **Supplier Order Controller** (/api/v1/suppliers/orders/)
  http://localhost:8080/api/v1/suppliers/orders
  - Get all order : get all orders listing
  - Get pending order : get all orders that are pending
  - Get preparing order : get all orders that are being prepared
  - Get dispatching order : get all orders that are dispatched
  - Get confirming order : get all orders in confirming state
  - Get completed order : get all orders that have been completed
  - Get order detail : get detail of an order
  - Get invoice : get invoice of the store. order must be completed to generate invoice
  - Accept order : accept a pending order — status moves to PROCESSING
  - Decline order : decline a pending order — status moves to REJECTED
  - Dispatch order : mark a PROCESSING order as dispatched — status moves to CONFIRMED
  - Mark delivered : mark a CONFIRMED order as delivered — status auto-completes to COMPLETED (no buyer confirmation required)
- **Supplier Homepage Controller** (/api/v1/suppliers/order_activities/)
  http://localhost:8080/api/v1/suppliers/order_activities
  - Get all order : get all orders listing
- **Supplier Report Controller** (/api/v1/suppliers/reports/)
  http://localhost:8080/api/v1/suppliers/reports
  - Get order activity homepage by month : get order activity by monthly basis
  - Get order activity homepage by year : get order activity by yearly basis
- **Supplier History Controller** (/api/v1/suppliers/history/)
  http://localhost:8080/api/v1/suppliers/history
  - Get history order : get history of order. have pagination
  - Get history import : get history of import or restock
- **Supplier Notification Controller** (/api/v1/suppliers/notifications/)
  http://localhost:8080/api/v1/suppliers/notifications
  - Get all notification : fetch all notifications for the supplier
  - Mark as read : mark a single notification as read by id
  - Mark all as read : mark all notifications as read

*Buyer — browses stores, manages carts and places business orders*

- **Buyer Store Controller** (/api/v1/buyers/stores/)
  http://localhost:8080/api/v1/buyers/stores
  - Rate store : give rating to a store
  - Bookmark store : bookmark a store. set store as favorite
  - Edit rating : change rating of a store
  - Delete rating : remove rating from a store
  - Remove bookmark : unbookmark a store
  - Get store rating : Get rating of a store by store id
  - Get store product : get all products from store
  - Get store by id : fetch store by its id
  - Get store order by rating : get store filter by store rating
  - Get store order by name : get store filter by name
  - Get store order by favorite : get store filter by favorite. can be first or last
  - Get store order by date : get store filter by created date
  - Get store by bookmark : get only boomarked/favorite store
  - Search store by name : find store by searching store name
  - Get all store : get store without filter
- **Buyer Profile Controller** (/api/v1/buyers/profiles/)
  http://localhost:8080/api/v1/buyers/profiles
  - Create buyer profile : create a profile for buyer. if not created, can not use all feature
  - Get profile : Get profile details
  - Edit profile : Change or update profile
- **Buyer Order Controller** (/api/v1/buyers/orders/)
  http://localhost:8080/api/v1/buyers/orders
  - Add to cart : add product to cart. can be one product or a list
  - Update cart product : update product quantity in cart
  - Remove product : remove product from cart
  - Confirm order : submit cart as a live order (status → PENDING)
  - Draft cart : save cart as draft for later
  - Cancel order : delete cart and cancel order
  - Get cart details : get details of cart
  - Get order details : get details of an order by id
  - Get orders progress : get all active orders and their status
  - Get invoice : get invoice for a completed order
- **Buyer Report Controller** (/api/v1/buyers/reports/)
  http://localhost:8080/api/v1/buyers/reports
  - Get report : get buyer report
- **Buyer History Controller** (/api/v1/buyers/history/)
  http://localhost:8080/api/v1/buyers/history
  - Get order history : get history of order
  - Get draft : get saved draft
- **Buyer Notification Controller** (/api/v1/buyers/notifications/)
  http://localhost:8080/api/v1/buyers/notifications
  - Get all notification : fetch all notifications for the buyer
  - Mark as read : mark a single notification as read by id
  - Mark all as read : mark all notifications as read

## Testing

### Database Server
- Database : warehouse_master
- Username : warehouse_master
- Password : warehouse_master
- Port : 5436
- Ip / Host : 8.219.139.67

### Connection Database
Go to **DataGrip** and choose the connection then input the information as provided (Database server).

**Note**
- This system have 2 roles which is supplier and buyer.
- This system need real verifiable email address with OTP
- Role of supplier is 1 while buyer is 2

---

## Order Flow

```
Buyer places order
        │
        ▼  status_id = 1
    PENDING  ──── Supplier declines ──→  REJECTED (8)
        │
        │  Supplier accepts
        ▼  status_id = 2
   PROCESSING
        │
        │  Supplier dispatches
        ▼  status_id = 3
   CONFIRMED
        │
        │  Supplier marks delivered
        ▼  status_id = 6
   COMPLETED  ✅  (auto-complete — no buyer confirmation required)
```

**Status ID reference (tb_status)**

| ID | Name | Who acts |
|----|------|----------|
| 1 | PENDING | Buyer just placed order |
| 2 | PROCESSING | Supplier accepted |
| 3 | CONFIRMED | Supplier dispatched |
| 6 | COMPLETED | Supplier marked delivered — order done |
| 8 | REJECTED | Supplier declined |
| 9 | DRAFT | Buyer saved cart as draft |

> Statuses 4 (SHIPPING), 5 (DELIVERED), 7 (CANCELLED) exist in the DB but are not used in the current flow.

---

## Notification System

Notifications are sent automatically at each order status change:

| Event | Recipient | Type ID |
|-------|-----------|---------|
| Order placed | Supplier | 3 — New Order |
| Stock runs out after accept | Supplier | 2 — Out of Stock |
| Order accepted | Buyer | 4 — Order Accepted |
| Order declined | Buyer | 5 — Order Declined |
| Order dispatched | Buyer | 7 — Order Dispatching |
| Order completed | Buyer | 9 — Order Complete |

Each notification stores `order_id` so the frontend can link directly to the order detail page (`/web/buyers/orders?orderId=X`).

---

## Security Flow — How It Works Step by Step

### STEP 1 — Register `POST /authorization/register`

```
Client sends: { email, password, roleId }
        │
        ▼
JwtAuthenticationController.insertUser()
        │
        ▼
JwtUserDetailsServiceImpl.insertUser()
        ├── Validate roleId is 1 (SUPPLIER) or 2 (BUYER)
        ├── Validate email format (regex)
        ├── Check duplicate email in tb_supplier_account OR tb_buyer_account
        ├── BCrypt encode the password
        └── INSERT INTO tb_supplier_account / tb_buyer_account
            is_verified = FALSE by default  ← account locked until OTP verified

Returns: AppUserDto (id, email, roleId)
```

---

### STEP 2 — Verify Email with OTP

#### Generate OTP `POST /authorization/api/v1/otp/generate`
```
Client sends: email
        │
        ▼
OtpServiceImplV1.generateOtp()
        ├── Find user in tb_supplier_account OR tb_buyer_account
        ├── Generate random 4-digit number (1000–9999)
        ├── EmailService.sendSimpleMail()  → sends OTP code to user's email
        └── INSERT INTO tb_supplier_otp / tb_buyer_otp  (with timestamp)
```

#### Verify OTP `POST /authorization/api/v1/otp/verify`
```
Client sends: otp (4-digit), email
        │
        ▼
OtpServiceImplV1.verifyOtp()
        ├── Check user is NOT already verified
        ├── Load user + latest OTP from DB
        ├── Check: email matches
        ├── Check: OTP code matches
        ├── Check: created_date < 3 minutes ago  ← expired if older
        └── UPDATE tb_supplier_account SET is_verified = TRUE

Account is now ACTIVE and can log in.
```

---

### STEP 3 — Login `POST /authorization/login`

```
Client sends: { email, password }
        │
        ▼
JwtAuthenticationController.createAuthenticationToken()
        │
        ├── Check is_verified = TRUE in DB
        │       └── if FALSE → auto-send new OTP and return 409
        │
        ├── AuthenticationManager.authenticate()
        │       └── DaoAuthenticationProvider
        │               ├── loadUserByUsername(email)  → fetch AppUser from DB
        │               └── BCrypt.matches(rawPassword, hashedPassword)
        │                       └── if wrong → 400 INVALID_PASSWORD
        │
        ├── JwtTokenUtil.generateToken()
        │       └── Build JWT: subject=email, expiry=24h, signed with HS512
        ├── getRoleIdByMail(email)
        └── getUserIdByMail(email)

Returns: { token, roleId, userId }
```

---

### STEP 4 — Every Protected API Request

```
Client sends: Authorization: Bearer <token>
        │
        ▼
TrailingSlashNormalizationFilter  ← Strip trailing slash (e.g. /profiles/ → /profiles)
        │
        ▼
CorsFilterConfiguration       ← Allow cross-origin request from browser
        │
        ▼
JwtRequestFilter
        ├── Extract token from "Bearer <token>" header
        ├── JwtTokenUtil.getUsernameFromToken()  → decode email from token
        │       └── if invalid/tampered → return 401 immediately
        ├── loadUserByUsername(email)  → fetch user from DB
        └── JwtTokenUtil.validateToken()
                ├── email in token == email in DB?
                └── token not expired?
                        └── if OK → set user as authenticated
        │
        ▼
SecurityConfig — check role permission
        ├── /api/v1/suppliers/**    → requires SUPPLIER role (roleId = 1)
        ├── /api/v1/buyers/**       → requires BUYER role    (roleId = 2)
        ├── /api/v1/files/**        → public (file uploads/downloads)
        ├── /ws/**                  → public (WebSocket endpoint)
        └── wrong role → 403 | no token → 401 JSON (JwtAuthenticationEntryPoint)
        │
        ▼
Controller → Business Logic
```

---

### STEP 5 — Change Password & Forget Password

```
── Change Password (requires login token) ────────────────────
PUT /authorization/change-password
        ├── BCrypt.matches(oldPassword, storedHash)  ← verify old password
        ├── BCrypt.encode(newPassword)
        └── UPDATE password in DB

── Forget Password (no token needed, uses OTP) ───────────────
PUT /authorization/forget?otp=1234&email=...&newPassword=...
        ├── Load user + latest OTP from DB
        ├── Check: OTP code matches
        ├── Check: OTP created < 3 minutes ago
        ├── BCrypt.encode(newPassword)
        └── UPDATE password in DB
```

---

### Security File Map

| File | Role |
|---|---|
| `AppUser` | Spring UserDetails — holds email, hashed password, role |
| `AppUserRepository` | MyBatis XML mapper: insert / find / update users |
| `OtpRepository` | SQL: generate and fetch OTP records |
| `JwtUserDetailsServiceImpl` | Core logic: register, login helpers, passwords |
| `OtpServiceImplV1` | Generate OTP, send email, verify OTP + expiry |
| `JwtAuthenticationController` | Endpoints: `/register` `/login` `/change-password` `/forget` |
| `OTPController` | Endpoints: `/otp/generate` `/otp/verify` |
| `JwtTokenUtil` | Create and decode JWT tokens (jjwt 0.12.x) |
| `JwtRequestFilter` | Validate token on every request |
| `TrailingSlashNormalizationFilter` | Strip trailing slashes before auth checks (highest precedence) |
| `SecurityConfig` | Route access rules by role |
| `JwtAuthenticationEntryPoint` | Return 401 JSON when unauthenticated |

---

## Recent Bug Fixes

| # | File | Issue | Fix |
|---|------|-------|-----|
| 1 | `BuyerReportMapper.xml` | `BindingException: Invalid bound statement` — mapper XML did not exist so all 16 buyer report queries failed at runtime | Created the complete XML mapper file with all SQL statements |
| 2 | `BeanConfig.java` | `HttpMessageNotReadableException: Cannot construct instance of ArrayList from String value` — sending `additionalPhone` as a plain string instead of array caused a 400 parse error | Added `ObjectMapper` bean with `ACCEPT_SINGLE_VALUE_AS_ARRAY` — both `"phone"` and `["phone"]` now work |
| 3 | `SupplierStoreServiceImpl.java` | Creating a store with an unverified account returned HTTP 401, which caused the frontend to auto-logout the user | Changed `UnauthorizedException` → `ForbiddenException` so the response is HTTP 403 (no auto-logout) |
| 4 | `BuyerProfileServiceImpl.java` | `NullPointerException: Cannot invoke String.isEmpty() because getProfileImage() is null` — `.isEmpty()` was called directly on a nullable field | Replaced all `.isEmpty()` checks with `== null || .isBlank()` for all 5 validated fields |
| 5 | `StoreMapper.xml`, `SupplierProductMapper.xml` | SQL injection — category/product store-search and the product-name lookup interpolated the search term directly into the query via MyBatis `${name}` instead of binding it | Parameterized all 9 occurrences with `#{name}` (`CONCAT('%', #{name}, '%')` for the two partial searches, plain `#{name}` for the exact-match lookup) |
| 6 | `HistoryRepository.java`, `HistoryMapper.xml`, `HistoryServiceImplV1.java` | IDOR — deleting/submitting a draft order only checked that the draft existed, not that it belonged to the logged-in buyer, so any buyer could delete or submit anyone else's draft by id | Draft queries now require the draft id **and** the authenticated buyer's account id to match in the same row; unknown draft and "belongs to someone else" return the identical 404 |

---

## Code Review & Improvement Plan

> This section documents the findings from a full codebase review. Items are grouped by priority. Each item explains **what** the problem is and **why** it matters.

---

### Group A — Security (Critical)

These issues can cause data breaches, unauthorized access, or exploitable vulnerabilities in production.

#### ~~A1. Weak default JWT secret~~ ✅ RESOLVED
- **What was fixed:** `application.yaml` no longer has any fallback value for `jwt.secret` — it's `${JWT_SECRET}` with nothing after the colon, so the app fails to start rather than sign tokens with a guessable key. See [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md) for how to generate your own local secret.

#### ~~A2. Weak default database password~~ ✅ RESOLVED
- **What was fixed:** `application.yaml` no longer has any fallback value for `spring.datasource.password`/`username`/`url` — all three are `${DB_URL}` / `${DB_USERNAME}` / `${DB_PASSWORD}` with no default, so a missing env var fails startup instead of connecting with `"123"`.

#### A3. CORS allows all origins
- **What:** `CorsFilterConfiguration.java` uses `allowedOriginPatterns("*")`, allowing any domain to call the API.
- **Why:** This disables the browser's Same-Origin Policy protection entirely. In production, malicious websites can make cross-origin requests on behalf of authenticated users.
- **Fix:** Restrict to specific trusted domains via an env var (e.g., `ALLOWED_ORIGINS`).

#### ~~A4. Outdated JWT library~~ ✅ RESOLVED
- **What was fixed:** Upgraded from `io.jsonwebtoken:jjwt:0.9.1` to the split `jjwt-api`, `jjwt-impl`, and `jjwt-jackson` at version **0.12.6**.
- **Impact:** `JwtTokenUtil` now uses the modern `Jwts.parser().verifyWith(key).build()` API with proper `HmacSHA512` key construction.

#### A5. `AlreadyExistException` returns HTTP 404 instead of 409
- **What:** The `AlreadyExistException` class is mapped to HTTP status `404 Not Found`.
- **Why:** 404 means "resource not found." A duplicate/conflict should return `409 Conflict`. Incorrect status codes confuse API consumers and make error handling on the client side unreliable.
- **Fix:** Change the HTTP status to `409 CONFLICT`.

---

### Group B — Code Quality (High)

These issues reduce reliability, make the codebase harder to maintain, and can cause silent bugs.

#### B1. No request validation
- **What:** Controller methods accept request body DTOs but none have `@Valid`. DTO fields have no `@NotNull`, `@NotBlank`, `@Size`, or `@Email` constraints.
- **Why:** Invalid or malformed input (empty strings, null fields, invalid emails) reaches the service layer and database without being rejected. This causes cryptic errors and potential data corruption.
- **Fix:** Add `jakarta.validation` annotations to all request DTOs and `@Valid` to all controller method parameters.

#### B2. Dockerfile JAR name mismatch
- **What:** The Dockerfile references `warehouse_master-0.0.1-SNAPSHOT.jar` but `pom.xml` defines the artifact as `stock-flow-commerce-0.0.1-SNAPSHOT.jar`.
- **Why:** The Docker image will fail to build or start because the JAR it tries to run does not exist.
- **Fix:** Update the Dockerfile `COPY` and `ENTRYPOINT` to use the correct artifact ID.

#### B3. No database migration tool
- **What:** Database schema is managed through raw SQL files (`table.sql`, `create_all_tables.sql`) that must be run manually. Two OTP tables are created at runtime in `DatabaseInitializer.java`.
- **Why:** There is no way to track which schema version the database is at, apply incremental changes safely, or roll back a bad migration. Teams working together will get out of sync.
- **Fix:** Integrate **Flyway** and convert all SQL scripts into versioned migration files (e.g., `V1__init_schema.sql`).

#### B4. Duplicate `ApiResponse` class
- **What:** There are two separate `ApiResponse` classes — one in `common/api/` and one in `model/`.
- **Why:** Developers may import the wrong one, causing inconsistent API responses. It also creates confusion about which one to use.
- **Fix:** Delete the `model/ApiResponse.java` duplicate. Use only `common/api/ApiResponse.java` everywhere.

#### B5. Inconsistent service implementation naming
- **What:** Service implementations use mixed suffixes: `ServiceImplV1`, `ServiceImp`, `ServiceImpleV1`, `ServiceImpl`.
- **Why:** Inconsistency makes the codebase harder to navigate and signals a lack of conventions, which slows onboarding.
- **Fix:** Standardize all to `*ServiceImpl`.

#### B6. Two `DateTimeUtil` classes
- **What:** Both `DateTimeUtils.java` (in `common/utils/`) and `DateTimeUtil.java` (in `utils/`) exist with overlapping functionality.
- **Why:** Developers don't know which one to use. Logic may diverge over time.
- **Fix:** Merge into a single `DateTimeUtils.java` in `common/utils/`.

---

### Group C — Observability (Medium)

These issues make it very hard to debug problems and monitor the application in production.

#### C1. No `@Transactional` on service methods
- **What:** Service methods that perform multiple database writes (e.g., placing an order, updating product stock) do not use `@Transactional`.
- **Why:** If the second write fails after the first succeeds, the database is left in a corrupt/partial state with no automatic rollback.
- **Fix:** Annotate all service methods that involve multiple DB operations with `@Transactional`.

#### C2. No structured logging
- **What:** The service layer has little to no logging. `@Slf4j` is not consistently used.
- **Why:** When something goes wrong in production, there are no logs to trace what happened. Debugging becomes guesswork.
- **Fix:** Add `@Slf4j` to all service classes and log key events (user login, order placed, errors, etc.).

#### C3. No Spring Actuator
- **What:** `spring-boot-starter-actuator` is not included.
- **Why:** There is no `/health`, `/metrics`, or `/readiness` endpoint. Deployment platforms (Docker, Kubernetes) cannot check if the app is alive, and there is no way to observe memory/CPU usage.
- **Fix:** Add `spring-boot-starter-actuator` to `pom.xml` and expose health endpoints.

#### C4. No rate limiting on auth/OTP endpoints
- **What:** `/authorization/login`, `/authorization/register`, and `/authorization/api/v1/otp/generate` have no rate limiting.
- **Why:** Attackers can brute-force passwords or abuse the OTP email sender to spam users and exhaust SMTP quotas.
- **Fix:** Add rate limiting using **Bucket4j** or Spring's built-in throttling on these endpoints.

#### C5. Dockerfile uses full JDK image instead of slim JRE
- **What:** `FROM openjdk:20-jdk` is used. Also, the Java version is 20 in the Dockerfile but 17 in `pom.xml`.
- **Why:** The full JDK image is ~600MB larger than needed. A JRE is sufficient to run a compiled Spring Boot app. The version mismatch can also cause unexpected behavior.
- **Fix:** Use `eclipse-temurin:17-jre-alpine` to match Java 17 and reduce image size significantly.

---

### Group D — Testing (Medium)

#### ~~D1. Near-zero test coverage~~ 🟡 PARTIALLY RESOLVED
- **What was fixed:** Added a reusable Testcontainers PostgreSQL base (`AbstractIntegrationTest`, seeded from the project's own `schema.sql`) plus MyBatis/service integration tests covering the SQL-injection and draft-ownership fixes above (`StoreSearchSqlInjectionIT`, `SupplierProductLookupSqlInjectionIT`, `DraftOrderOwnershipIT`).
- **Still open:** These new tests need Docker to actually run (not yet verified passing in every environment — compiles cleanly, blocked by "no Docker" where it isn't installed). No controller-level (`MockMvc`) tests yet, and most services still have no coverage.

---

### Group E — Database Schema (Low-Medium)

#### E1. Schema changes applied at runtime
- **What:** `DatabaseInitializer.java` runs `@PostConstruct` SQL at every startup to ensure schema readiness:
  - Creates `tb_supplier_otp` and `tb_buyer_otp` tables (`CREATE TABLE IF NOT EXISTS`)
  - Adds `is_active BOOLEAN DEFAULT TRUE` column to `tb_store` (`ALTER TABLE … ADD COLUMN IF NOT EXISTS`)
  - Adds `phone VARCHAR(50)` column to `tb_store`
- **Why:** Runtime schema changes are fragile, hard to audit, and conflict with proper migration practices. Running on every restart is unnecessary overhead.
- **Fix:** Move all table and column definitions into a Flyway migration script and remove `DatabaseInitializer`.

---

### Summary Table

| Group | Area | Priority | Items |
|-------|------|----------|-------|
| A | Security | Critical | ~~A1~~ ~~A2~~ A3 ~~A4~~ A5 |
| B | Code Quality | High | B1 B2 B3 B4 B5 B6 |
| C | Observability | Medium | C1 C2 C3 C4 C5 |
| D | Testing | Medium | D1 🟡 |
| E | Database | Low-Medium | E1 |
