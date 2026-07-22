# SQL Learning Guide — H-Phsar Project
> Every query in this guide is taken directly from this project's mapper XML files.
> The real table names and column names match your actual PostgreSQL database.

---

## Table of Contents

1. [Project Tables Reference](#1-project-tables-reference)
2. [Basic SELECT](#2-basic-select)
3. [WHERE Conditions](#3-where-conditions)
4. [Column Alias (AS)](#4-column-alias-as)
5. [JOIN](#5-join)
6. [Aggregation — COUNT, SUM, AVG, COALESCE](#6-aggregation--count-sum-avg-coalesce)
7. [GROUP BY and HAVING](#7-group-by-and-having)
8. [Subqueries](#8-subqueries)
9. [INSERT, UPDATE, DELETE](#9-insert-update-delete)
10. [Pagination — LIMIT / OFFSET](#10-pagination--limit--offset)
11. [PostgreSQL-Specific Features](#11-postgresql-specific-features)
12. [Dynamic SQL — MyBatis XML Tags](#12-dynamic-sql--mybatis-xml-tags)
13. [ResultMap — Mapping Nested Objects](#13-resultmap--mapping-nested-objects)
14. [Practice Exercises](#14-practice-exercises)

---

## 1. Project Tables Reference

These are the real tables in your database. Use these when writing your practice queries.

```
tb_supplier_account   (id, role_id, email, password, is_verified, is_active, created_date, updated_date)
tb_buyer_account      (id, role_id, email, password, is_verified, is_active, created_date, updated_date)
tb_role                  (id, name)

tb_store                 (id, supplier_account_id, name, description, banner_image, address,
                          phone, is_publish, is_active, created_date, updated_date)
tb_store_phone           (id, store_id, phone)
tb_store_category        (id, store_id, category_id)
tb_category              (id, name, created_date, updated_date)

tb_store_product_detail  (id, store_id, product_id, qty, price, is_publish, image,
                          category_id, description)
tb_product               (id, name, is_active, created_date, updated_date)
tb_product_import        (id, created_date, store_id)
tb_product_import_detail (id, product_id, product_import_id, qty, price, category_id)

tb_order                 (id, buyer_account_id, store_id, status_id, created_date, updated_date)
tb_order_detail          (id, order_id, qty, unit_price, store_product_id)
tb_status                (id, name)

tb_buyer_info         (id, buyer_account_id, first_name, last_name, gender, address,
                          primary_phone_number, profile_image, created_date, updated_date)
tb_buyer_phone        (id, buyer_info_id, phone_number)
tb_supplier_info      (id, supplier_account_id, first_name, last_name, gender, address,
                          primary_phone_number, profile_image, created_date, updated_date)
tb_supplier_phone     (id, supplier_info_id, phone_number)

tb_bookmark              (id, store_id, buyer_account_id)
tb_rating_detail         (id, store_id, buyer_account_id, rated_star, created_date)

tb_notification_type         (id, name)
tb_buyer_notification     (id, buyer_id, type_id, order_id, content, is_read, created_date)
tb_supplier_notification  (id, supplier_id, type_id, order_id, content, is_read, created_date)
```

**Status IDs (tb_status):**

| ID | Meaning |
|----|---------|
| 1 | PENDING |
| 2 | PROCESSING |
| 3 | CONFIRMED |
| 6 | COMPLETED |
| 8 | REJECTED |
| 9 | CART (draft not submitted) |

---

## 2. Basic SELECT

The simplest query: read rows from a table.

```sql
-- Syntax
SELECT column1, column2 FROM table_name;

-- Get all columns
SELECT * FROM table_name;
```

### From your project:

```sql
-- AppUserMapper.xml — get role id for a user
SELECT role_id FROM tb_supplier_account WHERE email = #{email}

-- AppUserMapper.xml — get user id
SELECT id FROM tb_supplier_account WHERE email = #{email}

-- AppUserMapper.xml — check if email is verified
SELECT is_verified FROM tb_supplier_account WHERE email = #{email}

-- CategoryMapper.xml — get total category count for a store
SELECT count(*) FROM tb_store_category WHERE store_id = #{storeId}

-- StoreMapper.xml — get the store id for a user
SELECT id FROM tb_store WHERE supplier_account_id = #{currentUserId}
```

### Key rules:
| Rule | Example |
|------|---------|
| `SELECT *` returns all columns | `SELECT * FROM tb_category` |
| List specific columns to save bandwidth | `SELECT id, name FROM tb_category` |
| Always match the column name from the DB | Column is `is_verified`, not `isVerified` |

---

## 3. WHERE Conditions

Filter which rows to return.

```sql
-- Syntax
SELECT ... FROM table WHERE condition;
```

### Comparison operators:

```sql
-- Equals
WHERE id = 5
WHERE is_verified = true
WHERE is_publish = false

-- Not equals
WHERE status_id != 9

-- Greater / Less
WHERE qty > 0
WHERE qty >= 10
WHERE price < 1000

-- Multiple conditions with AND
WHERE store_id = #{storeId} AND status_id = 1

-- Multiple conditions with OR
WHERE status_id = 5 OR status_id = 6

-- IN — match any value in a list
WHERE status_id IN (1, 2, 3, 4, 5)

-- NOT IN
WHERE status_id NOT IN (8, 9)
```

### From your project:

```sql
-- SupplierOrderMapper.xml — all active orders for a store
SELECT id, store_id, buyer_account_id, created_date, status_id
FROM tb_order
WHERE store_id = #{storeId} AND status_id IN (1, 2, 3, 4, 5)

-- BuyerOrderMapper.xml — check if buyer has an active cart in a store
SELECT EXISTS(
    SELECT * FROM tb_order
    WHERE store_id = #{storeId}
      AND buyer_account_id = #{buyerId}
      AND status_id = 9
)

-- NotificationMapper.xml — unread notifications only
SELECT EXISTS(
    SELECT 1 FROM tb_buyer_notification
    WHERE buyer_id = #{currentUserId} AND is_read = false
)

-- StoreMapper.xml — only published and active stores
SELECT id, name FROM tb_store
WHERE is_publish = true AND is_active = true

-- SupplierReportMapper.xml — date range filter
WHERE b.created_date BETWEEN '${startDate}' AND '${endDate}'
```

### NULL handling:

```sql
-- Check if a column is null
WHERE profile_image IS NULL

-- Check if a column has a value
WHERE profile_image IS NOT NULL
```

---

## 4. Column Alias (AS)

Rename a column in the query result. This is how your project bridges DB naming (snake_case) to Java naming (camelCase).

```sql
-- Syntax
SELECT column_name AS java_field_name FROM table_name;
```

### From your project:

```sql
-- AppUserMapper.xml — many aliases in one query
SELECT ta.id,
       ta.email,
       ta.password,
       tr.name     AS role,          -- rename: name → role
       ta.role_id,
       ta.is_verified,               -- auto-mapped if resultMap defines it
       ta.is_active
FROM tb_supplier_account ta
JOIN tb_role tr ON ta.role_id = tr.id
WHERE ta.email = #{email}

-- StoreMapper.xml — multiple snake_case → camelCase aliases
SELECT id,
       name,
       address,
       banner_image           AS bannerImage,
       supplier_account_id AS supplierAccountId,
       description,
       phone                  AS primaryPhone,
       created_date           AS createdDate,
       updated_date           AS updatedDate,
       is_publish             AS isPublish,
       is_active              AS isActive
FROM tb_store
WHERE supplier_account_id = #{currentUserId}

-- NotificationMapper.xml — alias with NULL literal value
SELECT id,
       order_id      AS orderId,
       NULL          AS store,        -- Java field "store" gets null value
       NULL          AS image,
       content       AS title,
       is_read       AS seen,
       created_date  AS createdDate
FROM tb_buyer_notification
```

### When to use AS:
| Situation | Example |
|-----------|---------|
| DB column is snake_case, Java field is camelCase | `banner_image AS bannerImage` |
| Renaming for clarity | `tr.name AS role` |
| Aggregation result needs a name | `SUM(qty) AS totalQty` |
| Subquery result needs a name | `(SELECT AVG(rated_star) ...) AS rating` |

---

## 5. JOIN

Combine data from two or more tables based on a matching column.

```sql
-- Syntax
SELECT ... FROM table_a
JOIN table_b ON table_a.column = table_b.column
```

### JOIN types used in this project:

#### INNER JOIN — only rows that exist in BOTH tables

```sql
-- AppUserMapper.xml — get user with role name
SELECT ta.id, ta.email, tr.name AS role
FROM tb_supplier_account ta
JOIN tb_role tr ON ta.role_id = tr.id       -- only rows where role_id matches in tb_role
WHERE ta.email = #{email}
```

If a user has a `role_id` that doesn't exist in `tb_role`, that user is NOT returned.

```sql
-- CategoryMapper.xml — category with join to get full data
SELECT TCG.id, name, created_date, updated_date
FROM tb_category TCG
    INNER JOIN tb_store_category TSC ON TCG.id = TSC.category_id
WHERE TSC.store_id = #{storeId}
```

```sql
-- SupplierOrderMapper.xml — product items in an order
SELECT tspd.id AS productId,
       image,
       tspd.qty AS inStock,
       a.qty    AS qty,
       a.unit_price AS unitPrice,
       (a.qty * a.unit_price) AS subTotal
FROM tb_order_detail AS a
    JOIN tb_store_product_detail tspd ON a.store_product_id = tspd.id
WHERE order_id = #{orderId}
```

#### FULL OUTER JOIN — all rows from both tables, NULL where no match

```sql
-- StoreMapper.xml — all stores with their bookmarks (even stores with no bookmark)
FROM tb_store
    FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
WHERE is_publish = TRUE
```

Comparison:

| tb_store.id | tb_bookmark.store_id | INNER JOIN result | FULL OUTER JOIN result |
|-------------|----------------------|-------------------|------------------------|
| 1           | 1                    | ✅ included        | ✅ included             |
| 2           | (no bookmark)        | ❌ excluded        | ✅ included (bookmark columns = NULL) |
| (none)      | 3 (orphan)           | ❌ excluded        | ✅ included (store columns = NULL) |

#### Multiple JOINs — chain as many as needed

```sql
-- StoreMapper.xml — category names for a store's products
SELECT DISTINCT A.id, A.name
FROM tb_category A
    INNER JOIN tb_store_product_detail B ON A.id = B.category_id
WHERE store_id = #{storeId}

-- HistoryMapper.xml — import history across 3 tables
FROM tb_product_import_detail
    JOIN tb_product_import tpi ON tpi.id = tb_product_import_detail.product_import_id
WHERE store_id = #{storeId}

-- BuyerReportMapper.xml — order + detail + product + category (4 tables)
FROM tb_order o
    JOIN tb_order_detail od          ON o.id              = od.order_id
    JOIN tb_store_product_detail spd ON od.store_product_id = spd.id
    JOIN tb_category c               ON spd.category_id    = c.id
WHERE o.buyer_account_id = #{currentUserId}
```

### Table alias shorthand:

```sql
-- Without alias (hard to read)
SELECT tb_store.id, tb_store.name FROM tb_store JOIN tb_bookmark ON tb_store.id = tb_bookmark.store_id

-- With alias (clean and short)
FROM tb_order o
    JOIN tb_order_detail od ON o.id = od.order_id
    -- now use o.store_id instead of tb_order.store_id
```

---

## 6. Aggregation — COUNT, SUM, AVG, COALESCE

Aggregate functions calculate a single value from many rows.

### COUNT — count rows

```sql
-- Count all rows
SELECT count(*) FROM tb_store WHERE is_publish = true

-- Count in a specific group
SELECT count(*) FROM tb_order WHERE store_id = #{storeId} AND status_id = 1

-- Count distinct values (no duplicates)
SELECT count(DISTINCT store_id) FROM tb_bookmark WHERE buyer_account_id = #{buyerId}
```

### SUM — add up values

```sql
-- Total revenue for an order
SELECT sum(unit_price * qty) FROM tb_order_detail WHERE order_id = #{orderId}

-- Total expense from imports
SELECT COALESCE(sum(qty * price), 0) AS totalExpense
FROM tb_product_import_detail a
    JOIN tb_product_import b ON b.id = a.product_import_id
WHERE b.store_id = #{storeId}
```

### AVG — calculate average

```sql
-- Average store rating (returns double)
SELECT COALESCE(AVG(rated_star), 0) FROM tb_rating_detail WHERE store_id = #{id}
```

### COALESCE — return the first non-NULL value

```sql
-- Syntax: COALESCE(value, fallback_if_null)
COALESCE(AVG(rated_star), 0)     -- if no ratings exist, AVG returns NULL → return 0 instead
COALESCE(SUM(qty), 0)            -- if no rows, SUM returns NULL → return 0 instead
COALESCE(COUNT(*), 0)            -- COUNT never returns NULL but this is a safe habit
```

### From your project (BuyerReportMapper.xml):

```sql
-- Total money spent by a buyer in a month
SELECT COALESCE(SUM(od.qty * od.unit_price), 0)
FROM tb_order o
    JOIN tb_order_detail od ON o.id = od.order_id
WHERE o.buyer_account_id = #{currentUserId}
  AND o.status_id = #{statusId}
  AND EXTRACT(YEAR  FROM o.created_date) = #{year}
  AND EXTRACT(MONTH FROM o.created_date) = #{month}

-- Count how many ratings this buyer submitted
SELECT COALESCE(COUNT(*), 0)
FROM tb_rating_detail
WHERE buyer_account_id = #{currentUserId}
  AND EXTRACT(YEAR  FROM created_date) = #{year}
  AND EXTRACT(MONTH FROM created_date) = #{month}
```

---

## 7. GROUP BY and HAVING

`GROUP BY` groups rows with the same value so you can aggregate per group.

```sql
-- Syntax
SELECT column, AGG_FUNCTION(other_column)
FROM table
GROUP BY column
```

### From your project:

```sql
-- StoreMapper.xml — calculate rating per store, then sort by it
SELECT tb_store.id,
       name,
       (SELECT coalesce(avg(rated_star), 0)
        FROM tb_rating_detail WHERE store_id = tb_store.id) AS rating
FROM tb_store
    FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
WHERE is_publish = TRUE
GROUP BY tb_store.id         -- one row per store
ORDER BY rating DESC
LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} - 1)

-- BuyerReportMapper.xml — total qty per category
SELECT COALESCE(SUM(od.qty), 0)
FROM tb_order o
    JOIN tb_order_detail od          ON o.id              = od.order_id
    JOIN tb_store_product_detail spd ON od.store_product_id = spd.id
WHERE o.buyer_account_id = #{currentUserId}
  AND o.status_id = #{statusId}
GROUP BY spd.category_id     -- one sum per category

-- BuyerReportMapper.xml — category name + total qty ordered
SELECT c.name AS categoryName, SUM(od.qty) AS totalItem
FROM tb_order o
    JOIN tb_order_detail od          ON o.id              = od.order_id
    JOIN tb_store_product_detail spd ON od.store_product_id = spd.id
    JOIN tb_category c               ON spd.category_id    = c.id
WHERE o.buyer_account_id = #{currentUserId}
GROUP BY c.name
```

### HAVING — filter AFTER grouping (WHERE filters before grouping)

```sql
-- Example: only show categories that have more than 10 items ordered
SELECT c.name, SUM(od.qty) AS totalItem
FROM tb_order o
    JOIN tb_order_detail od          ON o.id              = od.order_id
    JOIN tb_store_product_detail spd ON od.store_product_id = spd.id
    JOIN tb_category c               ON spd.category_id    = c.id
WHERE o.buyer_account_id = #{currentUserId}
GROUP BY c.name
HAVING SUM(od.qty) > 10     -- filter groups after aggregation
```

| Keyword | When it runs | What it filters |
|---------|-------------|-----------------|
| `WHERE` | Before GROUP BY | Individual rows |
| `HAVING` | After GROUP BY | Aggregated groups |

---

## 8. Subqueries

A query inside another query.

### Pattern 1: Scalar subquery in SELECT — one value per row

```sql
-- NotificationMapper.xml — look up notification type name inline
SELECT id,
       (SELECT name FROM tb_notification_type WHERE id = type_id) AS notificationType,
       order_id,
       content
FROM tb_buyer_notification
WHERE buyer_id = #{currentUserId}

-- BuyerOrderMapper.xml — inline lookup for store name and image
SELECT id,
       store_id AS storeId,
       (SELECT name FROM tb_store WHERE id = tb_order.store_id)         AS storeName,
       (SELECT banner_image FROM tb_store WHERE id = tb_order.store_id) AS storeImage,
       (SELECT SUM(unit_price) FROM tb_order_detail WHERE order_id = tb_order.id) AS total
FROM tb_order
WHERE status_id = 9 AND buyer_account_id = #{currentUserId}
```

Each row of the outer query runs the inner query once. Clean substitute for JOIN when you need just one value.

### Pattern 2: IN (subquery) — filter by a list from another table

```sql
-- StoreMapper.xml — only stores that the user bookmarked
SELECT ... FROM tb_store
WHERE is_publish = TRUE
  AND tb_store.id IN (
      SELECT DISTINCT store_id FROM tb_bookmark
      WHERE buyer_account_id = #{currentUser}
  )

-- CategoryMapper.xml — search categories by name using IN
SELECT * FROM tb_store_category
WHERE store_id = #{storeId}
  AND category_id IN (
      SELECT id FROM tb_category WHERE name ILIKE CONCAT('%', #{name}, '%')
  )

-- StoreMapper.xml — stores that have a product matching a name
WHERE id IN (
    SELECT store_id FROM tb_store_product_detail
    WHERE product_id IN (
        SELECT id FROM tb_product WHERE name ILIKE '%${name}%'
    )
)
```

Nested IN is common in Korean SI. Read from the innermost query outward.

### Pattern 3: EXISTS — check if at least one row matches

```sql
-- Syntax: returns true/false
SELECT EXISTS(SELECT 1 FROM table WHERE condition)

-- AppUserMapper.xml — check if phone is already used
SELECT EXISTS(SELECT 1 FROM tb_supplier_phone WHERE phone_number = #{phone})

-- StoreMapper.xml — check if user already bookmarked a store
SELECT EXISTS(
    SELECT * FROM tb_bookmark
    WHERE store_id = #{storeId} AND buyer_account_id = #{currentUserId}
)

-- SupplierProductMapper.xml — check if a product is in any order
SELECT EXISTS(
    SELECT * FROM tb_order x
        JOIN tb_order_detail y ON x.id = y.order_id
    WHERE store_product_id = #{productId}
      AND store_id = (SELECT id FROM tb_store WHERE supplier_account_id = #{currentUserId})
)
```

`EXISTS` is faster than `COUNT(*) > 0` because it stops as soon as it finds the first match.

### Pattern 4: Correlated subquery — inner query references outer row

```sql
-- StoreMapper.xml — rating for each store in the list
SELECT tb_store.id,
       name,
       (SELECT coalesce(avg(rated_star), 0)
        FROM tb_rating_detail
        WHERE store_id = tb_store.id)     -- ← tb_store.id comes from the OUTER query
           AS rating
FROM tb_store
WHERE is_publish = TRUE
```

The inner query runs once per outer row, using the outer row's value. This is called "correlated" because the inner query is correlated with the outer query.

### Pattern 5: Subquery in FROM clause

```sql
-- SupplierProductMapper.xml — COALESCE with subquery
SELECT COALESCE((SELECT id FROM tb_product WHERE name ILIKE '${name}'), 0)
```

---

## 9. INSERT, UPDATE, DELETE

### INSERT — add new rows

```sql
-- Basic syntax
INSERT INTO table_name (col1, col2) VALUES (val1, val2)

-- AppUserMapper.xml — insert supplier and return the full new row
INSERT INTO tb_supplier_account
VALUES (DEFAULT, #{user.roleId}, #{user.email}, #{user.password}, DEFAULT, DEFAULT, DEFAULT, DEFAULT)
RETURNING id, email, password,
          (SELECT name FROM tb_role WHERE id = role_id) AS role,
          role_id, is_verified, is_active

-- CategoryMapper.xml — insert category and return just the id
INSERT INTO tb_category(name) VALUES (#{category.name}) RETURNING *

-- BuyerProfileMapper.xml — insert profile and return new id
INSERT INTO tb_buyer_info
VALUES (DEFAULT, #{currentUserId}, #{re.firstName}, #{re.lastName}, #{re.gender},
        #{re.address}, #{re.primaryPhoneNumber}, #{re.profileImage}, DEFAULT, DEFAULT)
RETURNING id

-- StoreMapper.xml — insert store and return full row for display
INSERT INTO tb_store (id, supplier_account_id, name, description, created_date, updated_date,
                      is_publish, is_active, banner_image, address, phone)
VALUES (DEFAULT, #{currentUserId}, #{store.name}, #{store.description}, DEFAULT, DEFAULT,
        true, DEFAULT, #{store.bannerImage}, #{store.address}, #{store.primaryPhone})
RETURNING id, name, address, banner_image AS bannerImage, ...
```

**`DEFAULT`** = let PostgreSQL use the column's default value (usually auto-increment for id, NOW() for timestamps, false for booleans).

**`RETURNING`** = PostgreSQL-only. Get data back immediately after insert without a second SELECT query.

### UPDATE — change existing rows

```sql
-- Basic syntax
UPDATE table_name SET col1 = val1, col2 = val2 WHERE condition

-- AppUserMapper.xml — change password
UPDATE tb_supplier_account
SET password = #{newPassword}
WHERE email = #{email}
RETURNING *

-- StoreMapper.xml — update all store fields
UPDATE tb_store
SET name         = #{store.name},
    description  = #{store.description},
    updated_date = NOW(),              -- use NOW() to auto-set current timestamp
    is_publish   = #{store.isPublish},
    banner_image = #{store.bannerImage},
    address      = #{store.address},
    phone        = #{store.primaryPhone}
WHERE id = #{storeId}
RETURNING id, name, address, banner_image AS bannerImage, ...

-- SupplierProductMapper.xml — add to existing qty (not replace)
UPDATE tb_store_product_detail
SET qty   = qty + #{qty},    -- ← add to current value, don't replace it
    price = #{price}
WHERE store_id = #{storeId} AND id = #{productId}

-- SupplierOrderMapper.xml — deduct stock when order is accepted
UPDATE tb_store_product_detail
SET qty = qty - (
    SELECT qty FROM tb_order_detail
    WHERE order_id = #{orderId}
      AND tb_order_detail.store_product_id = #{productId}
)
WHERE id = (
    SELECT store_product_id FROM tb_order_detail
    WHERE order_id = #{orderId}
      AND tb_order_detail.store_product_id = #{productId}
)
AND store_id = #{storeId}

-- Status transitions (order flow)
UPDATE tb_order SET status_id = 2 WHERE id = #{orderId} RETURNING 1  -- accept
UPDATE tb_order SET status_id = 8 WHERE id = #{orderId} RETURNING 1  -- decline
UPDATE tb_order SET status_id = 3 WHERE id = #{orderId} RETURNING 1  -- dispatch
UPDATE tb_order SET status_id = 6 WHERE id = #{orderId} RETURNING 1  -- delivered
```

### DELETE — remove rows

```sql
-- Basic syntax
DELETE FROM table_name WHERE condition

-- BuyerProfileMapper.xml — delete phone numbers before re-inserting
DELETE FROM tb_buyer_phone WHERE buyer_info_id = #{buyerInfoId}

-- StoreMapper.xml — delete store permanently
DELETE FROM tb_store WHERE supplier_account_id = #{currentUserId} RETURNING 1

-- BuyerOrderMapper.xml — cancel cart
DELETE FROM tb_order WHERE id = #{orderId} AND status_id = 9 RETURNING 1

-- CategoryMapper.xml — delete category from store
DELETE FROM tb_store_category
WHERE category_id = #{id} AND store_id = #{storeId}
RETURNING category_id
```

**`RETURNING 1`** is a common pattern — return the number `1` just to confirm the operation happened (service layer checks if result is not null).

---

## 10. Pagination — LIMIT / OFFSET

Every list API uses pagination. This is 페이징 in Korean SI.

```sql
-- Syntax
SELECT ... FROM table
ORDER BY column
LIMIT  {how many rows to return}
OFFSET {how many rows to skip}
```

### Formula:
```
OFFSET = pageSize * (pageNumber - 1)
```

| pageNumber | pageSize | OFFSET | Rows returned |
|------------|----------|--------|---------------|
| 1 | 10 | 0 | rows 1–10 |
| 2 | 10 | 10 | rows 11–20 |
| 3 | 10 | 20 | rows 21–30 |
| 1 | 5  | 0  | rows 1–5 |
| 2 | 5  | 5  | rows 6–10 |

### From your project:

```sql
-- CategoryMapper.xml — paginated category list
SELECT * FROM tb_store_category
WHERE store_id = #{storeId}
ORDER BY category_id ASC
LIMIT #{pageSize}
OFFSET #{pageSize} * (#{pageNumber} - 1)

-- SupplierProductMapper.xml — paginated product list with sort
SELECT a.id, b.name, a.qty, a.price, a.image
FROM tb_store_product_detail a
    JOIN tb_product b ON a.product_id = b.id
WHERE a.store_id = #{storeId}
ORDER BY ${by} ASC         -- ${by} = column name (dynamic)
LIMIT #{pageSize}
OFFSET #{pageSize} * (#{pageNumber} - 1)

-- CategoryMapper.xml — paginated category search
SELECT * FROM tb_store_category
WHERE store_id = #{storeId}
  AND category_id IN (SELECT id FROM tb_category WHERE name ILIKE CONCAT('%', #{name}, '%'))
ORDER BY category_id ASC
LIMIT #{pageSize}
OFFSET #{pageSize} * (#{pageNumber} - 1)
```

### Always pair pagination with a count query:

```sql
-- The data query
SELECT ... FROM tb_store WHERE is_publish = true
LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} - 1)

-- The count query (tells frontend total pages)
SELECT count(*) FROM tb_store WHERE is_publish = true

-- Frontend calculates:  totalPages = CEIL(totalCount / pageSize)
```

---

## 11. PostgreSQL-Specific Features

These are features that only exist in PostgreSQL (not MySQL or Oracle).

### ILIKE — case-insensitive LIKE search

```sql
-- LIKE (case-sensitive) — matches "Apple" but not "apple"
WHERE name LIKE '%Apple%'

-- ILIKE (case-insensitive) — matches "Apple", "apple", "APPLE", "ApPlE"
WHERE name ILIKE '%apple%'

-- SupplierProductMapper.xml
WHERE name ILIKE '%${name}%'

-- StoreMapper.xml
WHERE is_publish = TRUE AND name ILIKE '%${name}%'

-- CategoryMapper.xml — search with CONCAT to build the pattern
WHERE name ILIKE CONCAT('%', #{name}, '%')
```

`CONCAT('%', #{name}, '%')` is safer than `'%${name}%'` because `#{name}` is parameterized binding. Use this when possible.

### EXTRACT — get year or month from a date

```sql
-- Syntax
EXTRACT(YEAR  FROM date_column)
EXTRACT(MONTH FROM date_column)
EXTRACT(DAY   FROM date_column)

-- BuyerReportMapper.xml — filter orders by specific year and month
WHERE o.buyer_account_id = #{currentUserId}
  AND EXTRACT(YEAR  FROM o.created_date) = #{year}
  AND EXTRACT(MONTH FROM o.created_date) = #{month}

-- Only filter by year (no month — for yearly reports)
WHERE o.buyer_account_id = #{currentUserId}
  AND EXTRACT(YEAR FROM o.created_date) = #{year}
```

### date_trunc — truncate date to a unit

```sql
-- SupplierReportMapper.xml — truncate to month (strip day/time)
SELECT date_trunc('month', a.created_date)  -- returns: 2025-01-01 00:00:00
FROM tb_order a
    JOIN tb_order_detail b ON a.id = b.order_id
WHERE a.store_id = #{storeId}
```

`date_trunc('month', '2025-03-15')` → `2025-03-01 00:00:00` — useful for grouping by month.

### BETWEEN — range filter (inclusive)

```sql
-- SupplierReportMapper.xml — filter by date range
WHERE b.created_date BETWEEN '${startDate}' AND '${endDate}'
-- Equivalent to: WHERE created_date >= startDate AND created_date <= endDate
```

### NOW() — current timestamp

```sql
-- StoreMapper.xml — auto-set updated_date when editing
UPDATE tb_store
SET name         = #{store.name},
    updated_date = NOW()           -- current timestamp at query execution time
WHERE id = #{storeId}

-- NotificationMapper.xml — set created_date on insert
INSERT INTO tb_buyer_notification (id, buyer_id, type_id, content, is_read, created_date)
VALUES (DEFAULT, #{param1}, #{param2}, #{param4}, #{param7}, NOW())
```

### CONCAT — join strings together

```sql
-- BuyerProfileMapper.xml — combine first and last name
SELECT concat(first_name, ' ', last_name) FROM tb_buyer_info
WHERE buyer_account_id = #{buyerId}
-- Result: "Hen Heang"

-- CategoryMapper.xml — build ILIKE pattern safely
WHERE name ILIKE CONCAT('%', #{name}, '%')
```

### DISTINCT — remove duplicate rows

```sql
-- CategoryMapper.xml — unique categories for a store (product may repeat categories)
SELECT DISTINCT A.id, A.name, A.updated_date, A.created_date
FROM tb_category A
    INNER JOIN tb_store_product_detail B ON A.id = B.category_id
WHERE store_id = #{storeId}

-- StoreMapper.xml — count unique stores bookmarked
SELECT count(DISTINCT store_id) FROM tb_bookmark WHERE buyer_account_id = #{buyerId}
```

---

## 12. Dynamic SQL — MyBatis XML Tags

동적 SQL = SQL that changes based on what parameters are passed. This is a core skill in Korean SI.

### `<sql>` + `<include>` — reusable SQL fragment

Define once, reuse everywhere. Like a Java method for SQL.

```xml
<!-- Define the fragment (SupplierOrderMapper.xml) -->
<sql id="selectOrderBase">
    SELECT id, store_id AS storeId, buyer_account_id AS buyerId,
           created_date AS date, status_id
    FROM tb_order
</sql>

<sql id="paginate">
    LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} - 1)
</sql>

<!-- Reuse the fragment -->
<select id="getAllOrders" resultMap="OrderResultMap">
    <include refid="selectOrderBase"/>
    WHERE store_id = #{storeId} AND status_id IN (1, 2, 3, 4, 5)
    <include refid="paginate"/>
</select>

<select id="getPendingOrders" resultMap="OrderResultMap">
    <include refid="selectOrderBase"/>
    WHERE store_id = #{storeId} AND status_id = 1
    <include refid="paginate"/>
</select>
```

Without `<sql>/<include>`, you would copy-paste the SELECT and LIMIT lines into every query.

### `<choose>` / `<when>` / `<otherwise>` — if/else logic

```xml
<!-- SupplierOrderMapper.xml — sort direction based on parameter -->
<sql id="orderByDate">
    ORDER BY created_date
    <choose>
        <when test="sort != null and sort.equalsIgnoreCase('asc')">ASC</when>
        <otherwise>DESC</otherwise>
    </choose>
</sql>
```

Equivalent Java:
```java
if (sort != null && sort.equalsIgnoreCase("asc")) {
    sql += "ASC";
} else {
    sql += "DESC";
}
```

Used in the actual queries:
```xml
<select id="getImportHistory" resultMap="ImportHistoryResultMap">
    SELECT ...
    FROM tb_product_import_detail JOIN tb_product_import ...
    WHERE store_id = #{storeId}
    <include refid="orderByDate"/>     <!-- ← sort direction is dynamic -->
    <include refid="paginate"/>
</select>
```

### `<if>` — include SQL only when a condition is true

Your project doesn't use `<if>` yet, but this is the most common dynamic SQL tag in Korean SI. Here's how it would work with your tables:

```xml
<!-- Search products with optional filters -->
<select id="searchProducts" resultMap="ProductWithCategoryResultMap">
    SELECT a.id, b.name, a.qty, a.price, a.image
    FROM tb_store_product_detail a
        JOIN tb_product b ON a.product_id = b.id
    WHERE a.store_id = #{storeId}
    <if test="name != null and name != ''">
        AND b.name ILIKE CONCAT('%', #{name}, '%')
    </if>
    <if test="categoryId != null">
        AND a.category_id = #{categoryId}
    </if>
    <if test="minPrice != null">
        AND a.price >= #{minPrice}
    </if>
    <if test="maxPrice != null">
        AND a.price &lt;= #{maxPrice}
    </if>
</select>
```

Without `<if>`:
- You need a separate query for every combination of filters
- 3 optional filters = 8 different queries (2³)

With `<if>`:
- One query handles all combinations

**XML special characters inside `<if test="...">` conditions:**

| Character | XML escape | Meaning |
|-----------|-----------|---------|
| `<` | `&lt;` | less than |
| `>` | `&gt;` | greater than |
| `&` | `&amp;` | and |
| `"` | `&quot;` | double quote |

### `<where>` — smart WHERE clause

`<where>` automatically adds `WHERE` keyword and removes leading `AND`/`OR`.

```xml
<!-- Without <where> — breaks if no conditions are true -->
<select id="searchStore">
    SELECT * FROM tb_store
    WHERE                           <!-- ← SQL error if nothing follows! -->
    <if test="name != null">AND name ILIKE '%${name}%'</if>
    <if test="isPublish != null">AND is_publish = #{isPublish}</if>
</select>

<!-- With <where> — safe even when all conditions are false -->
<select id="searchStore">
    SELECT * FROM tb_store
    <where>
        <if test="name != null">AND name ILIKE '%${name}%'</if>
        <if test="isPublish != null">AND is_publish = #{isPublish}</if>
    </where>
</select>
<!-- If both are null: no WHERE clause at all (returns all stores) -->
<!-- If only name is set: WHERE name ILIKE '...' (no leading AND) -->
```

### `<set>` — smart SET clause for UPDATE

Same concept as `<where>` but for UPDATE statements.

```xml
<!-- Update only the fields that are provided -->
<update id="updateStore">
    UPDATE tb_store
    <set>
        <if test="name != null">name = #{name},</if>
        <if test="description != null">description = #{description},</if>
        <if test="bannerImage != null">banner_image = #{bannerImage},</if>
        updated_date = NOW()
    </set>
    WHERE id = #{storeId}
</update>
```

### `#{}` vs `${}` — parameterized vs raw injection

| Syntax | What it does | Use for |
|--------|-------------|---------|
| `#{value}` | Becomes a `?` in PreparedStatement — safe from SQL injection | Values: id, name, phone, password |
| `${value}` | Directly inserted as raw string — NOT safe if user-supplied | Column names, ORDER BY direction |

```xml
<!-- SAFE — values use #{} -->
WHERE store_id = #{storeId}
  AND name = #{name}
  AND price > #{minPrice}

<!-- NECESSARY — column name uses ${} because column names can't be bound as ? -->
ORDER BY ${by} ASC     <!-- by = "price" or "name" or "created_date" -->
LIMIT #{pageSize}      <!-- pageSize value still uses #{} -->

-- SupplierProductMapper.xml
ORDER BY ${by} ASC
LIMIT #{pageSize}
OFFSET #{pageSize} * (#{pageNumber} - 1)

-- SearchStoreMapper.xml — raw list for IN clause
WHERE id IN (${combinedList})  <!-- combinedList = "1,2,3,5" built in service layer -->
```

**Rule:** If the value comes from user input (request body, query params), always use `#{}`. Only use `${}` for structural SQL parts (column names, sort directions) that you control and validate in the service layer.

---

## 13. ResultMap — Mapping Nested Objects

결과 매핑 is how MyBatis maps DB columns to Java object fields, including nested objects.

### Basic `<resultMap>` — flat object

```xml
<!-- AppUserMapper.xml — simple flat mapping -->
<resultMap id="AppUserResultMap" type="com.henheang.hphsar.model.appUser.AppUser">
    <id     property="id"         column="id"/>            <!-- primary key -->
    <result property="email"      column="email"/>
    <result property="password"   column="password"/>
    <result property="roleId"     column="role_id"/>        <!-- snake_case → camelCase -->
    <result property="isVerified" column="is_verified"/>
    <result property="isActive"   column="is_active"/>
</resultMap>
```

- `<id>` = primary key column (helps MyBatis performance)
- `<result>` = all other columns
- `property` = Java field name
- `column` = DB column name

### `<association>` — one nested object (one-to-one)

```xml
<!-- SupplierProductMapper.xml — Product contains a Category -->
<resultMap id="ProductWithCategoryResultMap" type="...Product">
    <id     property="id"   column="id"/>
    <result property="name" column="name"/>
    <result property="qty"  column="qty"/>
    <!-- After loading the product row, call getCategoryByCategoryId
         with the value from column "category_id" and put the result in product.category -->
    <association property="category"
                 column="category_id"
                 select="getCategoryByCategoryId"/>
</resultMap>

<!-- The sub-select called by <association> -->
<select id="getCategoryByCategoryId" resultMap="CategoryResultMap">
    SELECT id, name, created_date AS createdDate, updated_date AS updatedDate
    FROM tb_category WHERE id = #{id}
</select>
```

Java class relationship:
```java
class Product {
    Integer id;
    String name;
    Category category;   // ← this single nested object is loaded via <association>
}
```

### `<collection>` — a List of nested objects (one-to-many)

```xml
<!-- StoreMapper.xml — Store contains a List<String> additionalPhone -->
<resultMap id="StoreResultMap" type="...Store">
    <id     property="id"   column="id"/>
    <result property="name" column="name"/>
    <!-- After loading the store row, call getAdditionalPhone with store id
         and put the result List<String> into store.additionalPhone -->
    <collection property="additionalPhone"
                column="id"
                select="getAdditionalPhone"/>
</resultMap>

<!-- The sub-select called by <collection> -->
<select id="getAdditionalPhone" resultType="String">
    SELECT phone FROM tb_store_phone WHERE store_id = #{storeId}
</select>
```

Java class relationship:
```java
class Store {
    Integer id;
    String name;
    List<String> additionalPhone;   // ← this List is loaded via <collection>
}
```

### Combining `<association>` and `<collection>` in one resultMap

```xml
<!-- StoreMapper.xml — StoreBuyer has rating (Double), ratingCount (Integer),
     additionalPhone (List<String>), and categories (List<Category>) -->
<resultMap id="StoreBuyerResultMap" type="...StoreBuyer">
    <id     property="id"   column="id"/>
    <result property="name" column="name"/>
    <!-- Single values via association -->
    <association property="rating"       column="id" select="getRating"/>
    <association property="ratingCount"  column="id" select="getRatingCount"/>
    <!-- Lists via collection -->
    <collection property="additionalPhone" column="id" select="getAdditionalPhone"/>
    <collection property="categories"      column="id" select="getCategoryListingByStoreId"/>
</resultMap>
```

### Cross-mapper sub-select — calling another mapper's method

```xml
<!-- SupplierOrderMapper.xml — order result loads buyer name from BuyerProfileMapper -->
<resultMap id="OrderResultMap" type="...Order">
    <id property="id" column="id"/>
    <!-- Calls getBuyerNameById in BuyerProfileRepository -->
    <association property="name"
                 column="buyerId"
                 select="com.henheang.hphsar.repository.BuyerProfileRepository.getBuyerNameById"/>
    <!-- Calls getStoreImageById in StoreRepository -->
    <association property="storeImage"
                 column="storeId"
                 select="com.henheang.hphsar.repository.StoreRepository.getStoreImageById"/>
    <!-- Calls getAdditionalPhone in StoreRepository — returns a List<String> -->
    <collection property="storeAdditionalPhone"
                column="storeId"
                select="com.henheang.hphsar.repository.StoreRepository.getAdditionalPhone"/>
</resultMap>
```

Full path format: `package.RepositoryInterface.methodId`

### resultMap vs resultType — when to use which:

| Use | When |
|-----|------|
| `resultType="int"` | Query returns a single number |
| `resultType="boolean"` | Query returns true/false |
| `resultType="String"` | Query returns a single text value |
| `resultType="double"` | Query returns a decimal number |
| `resultMap="..."` | Query returns a full Java object with field mappings |

---

## 14. Practice Exercises

Write these queries yourself using your real tables. Answers are at the bottom.

### Level 1 — Basic

**Q1.** Get all stores where `is_publish = true` and `is_active = true`.

**Q2.** Get the email and `is_verified` status for all buyers in `tb_buyer_account`.

**Q3.** Count how many products are in store with id = 5 (`tb_store_product_detail`).

**Q4.** Get all orders for buyer id = 3 where status is PENDING (status_id = 1).

---

### Level 2 — JOIN

**Q5.** Get each store's name along with the supplier's email. (JOIN `tb_store` with `tb_supplier_account`)

**Q6.** Get all products in store id = 5, showing the product name (from `tb_product`) and price (from `tb_store_product_detail`).

**Q7.** Get all completed orders (status_id = 6) with the buyer's email address.

---

### Level 3 — Aggregation

**Q8.** Get the total number of orders per store. Show store_id and order count.

**Q9.** Get the average rating for each store. Show store id and avg_rating. Use COALESCE so stores with no rating show 0.

**Q10.** Get the total revenue (qty * unit_price) for each completed order. Show order_id and total.

---

### Level 4 — Subqueries

**Q11.** Get all stores that have at least one product with qty = 0. (Use EXISTS or IN)

**Q12.** Get all buyers who have never placed an order. (Use NOT EXISTS or NOT IN)

**Q13.** For each buyer, show their full name (from `tb_buyer_info`) and their total number of orders.

---

### Level 5 — Pagination

**Q14.** Get stores page 2, 5 stores per page, ordered by name ASC.

**Q15.** Get the count of all published stores (for calculating total pages).

---

### Level 6 — Dynamic SQL (MyBatis)

**Q16.** Write a `<select>` with `<if>` that searches products by optional `name` and optional `categoryId`.

**Q17.** Write an `<sql>` fragment for `ORDER BY created_date` with `<choose>` for ASC/DESC, then `<include>` it in two different queries.

---

### Answers

<details>
<summary>Click to see answers</summary>

**Q1.**
```sql
SELECT * FROM tb_store WHERE is_publish = true AND is_active = true
```

**Q2.**
```sql
SELECT email, is_verified FROM tb_buyer_account
```

**Q3.**
```sql
SELECT count(*) FROM tb_store_product_detail WHERE store_id = 5
```

**Q4.**
```sql
SELECT * FROM tb_order WHERE buyer_account_id = 3 AND status_id = 1
```

**Q5.**
```sql
SELECT s.name AS storeName, d.email AS supplierEmail
FROM tb_store s
    JOIN tb_supplier_account d ON s.supplier_account_id = d.id
```

**Q6.**
```sql
SELECT p.name AS productName, spd.price
FROM tb_store_product_detail spd
    JOIN tb_product p ON spd.product_id = p.id
WHERE spd.store_id = 5
```

**Q7.**
```sql
SELECT o.id AS orderId, ra.email AS buyerEmail
FROM tb_order o
    JOIN tb_buyer_account ra ON o.buyer_account_id = ra.id
WHERE o.status_id = 6
```

**Q8.**
```sql
SELECT store_id, count(*) AS orderCount
FROM tb_order
GROUP BY store_id
ORDER BY orderCount DESC
```

**Q9.**
```sql
SELECT id AS storeId, COALESCE(AVG(rated_star), 0) AS avgRating
FROM tb_store
    LEFT JOIN tb_rating_detail rd ON tb_store.id = rd.store_id
GROUP BY tb_store.id
```

**Q10.**
```sql
SELECT order_id, SUM(qty * unit_price) AS total
FROM tb_order_detail od
    JOIN tb_order o ON od.order_id = o.id
WHERE o.status_id = 6
GROUP BY order_id
```

**Q11.**
```sql
SELECT * FROM tb_store s
WHERE EXISTS (
    SELECT 1 FROM tb_store_product_detail spd
    WHERE spd.store_id = s.id AND spd.qty = 0
)
```

**Q12.**
```sql
SELECT * FROM tb_buyer_account ra
WHERE NOT EXISTS (
    SELECT 1 FROM tb_order o
    WHERE o.buyer_account_id = ra.id
)
```

**Q13.**
```sql
SELECT concat(ri.first_name, ' ', ri.last_name) AS fullName,
       count(o.id) AS totalOrders
FROM tb_buyer_info ri
    LEFT JOIN tb_order o ON ri.buyer_account_id = o.buyer_account_id
GROUP BY ri.id, ri.first_name, ri.last_name
```

**Q14.**
```sql
SELECT * FROM tb_store
WHERE is_publish = true
ORDER BY name ASC
LIMIT 5 OFFSET 5 * (2 - 1)
-- OFFSET = 5 * 1 = 5 (skip page 1)
```

**Q15.**
```sql
SELECT count(*) FROM tb_store WHERE is_publish = true
```

**Q16.**
```xml
<select id="searchProducts" resultMap="ProductWithCategoryResultMap">
    SELECT a.id, b.name, a.qty, a.price, a.image
    FROM tb_store_product_detail a
        JOIN tb_product b ON a.product_id = b.id
    <where>
        <if test="storeId != null">AND a.store_id = #{storeId}</if>
        <if test="name != null and name != ''">
            AND b.name ILIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="categoryId != null">AND a.category_id = #{categoryId}</if>
    </where>
</select>
```

**Q17.**
```xml
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

<select id="getOrderHistory" resultMap="OrderHistoryResultMap">
    SELECT id, store_id, buyer_account_id, created_date AS date, status_id
    FROM tb_order
    WHERE store_id = #{storeId} AND status_id IN (5, 6)
    <include refid="orderByDate"/>
    <include refid="paginate"/>
</select>

<select id="getImportHistory" resultMap="ImportHistoryResultMap">
    SELECT ... FROM tb_product_import_detail
    JOIN tb_product_import tpi ON tpi.id = tb_product_import_detail.product_import_id
    WHERE store_id = #{storeId}
    <include refid="orderByDate"/>
    <include refid="paginate"/>
</select>
```

</details>

---

## Quick Reference Card

### SQL clauses order (always in this order):

```sql
SELECT    columns
FROM      table
JOIN      other_table ON condition
WHERE     filter_rows
GROUP BY  column
HAVING    filter_groups
ORDER BY  column ASC/DESC
LIMIT     n
OFFSET    n
```

### MyBatis dynamic SQL cheatsheet:

```xml
<sql id="name">  ...reusable SQL fragment...  </sql>
<include refid="name"/>

<if test="param != null">  ...SQL...  </if>

<choose>
    <when test="condition">  ...SQL...  </when>
    <otherwise>  ...SQL...  </otherwise>
</choose>

<where>  <if> conditions </if>  </where>   <!-- auto adds WHERE, strips leading AND -->
<set>    <if> assignments </if>  </set>    <!-- auto adds SET, strips trailing comma -->
```

### `#{}` vs `${}`:

```
#{value}  →  PreparedStatement ?  →  safe, always use for VALUES
${value}  →  raw string concat  →  only for column names / ORDER BY direction
```

### PostgreSQL special functions used in this project:

```sql
RETURNING id          -- get back inserted/updated/deleted row data
NOW()                 -- current timestamp
COALESCE(val, 0)      -- replace NULL with 0
EXISTS(SELECT 1 ...)  -- true if any row matches
ILIKE '%text%'        -- case-insensitive search
EXTRACT(YEAR FROM d)  -- get year from date
EXTRACT(MONTH FROM d) -- get month from date
date_trunc('month', d)-- truncate to month
BETWEEN a AND b       -- range filter (inclusive)
CONCAT(a, b, c)       -- join strings
DISTINCT              -- remove duplicate rows
DEFAULT               -- use column default value on INSERT
```
