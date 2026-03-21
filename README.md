# H-Phsar API

> **H-Phsar** (H = Hen Heang · Phsar = ផ្សារ Market) — A Cambodian B2B online marketplace platform connecting Distributors and Retailers.

**Build with** Spring Boot

## PROJECT ADDRESS
https://spring.hanyeaktong.site/swagger-ui/index.html


## Installation


****To Get started:****

1. Clone repo
    ```bash
    git clone https://github.com/Hen-Heang/h-phsar-api.git
    ```

2. To run the code
   - Start the project Go to file > Open > Choose the folder of the cloned project
   - Run the project by clicking the start button
   - Open browser and type http://localhost:8888/swagger-ui/index.html

> **H-Phsar** is a Cambodian B2B online marketplace (ផ្សារ) for business between Distributor and Retailer. Business owners can trade more efficiently with lower operational complexity.

### What we have done

- **API authorization** (/authorization)
  http://localhost:8888/authorization
  - Sign up : register an account.
  - Log in : log in your account if verified
  - Change password: Change to new password
  - Forget Password: By combining OTP endpoint, we can request new password if we forget our password
- **Generate OTP** (/authorization/api/v1/otp/)
  http://localhost:8888/authorization/api/v1/otp/
  - Generate OTP : generate a new 4 digits OTP code and send it to your email.
  - Veryfy OTP : Verify your account

**There are 2 types of user in the system**

*Distributor*

- **Distributor profile controller** (/api/v1/distributor/profiles/)
  http://localhost:8888/api/v1/distributor/profiles
  - Add user profile : create a user profile for distributor.
  - Get user profile : get details of user profile
  - Update user profile : update distributor user profile
- **Distributor Store Controller** (/api/v1/distributor/stores/)
  http://localhost:8888/api/v1/distributor/stores
  - Setup new store : Setup new store for distributor
  - Get store detail : view detail of store
  - Edit store : update store detail
  - Disable store : delist store from public view
  - Enable store : list store back to view back
  - Delete store : remove store completely
- **Distributor Category Controller** (/api/v1/distributor/categories/)
  http://localhost:8888/api/v1/distributor/categories
  - Create new category : create new category for the store
  - Get category by id : get category detail using the id
  - Get all category : get every category in the store
  - Search category by name : filter category by searching the name
  - Edit category : update category info
  - Delete category : remove category from store
- **Distributor Product Controller** (/api/v1/distributor/products/)
  http://localhost:8888/api/v1/distributor/products
  - Create new product : Create new product for the store
  - Import product : restock product and set new price for the product
  - Get all product :  get product listing with sorting. can sort by created_date, qty, name, price, and product_id
  - Get product by id : get product detail by id
  - Edit product : update product detail
  - Unpublish product : delist product from public
  - Publish product : list product in public
  - Delete product : remove product permanently
- **Distributor Order Controller** (/api/v1/distributor/orders/)
  http://localhost:8888/api/v1/distributor/orders
  - Get all order : get all orders listing
  - Get pending order : get all orders that is pending
  - Get preparing order : get all orders that is preparing
  - Get dispatching order : get all orders that is dispatching
  - Get confirming order : get all orders that is comfirming
  - Get completed order : get all orders that has been completed
  - Get order detail : get detail of an order
  - Get invoice : get invoice of the store. order need to be completed in other to get invoice
  - Accept order : accept order that is still in pending. update to preparing
  - Decline order : decline order that is in pending
  - Update order to dispatching : update order status that is preparing to dispatching
  - Update order to confirming : update order status that is dispatching to confirming. confirm order will wait for retailer to comfirm that they got the product/order
- **Distributor Homepage Controller** (/api/v1/distributor/order_activities/)
  http://localhost:8888/api/v1/distributor/order_activities
  - Get all order : get all orders listing
- **Distributor Report Controller** (/api/v1/distributor/reports/)
  http://localhost:8888/api/v1/distributor/reports
  - Get order activity homepage by month : get order activity by monthly basis
  - Get order activity homepage by year : get order activity by yearly basis
- **Distributor History Controller** (/api/v1/distributor/history/)
  http://localhost:8888/api/v1/distributor/history
  - Get history order : get history of order. have pagination
  - Get history import : get history of import or restock

*Retailer*

- **Retailer Store Controller** (/api/v1/retailer/stores/)
  http://localhost:8888/api/v1/retailer/stores
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
- **Retailer Profile Controller** (/api/v1/retailer/profiles/)
  http://localhost:8888/api/v1/retailer/profiles
  - Create retailer profile : create a profile for retailer. if not created, can not use all feature
  - Get profile : Get profile details
  - Edit profile : Change or update profile
