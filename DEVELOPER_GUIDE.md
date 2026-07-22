# 📘 H-Phsar API — Developer Guide

> Your reference when adding new features or modifying existing code in this project.

---

## Table of Contents

| # | Section |
|---|---------|
| 1 | [Project Structure](#1-project-structure) |
| 2 | [Key Common Classes](#2-key-common-classes) |
| 3 | [How to Add a New Feature (Step-by-Step)](#3-how-to-add-a-new-feature) |
| 4 | [Controller Pattern](#4-controller-pattern) |
| 5 | [Service + ServiceImpl Pattern](#5-service-pattern) |
| 6 | [Repository + Mapper XML Pattern](#6-repository--mapper-xml-pattern) |
| 7 | [Exception Handling (예외 처리)](#7-exception-handling) |
| 8 | [Getting the Current User](#8-getting-the-current-user) |
| 9 | [Date Handling (날짜 처리)](#9-date-handling) |
| 10 | [Common Mistakes & Warnings](#10-common-mistakes--warnings) |

---

## 1. Project Structure

```
src/main/java/com/henheang/hphsar/
│
├── common/
│   ├── Pagination.java                    ← Paging response object
│   └── api/
│       ├── ApiResponse.java               ← ✅ Standard response wrapper (USE THIS)
│       ├── PagedResponse.java             ← ✅ Paging response wrapper
│       ├── Code.java                      ← ✅ Response code enum (SUCCESS, NOT_FOUND, etc.)
│       ├── ApiStatus.java
│       └── Common.java
│
├── config/                                ← Spring config (JWT, Security, CORS, etc.)
│
├── controller/
│   ├── BaseController.java                ← ✅ Parent class — all controllers extend this
│   └── supplier/
│       └── product/
│           └── SupplierProductController.java
│
├── service/
│   ├── SupplierProductService.java        ← Interface (계약서 — the contract)
│   └── implement/
│       └── SupplierProductServiceImpl.java  ← Actual logic (구현체)
│
├── repository/
│   └── SupplierProductRepository.java     ← MyBatis mapper interface
│
├── model/
│   └── product/
│       └── Product.java                   ← Data model (VO/DTO)
│
├── exception/
│   ├── GlobalExceptionHandler.java        ← ✅ Catches all exceptions automatically
│   ├── NotFoundException.java
│   ├── BadRequestException.java
│   └── ... (other exception classes)
│
└── utils/
    └── DateTimeUtil.java                  ← ✅ Use this for date formatting

src/main/resources/
└── mapper/
    └── SupplierProductMapper.xml          ← ✅ Actual SQL queries here
```

> ⚠️ **Important:** `model/ApiResponse.java` is the **old version**. Always use `common/api/ApiResponse.java` for new code.

---

## 2. Key Common Classes

### 2.1 BaseController — Response Helper

File: `controller/BaseController.java`

Every controller in this project extends `BaseController`. It gives you shortcut methods so you don't have to build `ApiResponse` manually every time.

```java
// 200 OK with data
return ok("Fetched successfully.", data);

// 201 Created with data
return created("Product added successfully.", data);

// 200 OK with paging (페이징 응답)
return okPage("Fetched successfully.", list, page, size, totalElements);

// Using a Code enum (for specific response codes)
return ok(Code.PROFILE_FETCHED, data);
```

---

### 2.2 ApiResponse — Standard JSON Response Format

File: `common/api/ApiResponse.java`

What the client actually receives:

```json
{
    "status": 200,
    "code": 200,
    "message": "Fetched successfully.",
    "data": { ... },
    "timestamp": "2025-04-23 10:30:00"
}
```

---

### 2.3 Code Enum — Response Codes (응답 코드)

File: `common/api/Code.java`

```java
Code.SUCCESS                // 200 — general success
Code.NOT_FOUND              // 404
Code.EMAIL_ALREADY_EXISTS   // 409 — duplicate email
Code.SYSTEM_ERROR           // 500 — server error
// See Code.java for the full list
```

---

### 2.4 DateTimeUtil — Date Formatting

File: `utils/DateTimeUtil.java`

```java
// LocalDateTime → String (for API responses)
String formatted = DateTimeUtil.format(LocalDateTime.now());
// result: "2025-04-23 10:30:00"

// String → LocalDateTime (for parsing input)
LocalDateTime dt = DateTimeUtil.parse("2025-04-23 10:30:00");
```

---

## 3. How to Add a New Feature

> Example: Adding a new "Order" API

### Step 1 — Create the Model (VO/DTO)
```
model/order/
├── Order.java          ← Response object (what you send back)
└── OrderRequest.java   ← Request object (what you receive from client)
```

### Step 2 — Create the Repository Interface
```
repository/OrderRepository.java   ← @Mapper interface (MyBatis)
```

### Step 3 — Create the Mapper XML (SQL queries here)
```
resources/mapper/OrderMapper.xml
```

### Step 4 — Create the Service Interface (계약서)
```
service/OrderService.java   ← Define what methods exist
```

### Step 5 — Create the Service Implementation (구현체)
```
service/implement/OrderServiceImplV1.java   ← Write the actual logic
```

### Step 6 — Create the Controller
```
controller/order/OrderController.java
```

> Follow this order every time. Do not skip steps.

---

## 4. Controller Pattern

### ✅ Correct Pattern

```java
@RestController
@Tag(name = "Order Controller")
@RequestMapping("${base.supplier.v1}/orders")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class OrderController extends BaseController {   // ← Must extend BaseController

    private final OrderService orderService;

    // GET single item
    @GetMapping("/{id}")
    @Operation(summary = "Get order by id")
    public ResponseEntity<?> getOrderById(@PathVariable Integer id) {
        return ok("Fetched successfully.", orderService.getOrderById(id));
    }

    // POST create
    @PostMapping
    @Operation(summary = "Create new order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        return created("Order created.", orderService.createOrder(request));
    }

    // GET list with paging (페이징)
    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Order> orders = orderService.getAllOrders(page, size);
        long total = orderService.countOrders();
        return okPage("Fetched successfully.", orders, page, size, total);
    }
}
```

### ❌ Old Pattern — Do Not Use

```java
// ❌ Manually building ApiResponse from the old model package
import com.henheang.hphsar.model.ApiResponse;   // ← wrong import

ApiResponse<Product> response = ApiResponse.<Product>builder()
        .status(HttpStatus.OK.value())
        .message("fetched successfully")
        .data(...)
        .date(formatter.format(date = new Date()))   // ← old Date usage
        .build();
return ResponseEntity.ok(response);
```

---

## 5. Service Pattern

### Service Interface (계약서)

```java
// service/OrderService.java
public interface OrderService {
    Order getOrderById(Integer id);
    List<Order> getAllOrders(int page, int size);
    long countOrders();
    Order createOrder(OrderRequest request);
    void deleteOrder(Integer id);
}
```

### Service Implementation (구현체)

```java
// service/implement/OrderServiceImplV1.java
@Service
@RequiredArgsConstructor
@Slf4j   // ← Use log.info() instead of System.out.println()
public class OrderServiceImplV1 implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Order getOrderById(Integer id) {
        Order order = orderRepository.getOrderById(id);
        if (order == null) {
            throw new NotFoundException("Order not found.");
        }
        return order;
    }

    @Override
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for store: {}", request.getStoreId());

        Integer orderId = orderRepository.insertOrder(request);
        if (orderId == null) {
            throw new InternalServerErrorException("Failed to create order.");
        }
        return orderRepository.getOrderById(orderId);
    }
}
```

> ⚠️ **Business logic belongs in the Service, not the Controller.** The controller only receives the request and returns the response.

---

## 6. Repository + Mapper XML Pattern

### Repository Interface

```java
// repository/OrderRepository.java
@Mapper
public interface OrderRepository {
    Order getOrderById(Integer id);
    List<Order> getAllOrders(@Param("page") int page, @Param("size") int size);
    long countOrders();
    Integer insertOrder(OrderRequest request);
    void deleteOrder(Integer id);
}
```

### Mapper XML

```xml
<!-- resources/mapper/OrderMapper.xml -->
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.henheang.hphsar.repository.OrderRepository">

    <!-- ResultMap: maps DB column names (snake_case) to Java field names (camelCase) -->
    <resultMap id="OrderResultMap" type="com.henheang.hphsar.model.order.Order">
        <id     property="id"          column="id"/>
        <result property="storeId"     column="store_id"/>
        <result property="status"      column="status"/>
        <result property="createdDate" column="created_date"/>
    </resultMap>

    <!-- Get one by ID -->
    <select id="getOrderById" resultMap="OrderResultMap">
        SELECT id, store_id, status, created_date
        FROM tb_order
        WHERE id = #{id}
    </select>

    <!-- Get list with paging -->
    <select id="getAllOrders" resultMap="OrderResultMap">
        SELECT id, store_id, status, created_date
        FROM tb_order
        ORDER BY created_date DESC
        LIMIT #{size} OFFSET #{size} * #{page}
    </select>

    <!-- Count total rows -->
    <select id="countOrders" resultType="long">
        SELECT COUNT(*) FROM tb_order
    </select>

    <!-- Insert and return the new ID (PostgreSQL RETURNING) -->
    <select id="insertOrder" resultType="int">
        INSERT INTO tb_order (store_id, status, created_date)
        VALUES (#{storeId}, 'PENDING', NOW())
        RETURNING id
    </select>

    <!-- Delete -->
    <delete id="deleteOrder">
        DELETE FROM tb_order WHERE id = #{id}
    </delete>

</mapper>
```

### Dynamic SQL — sort + pagination (Korean enterprise pattern)

For any list query that supports sorting, use `<sql>` fragments and `<choose>` instead of duplicating ASC/DESC methods.

**Step 1 — Define reusable fragments** (once per mapper file, after resultMaps):

```xml
<sql id="selectOrderBase">
    SELECT id, store_id AS storeId, buyer_account_id AS buyerId,
           created_date AS date, status_id
    FROM tb_order
</sql>

<sql id="orderByDate">
    ORDER BY created_date
    <choose>
        <when test="sort != null and sort.equalsIgnoreCase('asc')">ASC</when>
        <otherwise>DESC</otherwise>
    </choose>
</sql>

<sql id="paginate">
    LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} - 1)
</sql>
```

**Step 2 — Use `<include>` in queries:**

```xml
<select id="getOrders" resultMap="OrderResultMap">
    <include refid="selectOrderBase"/>
    WHERE store_id = #{storeId} AND status_id = 1
    <include refid="orderByDate"/>
    <include refid="paginate"/>
</select>
```

**Step 3 — Single repository method with `sort` param:**

```java
List<Order> getOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);
```

**Step 4 — Service just passes sort through — no if/else:**

```java
List<Order> orders = repository.getOrders(sort, pageNumber, pageSize, storeId);
```

> ❌ **Do not** create two methods `getOrdersASC` / `getOrdersDESC`. All existing mappers have been refactored to this pattern.

---

### PostgreSQL-specific patterns used in this project

```xml
<!-- INSERT and return the generated ID -->
<select id="insertSomething" resultType="int">
    INSERT INTO tb_table (...) VALUES (...) RETURNING id
</select>

<!-- Case-insensitive search (ILIKE = PostgreSQL only) -->
<select id="searchByName">
    SELECT * FROM tb_product WHERE name ILIKE '%${name}%'
</select>

<!-- Return 0 if not found instead of null (COALESCE) -->
<select id="getIdOrZero" resultType="int">
    SELECT COALESCE((SELECT id FROM tb_product WHERE name = #{name}), 0)
</select>
```

> ⚠️ `${variable}` (dollar sign) injects the value directly into SQL — SQL Injection risk.
> Only use it when necessary (e.g., ORDER BY column name), and always validate the value in the Service layer first with a whitelist check. See `SupplierProductServiceImpl.getAllProductBySorting()` for an example.

---

## 7. Exception Handling

### Available exception classes (`exception/` package)

| Class | HTTP Status | When to use |
|-------|-------------|-------------|
| `NotFoundException` | 404 | Data not found |
| `BadRequestException` | 400 | Invalid input from client |
| `UnauthorizedException` | 401 | Not logged in / token invalid |
| `ForbiddenException` | 403 | Logged in but no permission |
| `ConflictException` | 409 | Duplicate data |
| `AlreadyExistException` | 409 | Resource already exists |
| `InternalServerErrorException` | 500 | Unexpected server failure |

### How to use

```java
// Data not found
if (product == null) {
    throw new NotFoundException("Product not found.");
}

// Invalid input
if (request.getName().isBlank()) {
    throw new BadRequestException("Product name is required.");
}

// Duplicate
if (repository.existsByName(name)) {
    throw new ConflictException("Product name already exists.");
}

// Insert/update failed unexpectedly
if (insertedId == null) {
    throw new InternalServerErrorException("Failed to insert product.");
}
```

> 💡 `GlobalExceptionHandler` catches all these automatically and returns the correct HTTP status. You do **not** need to wrap them in try-catch.

---

## 8. Getting the Current User

### Current approach used in this project

```java
// Inside a Service or Controller
AppUser appUser = (AppUser) SecurityContextHolder.getContext()
                                                 .getAuthentication()
                                                 .getPrincipal();
Integer currentUserId = appUser.getId();
```

### Cleaner approach using CurrentUserProvider

File: `service/support/CurrentUserProvider.java`

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImplV1 implements OrderService {

    private final CurrentUserProvider currentUserProvider;

    public void someMethod() {
        Integer userId = currentUserProvider.getCurrentUserId();
    }
}
```

---

## 9. Date Handling

### ✅ Correct — use DateTimeUtil

```java
import com.henheang.hphsar.utils.DateTimeUtil;

// Format current time for response
String now = DateTimeUtil.format(LocalDateTime.now());
// "2025-04-23 10:30:00"

// Parse a date string from input
LocalDateTime dt = DateTimeUtil.parse("2025-04-23 10:30:00");
```

### ❌ Old way — do not use

```java
// ❌ This is the old pattern — avoid it in new code
SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
formatter.format(new Date());
formatter.parse(someString);
```

> Why? `SimpleDateFormat` + `Date` is not thread-safe and was replaced by `LocalDateTime` in Java 8. `DateTimeUtil` is the project standard.

---

## 10. Common Mistakes & Warnings

### ❌ Mistake 1: Using the old ApiResponse

```java
// ❌ Wrong import
import com.henheang.hphsar.model.ApiResponse;

// ✅ Correct import
import com.henheang.hphsar.common.api.ApiResponse;
// Or just use BaseController.ok() / created() — no import needed
```

---

### ❌ Mistake 2: Using System.out.println

```java
// ❌ Does not write to log files in production (운영환경에서 안 남음)
System.out.println("storeId: " + storeId);

// ✅ Use @Slf4j on the class, then use log
@Slf4j
public class OrderServiceImplV1 implements OrderService {

    public void someMethod() {
        log.info("storeId: {}", storeId);
        log.error("Something failed: {}", errorMessage);
    }
}
```

---

### ❌ Mistake 3: Writing business logic inside the Controller

```java
// ❌ Controller doing too much
@GetMapping("/{id}")
public ResponseEntity<?> getProduct(@PathVariable Integer id) {
    Integer storeId = repository.getStoreId(userId);      // ← move this to Service
    if (!repository.storeHasProduct(storeId, id)) { ... } // ← move this to Service
    Product p = repository.getProduct(storeId, id);       // ← move this to Service
    ...
}

// ✅ Controller should only receive and return
@GetMapping("/{id}")
public ResponseEntity<?> getProduct(@PathVariable Integer id) {
    return ok("Fetched successfully.", productService.getProductById(id));
}
```

---

### ❌ Mistake 4: Repeating the same magic number

```java
// ❌ Same number copy-pasted everywhere
if (id > 2147483646) { ... }
if (qty > 2147483646) { ... }
if (pageSize > 2147483646) { ... }

// ✅ Define it once as a constant
private static final int MAX_INT = Integer.MAX_VALUE - 1;
if (id > MAX_INT) { ... }
```

---

### ✅ Checklist before submitting new code (코드 제출 전 체크)

- [ ] Controller extends `BaseController`?
- [ ] Using `common/api/ApiResponse` (not `model/ApiResponse`)?
- [ ] Using `@Slf4j` + `log.info/error` (not `System.out.println`)?
- [ ] Using `DateTimeUtil.format()` (not `SimpleDateFormat`)?
- [ ] Business logic is inside Service, not Controller?
- [ ] Using custom exception classes (not raw try-catch)?
- [ ] If using `${variable}` in XML — is it validated with a whitelist in Service first?
- [ ] List queries with sort use `<sql>` fragments + `<choose>` (not separate ASC/DESC methods)?
- [ ] List controller endpoints use `okPage()` (not `ok()`) so frontend gets `totalElements` and `totalPages`?

---

> This guide is based on the actual code in `h-phsar-api-full`.  
> Update this document when you introduce a new pattern or change an existing one.