- **Retailer Profile Controller** (/api/v1/retailer/orders/)
  http://localhost:8888/api/v1/retailer/orders
  - Add to cart : add product to cart. can be one product or a list
  - Update cart product : update product in the cart (qty)
  - Remove product : remove product from cart
  - Confirm order : confirm cart as order
  - Draft cart : save cart as draft for continuing later
  - Cancel order : delete cart and cancel order
  - Confirm transaction : when product is delivered retailer have to confirm that they receive the product
  - Get cart details : get details of cart
  - Get order details : get details of the order by id
  - Get orders progress : get all orders and its progress
  - Get invoice : get invoice details
- **Retailer Report Controller** (/api/v1/retailer/reports/)
  http://localhost:8888/api/v1/retailer/report
  - Get report : get retailer report
- **Retailer History Controller** (/api/v1/retailer/history/)
  http://localhost:8888/api/v1/retailer/history
  - Get order history : get history of order
  - Get draft : get saved draft
  
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
- This system have 2 roles which is distributor and retailer.
- This system need real verifiable email address with OTP
- Role of distributor is 1 while retailer is 2

---

## Code Review & Improvement Plan

> This section documents the findings from a full codebase review. Items are grouped by priority. Each item explains **what** the problem is and **why** it matters.

---

### Group A — Security (Critical)

These issues can cause data breaches, unauthorized access, or exploitable vulnerabilities in production.

#### A1. Weak default JWT secret
- **What:** `application.properties` has `jwt.secret=JwtSuperSecretHahaKidding` as the fallback value.
- **Why:** If the `JWT_SECRET` environment variable is not set, any attacker who reads the source code (e.g., from a public repo) can forge valid JWT tokens and impersonate any user.
- **Fix:** Remove the default value. Force the app to fail on startup if `JWT_SECRET` is not provided.

#### A2. Weak default database password
- **What:** `spring.datasource.password=${DB_PASSWORD:123}` defaults to `"123"`.
- **Why:** Any exposed database port becomes trivially accessible. Even in development, weak defaults create bad habits.
- **Fix:** Remove the default. Require the `DB_PASSWORD` env var to be set explicitly.

#### A3. CORS allows all origins
- **What:** `CorsFilterConfiguration.java` uses `allowedOriginPatterns("*")`, allowing any domain to call the API.
- **Why:** This disables the browser's Same-Origin Policy protection entirely. In production, malicious websites can make cross-origin requests on behalf of authenticated users.
- **Fix:** Restrict to specific trusted domains via an env var (e.g., `ALLOWED_ORIGINS`).

#### A4. Outdated JWT library (jjwt 0.9.1 from 2018)
- **What:** `pom.xml` depends on `io.jsonwebtoken:jjwt:0.9.1`.
- **Why:** This version is 6+ years old, has known issues, and uses a deprecated API. The current version is `0.12.x` with a significantly improved and safer API.
- **Fix:** Upgrade to `jjwt-api`, `jjwt-impl`, and `jjwt-jackson` at version `0.12.x`.

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

#### D1. Near-zero test coverage
- **What:** Only 2 test files exist — a basic context load test and one `DateTimeUtil` unit test. No tests for controllers, services, or repositories.
- **Why:** Without tests, every code change risks breaking existing behavior silently. Refactoring is dangerous. Bugs are only caught in production.
- **Fix:** Add unit tests for all service classes using JUnit 5 + Mockito. Add integration tests for key API endpoints using `MockMvc` and a test database.

---

### Group E — Database Schema (Low-Medium)

#### E1. OTP tables created at runtime
- **What:** `DatabaseInitializer.java` creates `tb_distributor_otp` and `tb_retailer_otp` tables with `CREATE TABLE IF NOT EXISTS` at every application startup.
- **Why:** Runtime schema creation is fragile, hard to audit, and conflicts with proper migration practices. It also runs on every restart unnecessarily.
- **Fix:** Move these table definitions into a Flyway migration script and remove `DatabaseInitializer`.

---

### Summary Table

| Group | Area | Priority | Items |
|-------|------|----------|-------|
| A | Security | Critical | A1 A2 A3 A4 A5 |
| B | Code Quality | High | B1 B2 B3 B4 B5 B6 |
| C | Observability | Medium | C1 C2 C3 C4 C5 |
| D | Testing | Medium | D1 |
| E | Database | Low-Medium | E1 |
