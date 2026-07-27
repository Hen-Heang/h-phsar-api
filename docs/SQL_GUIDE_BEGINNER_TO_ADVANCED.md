# SQL Learning Guide — H-Phsar Project

> Beginner-to-advanced PostgreSQL and MyBatis guide using the H-Phsar marketplace domain.
>
> This version is designed for a beginner. It explains **what a query means, how to read it, why it works, common mistakes, and how the same SQL is used in the project**.
>
> Some examples are safer teaching versions of old project queries. They may not be exact copies of the current mapper files.

---

## What you will learn

By completing this guide, you should be able to:

1. Read a database table and understand primary keys and foreign keys.
2. Write `SELECT`, `WHERE`, `ORDER BY`, `LIMIT`, and `OFFSET` queries.
3. Join Supplier, Buyer, Store, Product, and Order data.
4. Build reports with `COUNT`, `SUM`, `AVG`, `GROUP BY`, and `HAVING`.
5. Safely write `INSERT`, `UPDATE`, and `DELETE` queries.
6. Use transactions so several writes succeed or fail together.
7. protect data with `NOT NULL`, `UNIQUE`, `CHECK`, and `FOREIGN KEY` constraints.
8. Create useful indexes and inspect queries with `EXPLAIN`.
9. Prevent overselling with atomic inventory updates.
10. Write safe MyBatis XML with `#{}`, `<if>`, `<where>`, `<choose>`, and `<foreach>`.
11. Understand `resultMap`, nested mapping, and the N+1 query problem.
12. Use CTEs and window functions for professional reports.

---

# Recommended learning order

Do not try to learn everything in one day.

| Level | Main knowledge | H-Phsar task you can solve |
|---|---|---|
| 0 | Tables, rows, columns, keys | Understand the database diagram |
| 1 | `SELECT`, `WHERE`, sorting | Find stores, users, and products |
| 2 | `JOIN` | Show product name with store price |
| 3 | Aggregation | Calculate sales and order totals |
| 4 | CRUD | Create and update project data |
| 5 | Transactions and constraints | Keep orders and stock correct |
| 6 | Subqueries and CTEs | Write complex business queries |
| 7 | Indexes and `EXPLAIN` | Make slow APIs faster |
| 8 | Concurrency and locking | Prevent inventory overselling |
| 9 | MyBatis | Connect safe SQL to Spring Boot |
| 10 | Window functions | Build rankings and monthly analytics |

A good rule is:

```text
Learn one concept
→ write three queries
→ run them in DBeaver
→ explain the result in your own words
→ find one mapper query using the concept
```

---

# Table of contents

1. [SQL mental model](#1-sql-mental-model)
2. [H-Phsar database map](#2-h-phsar-database-map)
3. [Basic SELECT](#3-basic-select)
4. [Filtering with WHERE](#4-filtering-with-where)
5. [NULL, Boolean, and three-valued logic](#5-null-boolean-and-three-valued-logic)
6. [Sorting and pagination](#6-sorting-and-pagination)
7. [Aliases and calculated columns](#7-aliases-and-calculated-columns)
8. [JOIN from beginner to professional](#8-join-from-beginner-to-professional)
9. [Aggregation and reports](#9-aggregation-and-reports)
10. [INSERT, UPDATE, DELETE, and RETURNING](#10-insert-update-delete-and-returning)
11. [Transactions](#11-transactions)
12. [Constraints and data integrity](#12-constraints-and-data-integrity)
13. [Subqueries, EXISTS, and CTEs](#13-subqueries-exists-and-ctes)
14. [Indexes and EXPLAIN](#14-indexes-and-explain)
15. [Dates and time ranges](#15-dates-and-time-ranges)
16. [Concurrency and safe inventory](#16-concurrency-and-safe-inventory)
17. [MyBatis safe SQL](#17-mybatis-safe-sql)
18. [MyBatis dynamic SQL](#18-mybatis-dynamic-sql)
19. [MyBatis resultMap and N+1](#19-mybatis-resultmap-and-n1)
20. [Window functions](#20-window-functions)
21. [Professional query patterns](#21-professional-query-patterns)
22. [Database design knowledge](#22-database-design-knowledge)
23. [Debugging checklist](#23-debugging-checklist)
24. [Practice path](#24-practice-path)
25. [Answers](#25-answers)
26. [Quick reference](#26-quick-reference)

---

# 1. SQL mental model

## 1.1 What is a database?

Think of a database as a collection of connected Excel sheets.

- A **table** is similar to one sheet.
- A **row** is one record.
- A **column** is one property of the record.
- A **primary key** identifies one row.
- A **foreign key** connects one table to another table.

Example table:

```text
tb_store
+----+---------------------+------------------+
| id | supplier_account_id | name             |
+----+---------------------+------------------+
| 1  | 10                  | Phnom Penh Supply|
| 2  | 11                  | Seoul Food Hub   |
+----+---------------------+------------------+
```

Here:

- `id` is the store primary key.
- `supplier_account_id` connects the store to `tb_supplier_account.id`.
- `name` contains the store name.

## 1.2 SQL is declarative

In Java, you normally describe **how** to do something step by step.

```java
for each store:
    if store is published:
        add store to result
```

In SQL, you describe **what result you want**:

```sql
SELECT id, name
FROM tb_store
WHERE is_publish = true;
```

PostgreSQL chooses the execution strategy.

## 1.3 Four main SQL operations

| Operation | SQL command | Meaning |
|---|---|---|
| Create | `INSERT` | Add rows |
| Read | `SELECT` | Read rows |
| Update | `UPDATE` | Change rows |
| Delete | `DELETE` | Remove rows |

This is called CRUD.

## 1.4 Safe learning environment

Practice against a local or test database.

Before running `UPDATE` or `DELETE`, first run the same condition as a `SELECT`:

```sql
SELECT *
FROM tb_order
WHERE id = 100;
```

Only after confirming the correct row:

```sql
UPDATE tb_order
SET updated_date = NOW()
WHERE id = 100;
```

Never practice destructive queries on a production database.

---

# 2. H-Phsar database map

## 2.1 Main project tables

```text
Authentication
├── tb_supplier_account
├── tb_buyer_account
└── tb_role

Supplier and Buyer profiles
├── tb_supplier_info
├── tb_supplier_phone
├── tb_buyer_info
└── tb_buyer_phone

Marketplace
├── tb_store
├── tb_store_phone
├── tb_category
├── tb_store_category
├── tb_product
└── tb_store_product_detail

Inventory imports
├── tb_product_import
└── tb_product_import_detail

Orders
├── tb_order
├── tb_order_detail
└── tb_status

Engagement
├── tb_bookmark
├── tb_rating_detail
├── tb_buyer_notification
├── tb_supplier_notification
└── tb_notification_type
```

## 2.2 Important relationships

```text
tb_supplier_account.id
        │
        └── tb_store.supplier_account_id
                  │
                  ├── tb_store_product_detail.store_id
                  └── tb_order.store_id

tb_buyer_account.id
        │
        └── tb_order.buyer_account_id
                  │
                  └── tb_order_detail.order_id

tb_product.id
        │
        └── tb_store_product_detail.product_id
                  │
                  └── tb_order_detail.store_product_id
```

Read the relationship aloud:

> A supplier account owns stores. A store lists store products. A buyer places an order at a store. An order contains order details. Each order detail references a store-specific product.

## 2.3 Never memorize status numbers blindly

Status IDs may change during project modernization. Inspect the real database:

```sql
SELECT id, name
FROM tb_status
ORDER BY id;
```

For learning examples, prefer joining by name:

```sql
SELECT o.id, s.name AS status
FROM tb_order o
JOIN tb_status s ON s.id = o.status_id;
```

This is easier to understand than seeing only `status_id = 3`.

## 2.4 Useful database inspection queries

List tables in the public schema:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

List columns for one table:

```sql
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'tb_order'
ORDER BY ordinal_position;
```

List constraints for one table:

```sql
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'tb_order'::regclass;
```

---

# 3. Basic SELECT

## 3.1 Read columns

Syntax:

```sql
SELECT column1, column2
FROM table_name;
```

Project example:

```sql
SELECT id, name
FROM tb_category;
```

Meaning:

```text
SELECT id, name  → return these columns
FROM tb_category → read from this table
```

## 3.2 `SELECT *`

```sql
SELECT *
FROM tb_category;
```

`*` means all columns.

It is useful while exploring a table, but production queries should usually list the required columns:

```sql
SELECT id, name, created_date
FROM tb_category;
```

Why avoid `SELECT *` in APIs?

- It may return private columns such as password hashes.
- It sends unnecessary data.
- A schema change can unexpectedly change the API result.
- It makes mapper behavior less clear.

## 3.3 DISTINCT

Use `DISTINCT` to remove duplicate result rows:

```sql
SELECT DISTINCT category_id
FROM tb_store_product_detail
WHERE store_id = 5;
```

Without `DISTINCT`, the same category may appear many times because several products use it.

## 3.4 Reading one value

```sql
SELECT role_id
FROM tb_supplier_account
WHERE email = #{email};
```

This normally returns one column from one row.

At the database console, replace MyBatis syntax with a test value:

```sql
SELECT role_id
FROM tb_supplier_account
WHERE email = 'supplier@example.com';
```

## 3.5 Semicolon

A semicolon ends a SQL statement:

```sql
SELECT id, name FROM tb_store;
```

Use it consistently in SQL scripts.

---

# 4. Filtering with WHERE

`WHERE` decides which rows are included.

```sql
SELECT id, name
FROM tb_store
WHERE is_publish = true;
```

## 4.1 Comparison operators

| Operator | Meaning | Example |
|---|---|---|
| `=` | equal | `status_id = 1` |
| `<>` or `!=` | not equal | `status_id <> 9` |
| `>` | greater than | `qty > 0` |
| `>=` | greater than or equal | `price >= 10` |
| `<` | less than | `price < 100` |
| `<=` | less than or equal | `rated_star <= 5` |

## 4.2 AND

Both conditions must be true:

```sql
SELECT id, name
FROM tb_store
WHERE is_publish = true
  AND is_active = true;
```

## 4.3 OR

At least one condition must be true:

```sql
SELECT id, status_id
FROM tb_order
WHERE status_id = 1
   OR status_id = 2;
```

## 4.4 Parentheses matter

Dangerous or unclear:

```sql
WHERE store_id = 5
  AND status_id = 1
   OR status_id = 2
```

PostgreSQL evaluates `AND` before `OR`. The query may include status `2` orders from other stores.

Clear and safe:

```sql
WHERE store_id = 5
  AND (status_id = 1 OR status_id = 2)
```

Even cleaner:

```sql
WHERE store_id = 5
  AND status_id IN (1, 2)
```

## 4.5 IN

```sql
SELECT id, status_id
FROM tb_order
WHERE status_id IN (1, 2, 3);
```

This is equivalent to:

```sql
WHERE status_id = 1
   OR status_id = 2
   OR status_id = 3
```

## 4.6 BETWEEN

```sql
WHERE price BETWEEN 10 AND 100
```

`BETWEEN` includes both ends:

```text
price >= 10 AND price <= 100
```

For timestamps, a half-open range is usually safer. See the dates chapter.

## 4.7 LIKE and ILIKE

`LIKE` performs pattern matching. `ILIKE` is PostgreSQL case-insensitive matching.

```sql
SELECT id, name
FROM tb_product
WHERE name ILIKE '%rice%';
```

Wildcards:

| Symbol | Meaning |
|---|---|
| `%` | zero or more characters |
| `_` | exactly one character |

Examples:

```sql
-- Starts with "Khmer"
WHERE name ILIKE 'Khmer%'

-- Ends with "Market"
WHERE name ILIKE '%Market'

-- Contains "food"
WHERE name ILIKE '%food%'
```

MyBatis-safe version:

```xml
WHERE name ILIKE CONCAT('%', #{name}, '%')
```

Never use user text directly with `${name}`.

## 4.8 EXISTS

Check whether at least one matching row exists:

```sql
SELECT EXISTS (
    SELECT 1
    FROM tb_bookmark
    WHERE store_id = 5
      AND buyer_account_id = 10
);
```

Result:

```text
true or false
```

Use `EXISTS` when your question is:

> Does at least one row exist?

Do not count every row when you only need yes/no.

---

# 5. NULL, Boolean, and three-valued logic

## 5.1 What is NULL?

`NULL` means missing, unknown, or not provided.

It is not:

- zero
- empty text
- false

Wrong:

```sql
WHERE profile_image = NULL
```

Correct:

```sql
WHERE profile_image IS NULL
```

And:

```sql
WHERE profile_image IS NOT NULL
```

## 5.2 Why `NULL = NULL` is not true

SQL uses three logical results:

```text
TRUE
FALSE
UNKNOWN
```

Because `NULL` means unknown, comparing two unknown values does not prove they are equal.

Use `IS NULL`, `IS NOT NULL`, or PostgreSQL's null-safe comparison:

```sql
WHERE value_a IS NOT DISTINCT FROM value_b
```

## 5.3 COALESCE

Return the first non-null value:

```sql
SELECT COALESCE(profile_image, 'default-profile.png')
FROM tb_buyer_info;
```

For an average rating:

```sql
SELECT COALESCE(AVG(rated_star), 0) AS average_rating
FROM tb_rating_detail
WHERE store_id = 5;
```

Note:

```sql
COUNT(*)
```

already returns `0` when no rows match. It does not need `COALESCE`.

## 5.4 Boolean columns

PostgreSQL boolean conditions can be written clearly:

```sql
WHERE is_active = true
```

or simply:

```sql
WHERE is_active
```

To include only false values and not null values:

```sql
WHERE is_publish = false
```

To treat null as false:

```sql
WHERE is_publish IS NOT TRUE
```

---

# 6. Sorting and pagination

## 6.1 ORDER BY

```sql
SELECT id, name, created_date
FROM tb_store
ORDER BY created_date DESC;
```

- `ASC`: smallest/oldest/A first.
- `DESC`: largest/newest/Z first.

Always use deterministic sorting for pagination:

```sql
ORDER BY created_date DESC, id DESC
```

The second column resolves ties.

## 6.2 LIMIT

```sql
SELECT id, name
FROM tb_store
ORDER BY id
LIMIT 10;
```

Return no more than ten rows.

## 6.3 OFFSET

```sql
SELECT id, name
FROM tb_store
ORDER BY id
LIMIT 10
OFFSET 20;
```

Skip twenty rows and return the next ten.

For one-based page numbers:

```text
OFFSET = pageSize × (pageNumber - 1)
```

| Page | Size | Offset |
|---:|---:|---:|
| 1 | 10 | 0 |
| 2 | 10 | 10 |
| 3 | 10 | 20 |

## 6.4 Always pair list and count queries

Data:

```sql
SELECT id, name
FROM tb_store
WHERE is_publish = true
ORDER BY id
LIMIT #{pageSize}
OFFSET #{pageSize} * (#{pageNumber} - 1);
```

Count:

```sql
SELECT COUNT(*)
FROM tb_store
WHERE is_publish = true;
```

## 6.5 Offset pagination limitation

Large offsets can be slow because PostgreSQL still needs to find and skip rows.

For very large datasets, use keyset pagination:

```sql
SELECT id, name, created_date
FROM tb_store
WHERE (created_date, id) < (#{lastCreatedDate}, #{lastId})
ORDER BY created_date DESC, id DESC
LIMIT #{pageSize};
```

Learn offset pagination first. Learn keyset pagination after you understand indexes.

## 6.6 Dynamic sorting security

Unsafe when `by` comes directly from a request:

```xml
ORDER BY ${by}
```

Safer MyBatis pattern:

```xml
<choose>
    <when test="sortBy == 'price'">ORDER BY spd.price</when>
    <when test="sortBy == 'name'">ORDER BY p.name</when>
    <otherwise>ORDER BY spd.id</otherwise>
</choose>
```

This creates a strict allowlist.

---

# 7. Aliases and calculated columns

## 7.1 Column alias

```sql
SELECT banner_image AS bannerImage
FROM tb_store;
```

This helps map PostgreSQL `snake_case` to Java `camelCase`.

## 7.2 Table alias

Long form:

```sql
SELECT tb_order.id, tb_order.created_date
FROM tb_order;
```

Short form:

```sql
SELECT o.id, o.created_date
FROM tb_order o;
```

Use meaningful aliases:

```text
o   = order
od  = order detail
s   = store
p   = product
spd = store product detail
b   = buyer
sup = supplier
```

## 7.3 Calculated column

```sql
SELECT qty,
       unit_price,
       qty * unit_price AS subtotal
FROM tb_order_detail;
```

The alias `subtotal` exists only in the query result unless stored in a real table column.

## 7.4 CASE expression

`CASE` is SQL's `if/else`:

```sql
SELECT id,
       qty,
       CASE
           WHEN qty = 0 THEN 'OUT_OF_STOCK'
           WHEN qty < 10 THEN 'LOW_STOCK'
           ELSE 'AVAILABLE'
       END AS stock_status
FROM tb_store_product_detail;
```

Use it for reporting and display logic. Keep important business constraints in the database and service layer too.

---

# 8. JOIN from beginner to professional

A join combines related rows from different tables.

## 8.1 INNER JOIN

Return rows that have a match on both sides:

```sql
SELECT s.id,
       s.name,
       a.email AS supplier_email
FROM tb_store s
JOIN tb_supplier_account a
  ON a.id = s.supplier_account_id;
```

Read it in this order:

1. Start from `tb_store`.
2. For each store, find the supplier account whose `id` matches `supplier_account_id`.
3. Return selected columns.

## 8.2 LEFT JOIN

Return every row from the left table, even when no right-side row exists:

```sql
SELECT s.id,
       s.name,
       b.id AS bookmark_id
FROM tb_store s
LEFT JOIN tb_bookmark b
  ON b.store_id = s.id
 AND b.buyer_account_id = #{buyerId}
WHERE s.is_publish = true;
```

This is a strong pattern for store listings:

- All published stores are returned.
- The current buyer's bookmark is included when it exists.
- Other buyers' bookmarks do not duplicate the result.

## 8.3 Why FULL OUTER JOIN is usually wrong here

A `FULL OUTER JOIN` returns unmatched rows from both sides.

For stores and bookmarks, you normally do not want orphan bookmarks with no store. You want all stores, with optional bookmark information. That means `LEFT JOIN`, not `FULL OUTER JOIN`.

## 8.4 Condition in ON versus WHERE

Correct for optional bookmark:

```sql
LEFT JOIN tb_bookmark b
  ON b.store_id = s.id
 AND b.buyer_account_id = #{buyerId}
```

Potentially wrong:

```sql
LEFT JOIN tb_bookmark b ON b.store_id = s.id
WHERE b.buyer_account_id = #{buyerId}
```

The `WHERE` condition removes rows where `b` is null, effectively turning the left join into an inner join.

## 8.5 Multiple joins

Get order items with product and store information:

```sql
SELECT o.id AS order_id,
       s.name AS store_name,
       p.name AS product_name,
       od.qty,
       od.unit_price,
       od.qty * od.unit_price AS subtotal
FROM tb_order o
JOIN tb_store s
  ON s.id = o.store_id
JOIN tb_order_detail od
  ON od.order_id = o.id
JOIN tb_store_product_detail spd
  ON spd.id = od.store_product_id
JOIN tb_product p
  ON p.id = spd.product_id
WHERE o.id = #{orderId};
```

## 8.6 Avoid accidental row multiplication

Suppose a store has:

- 3 phone numbers
- 4 categories

Joining both one-to-many tables directly can produce:

```text
3 × 4 = 12 rows
```

This is not always wrong, but it can break counts and sums.

Solutions:

- Aggregate one side before joining.
- Use separate queries.
- Use `COUNT(DISTINCT ...)` carefully.
- Use JSON aggregation for nested results.

## 8.7 Join practice method

Build complex joins one table at a time:

```sql
-- Step 1
SELECT * FROM tb_order o;

-- Step 2
SELECT *
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id;

-- Step 3
SELECT *
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id
JOIN tb_store_product_detail spd ON spd.id = od.store_product_id;
```

Check row counts at every step.

---

# 9. Aggregation and reports

Aggregate functions combine several rows into summary values.

## 9.1 COUNT

```sql
SELECT COUNT(*)
FROM tb_order
WHERE store_id = #{storeId};
```

Count distinct buyers:

```sql
SELECT COUNT(DISTINCT buyer_account_id)
FROM tb_order
WHERE store_id = #{storeId};
```

## 9.2 SUM

Order total:

```sql
SELECT COALESCE(SUM(qty * unit_price), 0) AS order_total
FROM tb_order_detail
WHERE order_id = #{orderId};
```

## 9.3 AVG

```sql
SELECT COALESCE(AVG(rated_star), 0) AS average_rating
FROM tb_rating_detail
WHERE store_id = #{storeId};
```

## 9.4 MIN and MAX

```sql
SELECT MIN(price) AS minimum_price,
       MAX(price) AS maximum_price
FROM tb_store_product_detail
WHERE store_id = #{storeId};
```

## 9.5 GROUP BY

Count orders per store:

```sql
SELECT store_id,
       COUNT(*) AS order_count
FROM tb_order
GROUP BY store_id
ORDER BY order_count DESC;
```

Every non-aggregated selected column normally belongs in `GROUP BY`:

```sql
SELECT s.id,
       s.name,
       COUNT(o.id) AS order_count
FROM tb_store s
LEFT JOIN tb_order o ON o.store_id = s.id
GROUP BY s.id, s.name;
```

## 9.6 WHERE versus HAVING

`WHERE` filters rows before grouping.

`HAVING` filters groups after aggregation.

```sql
SELECT store_id,
       COUNT(*) AS order_count
FROM tb_order
WHERE created_date >= #{startDate}
GROUP BY store_id
HAVING COUNT(*) >= 10;
```

Meaning:

1. Keep recent order rows.
2. Group them by store.
3. Keep stores with at least ten orders.

## 9.7 Conditional aggregation

Count several order statuses in one query:

```sql
SELECT store_id,
       COUNT(*) FILTER (WHERE st.name = 'PENDING') AS pending_count,
       COUNT(*) FILTER (WHERE st.name = 'PROCESSING') AS processing_count,
       COUNT(*) FILTER (WHERE st.name = 'COMPLETED') AS completed_count
FROM tb_order o
JOIN tb_status st ON st.id = o.status_id
WHERE o.store_id = #{storeId}
GROUP BY store_id;
```

## 9.8 Revenue report

```sql
SELECT s.id AS store_id,
       s.name AS store_name,
       COALESCE(SUM(od.qty * od.unit_price), 0) AS revenue
FROM tb_store s
LEFT JOIN tb_order o
  ON o.store_id = s.id
LEFT JOIN tb_status st
  ON st.id = o.status_id
LEFT JOIN tb_order_detail od
  ON od.order_id = o.id
WHERE st.name = 'COMPLETED'
GROUP BY s.id, s.name
ORDER BY revenue DESC;
```

Be careful: the `WHERE st.name = ...` condition removes stores with no orders. To keep stores with zero revenue, put the status condition in the join:

```sql
LEFT JOIN tb_order o
  ON o.store_id = s.id
 AND o.status_id = (
     SELECT id FROM tb_status WHERE name = 'COMPLETED'
 )
```

---

# 10. INSERT, UPDATE, DELETE, and RETURNING

## 10.1 INSERT

Always list columns explicitly:

```sql
INSERT INTO tb_category (name, created_date, updated_date)
VALUES (#{name}, NOW(), NOW());
```

Why not `INSERT INTO table VALUES (...)`?

- Column order is easy to forget.
- Adding a new column may break the query.
- The query is harder to review.

## 10.2 RETURNING

PostgreSQL can return inserted data:

```sql
INSERT INTO tb_category (name, created_date, updated_date)
VALUES (#{name}, NOW(), NOW())
RETURNING id, name, created_date, updated_date;
```

This avoids a second `SELECT`.

## 10.3 UPDATE

```sql
UPDATE tb_store
SET name = #{name},
    description = #{description},
    updated_date = NOW()
WHERE id = #{storeId}
  AND supplier_account_id = #{supplierId};
```

The ownership condition is important.

## 10.4 Check affected rows

A MyBatis `<update>` mapper method can return the affected-row count:

```java
int updateStore(...);
```

Interpret it:

```text
1 → one row updated
0 → not found, wrong owner, or current state does not allow it
```

You usually do not need `RETURNING 1` just to know whether a row changed. `RETURNING 1` returns a constant result row; it is not the same idea as JDBC's affected-row count.

## 10.5 DELETE

```sql
DELETE FROM tb_bookmark
WHERE store_id = #{storeId}
  AND buyer_account_id = #{buyerId};
```

Every production `DELETE` should have a carefully reviewed `WHERE` condition.

Dangerous:

```sql
DELETE FROM tb_bookmark;
```

This removes every bookmark.

## 10.6 Soft delete

Instead of physically deleting important business data:

```sql
UPDATE tb_store
SET is_active = false,
    updated_date = NOW()
WHERE id = #{storeId}
  AND supplier_account_id = #{supplierId};
```

Soft delete is useful when you need history, audit, or recovery.

## 10.7 UPSERT with ON CONFLICT

Example: prevent duplicate bookmarks and insert safely:

```sql
INSERT INTO tb_bookmark (store_id, buyer_account_id)
VALUES (#{storeId}, #{buyerId})
ON CONFLICT (store_id, buyer_account_id) DO NOTHING;
```

This requires a unique constraint:

```sql
ALTER TABLE tb_bookmark
ADD CONSTRAINT uq_bookmark_store_buyer
UNIQUE (store_id, buyer_account_id);
```

---

# 11. Transactions

## 11.1 What is a transaction?

A transaction groups SQL operations into one unit.

```text
All operations succeed → COMMIT
One operation fails → ROLLBACK
```

Example:

```sql
BEGIN;

UPDATE tb_order
SET status_id = 2
WHERE id = 100;

INSERT INTO tb_buyer_notification (...)
VALUES (...);

COMMIT;
```

If the notification insert fails:

```sql
ROLLBACK;
```

The order status should return to its old value.

## 11.2 ACID in simple language

| Letter | Meaning | H-Phsar example |
|---|---|---|
| A | Atomicity | All order items are saved or none are saved |
| C | Consistency | Stock cannot become negative |
| I | Isolation | Concurrent orders do not corrupt each other |
| D | Durability | Committed order data survives restart |

## 11.3 Spring transaction boundary

Transactions normally belong in the service layer:

```java
@Transactional
public void acceptOrder(Integer orderId) {
    updateOrderStatus(orderId);
    deductAllItems(orderId);
    createNotification(orderId);
}
```

Do not place transaction orchestration in the controller.

## 11.4 Common transaction mistakes

1. Catching an exception and returning success.
2. Calling a transactional method from another method in the same class and expecting the proxy to start a transaction.
3. Performing external email sending and assuming it rolls back with the database.
4. Adding `@Transactional` to every read method without understanding why.
5. Manually deleting rows as rollback logic instead of using a transaction.

## 11.5 Savepoints

Advanced SQL can roll back part of a transaction:

```sql
BEGIN;

SAVEPOINT before_notification;

-- database changes

ROLLBACK TO SAVEPOINT before_notification;

COMMIT;
```

You rarely need manual savepoints in normal Spring service code. Learn standard transactions first.

---

# 12. Constraints and data integrity

Application validation is helpful. Database constraints are the final protection.

## 12.1 NOT NULL

```sql
ALTER TABLE tb_store_product_detail
ALTER COLUMN qty SET NOT NULL;
```

Use for values that must always exist.

## 12.2 CHECK

Prevent negative stock:

```sql
ALTER TABLE tb_store_product_detail
ADD CONSTRAINT chk_store_product_qty_non_negative
CHECK (qty >= 0);
```

Prevent invalid rating:

```sql
ALTER TABLE tb_rating_detail
ADD CONSTRAINT chk_rating_star_range
CHECK (rated_star BETWEEN 1 AND 5);
```

## 12.3 UNIQUE

One buyer should bookmark one store at most once:

```sql
ALTER TABLE tb_bookmark
ADD CONSTRAINT uq_bookmark_store_buyer
UNIQUE (store_id, buyer_account_id);
```

One buyer should rate one store at most once, when that is the business rule:

```sql
ALTER TABLE tb_rating_detail
ADD CONSTRAINT uq_rating_store_buyer
UNIQUE (store_id, buyer_account_id);
```

## 12.4 PRIMARY KEY

```sql
id INTEGER PRIMARY KEY
```

A primary key is unique and not null.

## 12.5 FOREIGN KEY

```sql
ALTER TABLE tb_order_detail
ADD CONSTRAINT fk_order_detail_order
FOREIGN KEY (order_id)
REFERENCES tb_order(id);
```

This prevents an order detail from referencing a nonexistent order.

## 12.6 ON DELETE behavior

| Rule | Meaning |
|---|---|
| `RESTRICT` | Do not delete the parent when children exist |
| `NO ACTION` | Similar protection, with transaction timing differences |
| `CASCADE` | Delete child rows automatically |
| `SET NULL` | Keep child row and clear its reference |

Use `CASCADE` carefully. Deleting one supplier could delete many stores, products, orders, and history rows.

## 12.7 Constraint-first thinking

Ask these questions for every table:

- Which columns must never be null?
- Which combination must be unique?
- Which numbers must have a valid range?
- Which row must reference another table?
- What should happen when the referenced row is deleted?

---

# 13. Subqueries, EXISTS, and CTEs

## 13.1 Scalar subquery

A scalar subquery returns one value:

```sql
SELECT o.id,
       (
           SELECT s.name
           FROM tb_store s
           WHERE s.id = o.store_id
       ) AS store_name
FROM tb_order o;
```

A join is often easier and may be more efficient:

```sql
SELECT o.id, s.name AS store_name
FROM tb_order o
JOIN tb_store s ON s.id = o.store_id;
```

## 13.2 IN subquery

```sql
SELECT id, name
FROM tb_store
WHERE id IN (
    SELECT store_id
    FROM tb_bookmark
    WHERE buyer_account_id = #{buyerId}
);
```

## 13.3 EXISTS correlated subquery

```sql
SELECT s.id, s.name
FROM tb_store s
WHERE EXISTS (
    SELECT 1
    FROM tb_store_product_detail spd
    WHERE spd.store_id = s.id
      AND spd.qty > 0
);
```

The inner query refers to `s.id` from the outer query.

## 13.4 NOT EXISTS

Find buyers with no submitted orders:

```sql
SELECT b.id, b.email
FROM tb_buyer_account b
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_order o
    JOIN tb_status st ON st.id = o.status_id
    WHERE o.buyer_account_id = b.id
      AND st.name <> 'CART'
);
```

`NOT EXISTS` is often safer than `NOT IN` when nulls may be present.

## 13.5 Common Table Expression: WITH

A CTE names a temporary query result:

```sql
WITH completed_orders AS (
    SELECT o.id, o.store_id
    FROM tb_order o
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
)
SELECT co.store_id,
       SUM(od.qty * od.unit_price) AS revenue
FROM completed_orders co
JOIN tb_order_detail od ON od.order_id = co.id
GROUP BY co.store_id;
```

Read it as:

1. First build `completed_orders`.
2. Then use it like a table in the main query.

## 13.6 Data-changing CTE

Advanced PostgreSQL can combine writes and returned data:

```sql
WITH updated_order AS (
    UPDATE tb_order
    SET status_id = #{nextStatusId},
        updated_date = NOW()
    WHERE id = #{orderId}
      AND status_id = #{currentStatusId}
    RETURNING id, status_id
)
INSERT INTO tb_order_status_history (order_id, new_status_id, changed_at)
SELECT id, status_id, NOW()
FROM updated_order;
```

Use this only when it makes the logic clearer and is well tested.

---

# 14. Indexes and EXPLAIN

## 14.1 What is an index?

An index is similar to the index at the end of a book.

Without an index, PostgreSQL may scan many rows.

With an appropriate index, it can find matching rows faster.

Indexes improve reads but add cost to:

- inserts
- updates
- deletes
- storage

Create indexes based on real queries, not guesses.

## 14.2 Good H-Phsar index candidates

Orders frequently searched by buyer:

```sql
CREATE INDEX idx_order_buyer_created
ON tb_order (buyer_account_id, created_date DESC);
```

Orders frequently searched by store and status:

```sql
CREATE INDEX idx_order_store_status_created
ON tb_order (store_id, status_id, created_date DESC);
```

Order details by order:

```sql
CREATE INDEX idx_order_detail_order
ON tb_order_detail (order_id);
```

Store products by store:

```sql
CREATE INDEX idx_store_product_store
ON tb_store_product_detail (store_id);
```

Foreign key columns are not automatically indexed just because they are foreign keys. Add indexes when query patterns need them.

## 14.3 Column order in a multicolumn index

Index:

```sql
(store_id, status_id, created_date)
```

This is useful for queries beginning with `store_id`, such as:

```sql
WHERE store_id = ?
  AND status_id = ?
ORDER BY created_date DESC
```

It may not help as much for a query filtering only by `status_id`.

## 14.4 Partial index

Index only active published stores:

```sql
CREATE INDEX idx_store_published
ON tb_store (created_date DESC)
WHERE is_publish = true
  AND is_active = true;
```

Use partial indexes when many rows are excluded by a stable condition used in real queries.

## 14.5 Expression index for case-insensitive equality

```sql
CREATE INDEX idx_product_lower_name
ON tb_product (LOWER(name));
```

Query:

```sql
WHERE LOWER(name) = LOWER(#{name})
```

For contains search such as `ILIKE '%rice%'`, a normal B-tree index usually cannot provide the same benefit. PostgreSQL trigram search may be a later topic.

## 14.6 EXPLAIN

```sql
EXPLAIN
SELECT id, name
FROM tb_store
WHERE supplier_account_id = 10;
```

`EXPLAIN` shows the query plan without executing the query.

## 14.7 EXPLAIN ANALYZE

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name
FROM tb_store
WHERE supplier_account_id = 10;
```

This executes the query and reports actual timing and buffer use.

Be careful with `EXPLAIN ANALYZE` on `UPDATE`, `DELETE`, or `INSERT`: it performs the write unless you wrap it in a transaction and roll it back.

Safe learning pattern:

```sql
BEGIN;

EXPLAIN (ANALYZE, BUFFERS)
UPDATE tb_store
SET updated_date = NOW()
WHERE id = 5;

ROLLBACK;
```

## 14.8 Important plan words

| Plan term | Simple meaning |
|---|---|
| `Seq Scan` | Read many/all table rows |
| `Index Scan` | Follow an index to table rows |
| `Index Only Scan` | Result may come mainly from index |
| `Nested Loop` | Join by repeatedly looking up rows |
| `Hash Join` | Build a hash table for matching |
| `Sort` | PostgreSQL must sort rows |
| `Filter` | Rows removed after being read |

Do not think `Seq Scan` is always bad. For a small table or a query returning most rows, it can be correct.

## 14.9 Performance workflow

```text
1. Find a slow endpoint.
2. Copy its real SQL and parameters.
3. Run EXPLAIN (ANALYZE, BUFFERS).
4. Look for large row counts and expensive operations.
5. Improve SQL or add one justified index.
6. Run EXPLAIN again.
7. Measure the API again.
```

---

# 15. Dates and time ranges

## 15.1 NOW and CURRENT_DATE

```sql
SELECT NOW();
SELECT CURRENT_DATE;
```

- `NOW()` includes date, time, and time zone information.
- `CURRENT_DATE` is a date.

## 15.2 Safe date parameters in MyBatis

Unsafe:

```xml
WHERE created_date BETWEEN '${startDate}' AND '${endDate}'
```

Safe:

```xml
WHERE created_date >= #{startDate}
  AND created_date < #{endExclusive}
```

Use Java `LocalDate`, `LocalDateTime`, `Instant`, or `OffsetDateTime` types as appropriate.

## 15.3 Half-open ranges

To query all of July 2026:

```sql
WHERE created_date >= TIMESTAMP '2026-07-01 00:00:00'
  AND created_date <  TIMESTAMP '2026-08-01 00:00:00'
```

This avoids mistakes about the final millisecond or microsecond of July 31.

## 15.4 Avoid applying functions to indexed date columns when possible

Convenient:

```sql
WHERE EXTRACT(YEAR FROM created_date) = 2026
  AND EXTRACT(MONTH FROM created_date) = 7
```

Potentially more index-friendly:

```sql
WHERE created_date >= DATE '2026-07-01'
  AND created_date <  DATE '2026-08-01'
```

## 15.5 date_trunc

Monthly sales:

```sql
SELECT date_trunc('month', o.created_date) AS month,
       SUM(od.qty * od.unit_price) AS revenue
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id
GROUP BY date_trunc('month', o.created_date)
ORDER BY month;
```

---

# 16. Concurrency and safe inventory

## 16.1 The overselling problem

Suppose stock is `5`.

Two requests both read `5` and each wants `4`.

Unsafe flow:

```text
Request A reads 5
Request B reads 5
Request A subtracts 4
Request B subtracts 4
Final stock may become negative
```

## 16.2 Atomic conditional update

Let PostgreSQL check and update in one statement:

```sql
UPDATE tb_store_product_detail
SET qty = qty - #{requestedQty}
WHERE id = #{storeProductId}
  AND qty >= #{requestedQty};
```

Check affected rows:

```text
1 → success
0 → insufficient stock or product not found
```

## 16.3 Validate positive quantity

```sql
UPDATE tb_store_product_detail
SET qty = qty - #{requestedQty}
WHERE id = #{storeProductId}
  AND #{requestedQty} > 0
  AND qty >= #{requestedQty};
```

The service should also validate `requestedQty > 0`.

## 16.4 Transaction for multiple items

```java
@Transactional
public void deductOrderStock(List<OrderItem> items) {
    for (OrderItem item : items) {
        int updated = repository.deductStockIfAvailable(
                item.storeProductId(),
                item.quantity()
        );

        if (updated != 1) {
            throw new InsufficientStockException();
        }
    }
}
```

If item 3 fails, deductions for items 1 and 2 roll back.

## 16.5 Conditional status transition

Prevent two requests from both changing the same pending order:

```sql
UPDATE tb_order
SET status_id = #{nextStatusId},
    updated_date = NOW()
WHERE id = #{orderId}
  AND status_id = #{expectedStatusId};
```

Again:

```text
1 → transition succeeded
0 → order was not in the expected state
```

## 16.6 SELECT FOR UPDATE

Row locking pattern:

```sql
SELECT id, qty
FROM tb_store_product_detail
WHERE id = #{storeProductId}
FOR UPDATE;
```

This locks the selected row until the transaction ends.

Use it when several related validations require a stable row. For simple stock subtraction, an atomic conditional update is usually easier.

## 16.7 Deadlocks

A deadlock can happen when two transactions lock resources in different orders.

Example:

```text
Transaction A locks Product 1, then wants Product 2
Transaction B locks Product 2, then wants Product 1
```

Reduce deadlock risk by processing item IDs in a consistent order:

```java
items.sort(comparing(OrderItem::storeProductId));
```

Applications should be prepared to retry certain serialization or deadlock failures.

## 16.8 Isolation levels

PostgreSQL supports transaction isolation levels. Learn them in this order:

1. `READ COMMITTED` — default and enough for many operations.
2. `REPEATABLE READ` — stable snapshot in one transaction.
3. `SERIALIZABLE` — strongest isolation, may require retrying failures.

Do not change the global isolation level without a measured business reason.

---

# 17. MyBatis safe SQL

## 17.1 `#{}` versus `${}`

| Syntax | Behavior | Safety |
|---|---|---|
| `#{value}` | JDBC prepared parameter `?` | Preferred for data values |
| `${value}` | Raw text substitution | Dangerous with user input |

Safe:

```xml
WHERE email = #{email}
```

Unsafe:

```xml
WHERE email = '${email}'
```

Search safely:

```xml
WHERE p.name ILIKE CONCAT('%', #{name}, '%')
```

## 17.2 Why `${}` can cause SQL injection

Suppose input is inserted directly:

```xml
WHERE name ILIKE '%${name}%'
```

A malicious string can change the SQL structure.

Prepared binding with `#{}` treats the input as data, not SQL syntax.

## 17.3 Structural SQL cannot use normal value binding

A column name cannot normally be replaced with a prepared `?` parameter.

Instead of:

```xml
ORDER BY ${sortColumn}
```

prefer an allowlist with `<choose>`:

```xml
<choose>
    <when test="sortColumn == 'name'">ORDER BY p.name</when>
    <when test="sortColumn == 'price'">ORDER BY spd.price</when>
    <otherwise>ORDER BY spd.id</otherwise>
</choose>
```

## 17.4 Use @Param clearly

```java
int deductStockIfAvailable(
    @Param("storeProductId") Integer storeProductId,
    @Param("requestedQty") Integer requestedQty
);
```

Mapper:

```xml
<update id="deductStockIfAvailable">
    UPDATE tb_store_product_detail
    SET qty = qty - #{requestedQty}
    WHERE id = #{storeProductId}
      AND qty >= #{requestedQty}
</update>
```

The names match exactly.

## 17.5 Mapper return types

| SQL purpose | Useful Java return type |
|---|---|
| One object that may not exist | `Optional<T>` or nullable `T` |
| Many objects | `List<T>` |
| `EXISTS` | `boolean` |
| Count | `long` |
| Insert/update/delete | `int` affected rows |

## 17.6 Do not expose password columns

Avoid mapping or returning password hashes in general profile queries.

Authentication queries may need the password hash internally, but API DTOs must not expose it.

---

# 18. MyBatis dynamic SQL

Official MyBatis dynamic SQL tags include:

```text
<if>
<choose> / <when> / <otherwise>
<trim>
<where>
<set>
<foreach>
```

## 18.1 `<if>`

```xml
<select id="searchProducts" resultMap="ProductResultMap">
    SELECT spd.id,
           p.name,
           spd.qty,
           spd.price
    FROM tb_store_product_detail spd
    JOIN tb_product p ON p.id = spd.product_id
    WHERE spd.store_id = #{storeId}

    <if test="name != null and name != ''">
        AND p.name ILIKE CONCAT('%', #{name}, '%')
    </if>

    <if test="categoryId != null">
        AND spd.category_id = #{categoryId}
    </if>
</select>
```

## 18.2 `<where>`

```xml
<select id="searchStores" resultMap="StoreResultMap">
    SELECT id, name, is_publish, is_active
    FROM tb_store
    <where>
        <if test="name != null and name != ''">
            AND name ILIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="isPublish != null">
            AND is_publish = #{isPublish}
        </if>
    </where>
</select>
```

`<where>`:

- adds `WHERE` only when needed
- removes a leading `AND` or `OR`

## 18.3 `<set>`

```xml
<update id="updateStore">
    UPDATE tb_store
    <set>
        <if test="request.name != null">
            name = #{request.name},
        </if>
        <if test="request.description != null">
            description = #{request.description},
        </if>
        updated_date = NOW()
    </set>
    WHERE id = #{storeId}
      AND supplier_account_id = #{supplierId}
</update>
```

`<set>` removes a trailing comma.

## 18.4 `<choose>`

```xml
<choose>
    <when test="sortDirection == 'asc'">ASC</when>
    <otherwise>DESC</otherwise>
</choose>
```

This is like Java `switch` or `if/else if/else`.

## 18.5 `<foreach>` for IN

Do not build a comma string such as `"1,2,3"` and inject it with `${}`.

Use:

```xml
<select id="findStoresByIds" resultMap="StoreResultMap">
    SELECT id, name
    FROM tb_store
    WHERE id IN
    <foreach collection="storeIds"
             item="storeId"
             open="("
             separator=","
             close=")">
        #{storeId}
    </foreach>
</select>
```

## 18.6 Reusable `<sql>` fragments

```xml
<sql id="storeColumns">
    s.id,
    s.name,
    s.description,
    s.banner_image AS bannerImage,
    s.created_date AS createdDate
</sql>

<select id="findStoreById" resultMap="StoreResultMap">
    SELECT <include refid="storeColumns"/>
    FROM tb_store s
    WHERE s.id = #{storeId}
</select>
```

Do not make fragments so abstract that no one can understand the final SQL.

---

# 19. MyBatis resultMap and N+1

## 19.1 Basic resultMap

```xml
<resultMap id="StoreResultMap"
           type="com.henheang.hphsar.model.store.Store">
    <id property="id" column="id"/>
    <result property="name" column="name"/>
    <result property="bannerImage" column="banner_image"/>
    <result property="isPublish" column="is_publish"/>
</resultMap>
```

- `property` is the Java field.
- `column` is the SQL result column.
- Marking the identifier with `<id>` helps MyBatis identify objects in nested mappings.

## 19.2 resultType versus resultMap

Use `resultType` for simple results:

```xml
<select id="countOrders" resultType="long">
    SELECT COUNT(*) FROM tb_order
</select>
```

Use `resultMap` for custom field mapping or nested objects.

Do not specify both on the same statement.

## 19.3 Nested select

```xml
<association property="category"
             column="category_id"
             select="findCategoryById"/>
```

This is easy to write, but it may execute one extra query per product.

## 19.4 N+1 problem

Suppose the main query returns 100 products.

```text
1 query for products
+ 100 queries for categories
= 101 queries
```

This is called N+1.

## 19.5 Join-based nested result mapping

A single query can return product and category columns:

```sql
SELECT spd.id AS store_product_id,
       p.id AS product_id,
       p.name AS product_name,
       c.id AS category_id,
       c.name AS category_name,
       spd.qty,
       spd.price
FROM tb_store_product_detail spd
JOIN tb_product p ON p.id = spd.product_id
LEFT JOIN tb_category c ON c.id = spd.category_id
WHERE spd.store_id = #{storeId};
```

Mapper:

```xml
<resultMap id="ProductWithCategoryResultMap" type="Product">
    <id property="id" column="store_product_id"/>
    <result property="name" column="product_name"/>
    <result property="qty" column="qty"/>
    <result property="price" column="price"/>

    <association property="category" javaType="Category">
        <id property="id" column="category_id"/>
        <result property="name" column="category_name"/>
    </association>
</resultMap>
```

## 19.6 Collection mapping

When one order has many items, joined rows repeat order columns. MyBatis can group them:

```xml
<resultMap id="OrderWithItemsResultMap" type="Order">
    <id property="id" column="order_id"/>
    <result property="createdDate" column="order_created_date"/>

    <collection property="items" ofType="OrderItem">
        <id property="id" column="order_detail_id"/>
        <result property="quantity" column="item_qty"/>
        <result property="unitPrice" column="unit_price"/>
    </collection>
</resultMap>
```

Always use unique column aliases to avoid mapping the wrong `id`.

---

# 20. Window functions

A window function calculates across related rows without collapsing them into one grouped row.

It uses `OVER (...)`.

## 20.1 Ranking stores by revenue

```sql
WITH store_revenue AS (
    SELECT o.store_id,
           SUM(od.qty * od.unit_price) AS revenue
    FROM tb_order o
    JOIN tb_order_detail od ON od.order_id = o.id
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
    GROUP BY o.store_id
)
SELECT store_id,
       revenue,
       DENSE_RANK() OVER (ORDER BY revenue DESC) AS revenue_rank
FROM store_revenue
ORDER BY revenue_rank;
```

## 20.2 Running total

```sql
WITH daily_sales AS (
    SELECT o.created_date::date AS sale_date,
           SUM(od.qty * od.unit_price) AS daily_revenue
    FROM tb_order o
    JOIN tb_order_detail od ON od.order_id = o.id
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
      AND o.store_id = #{storeId}
    GROUP BY o.created_date::date
)
SELECT sale_date,
       daily_revenue,
       SUM(daily_revenue) OVER (ORDER BY sale_date) AS running_revenue
FROM daily_sales
ORDER BY sale_date;
```

## 20.3 Row number per buyer

Find each buyer's most recent order:

```sql
WITH ranked_orders AS (
    SELECT o.*,
           ROW_NUMBER() OVER (
               PARTITION BY buyer_account_id
               ORDER BY created_date DESC, id DESC
           ) AS row_number
    FROM tb_order o
)
SELECT *
FROM ranked_orders
WHERE row_number = 1;
```

## 20.4 LAG

Compare monthly revenue with the previous month:

```sql
WITH monthly_revenue AS (
    SELECT date_trunc('month', o.created_date) AS month,
           SUM(od.qty * od.unit_price) AS revenue
    FROM tb_order o
    JOIN tb_order_detail od ON od.order_id = o.id
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
      AND o.store_id = #{storeId}
    GROUP BY date_trunc('month', o.created_date)
)
SELECT month,
       revenue,
       LAG(revenue) OVER (ORDER BY month) AS previous_revenue,
       revenue - LAG(revenue) OVER (ORDER BY month) AS change
FROM monthly_revenue
ORDER BY month;
```

---

# 21. Professional query patterns

## 21.1 Ownership inside mutation SQL

Unsafe pattern:

```text
Load order by ID
→ compare owner in Java
→ update by ID only
```

Stronger SQL:

```sql
UPDATE tb_order
SET updated_date = NOW()
WHERE id = #{orderId}
  AND buyer_account_id = #{buyerId};
```

The database condition protects the mutation.

## 21.2 Expected-state update

```sql
UPDATE tb_order
SET status_id = #{nextStatusId},
    updated_date = NOW()
WHERE id = #{orderId}
  AND status_id = #{expectedStatusId};
```

This prevents stale or conflicting workflow changes.

## 21.3 Snapshot price in order details

`tb_order_detail.unit_price` should store the price when the order was placed.

Do not calculate old invoice totals from the current product price:

```sql
-- Correct historical total
SUM(od.qty * od.unit_price)
```

The supplier may change `tb_store_product_detail.price` later.

## 21.4 Avoid duplicate query logic

If data and count queries use different filters, pagination becomes incorrect.

Keep their conditions aligned:

```xml
<sql id="storeSearchConditions">
    <if test="name != null and name != ''">
        AND s.name ILIKE CONCAT('%', #{name}, '%')
    </if>
    <if test="categoryId != null">
        AND EXISTS (
            SELECT 1
            FROM tb_store_category sc
            WHERE sc.store_id = s.id
              AND sc.category_id = #{categoryId}
        )
    </if>
</sql>
```

Use the same fragment in the list and count query.

## 21.5 Avoid hidden N+1 reports

A report endpoint should prefer one set-based query over Java loops calling the database repeatedly.

Weak:

```text
Load 100 stores
For each store, query revenue
For each store, query rating
```

Better:

```sql
SELECT s.id,
       s.name,
       COALESCE(r.rating, 0) AS rating,
       COALESCE(rev.revenue, 0) AS revenue
FROM tb_store s
LEFT JOIN (
    SELECT store_id, AVG(rated_star) AS rating
    FROM tb_rating_detail
    GROUP BY store_id
) r ON r.store_id = s.id
LEFT JOIN (
    SELECT o.store_id,
           SUM(od.qty * od.unit_price) AS revenue
    FROM tb_order o
    JOIN tb_order_detail od ON od.order_id = o.id
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
    GROUP BY o.store_id
) rev ON rev.store_id = s.id;
```

## 21.6 Use numeric for money

Money values should normally use a fixed-precision type such as:

```sql
NUMERIC(19, 2)
```

Avoid binary floating-point for exact financial values.

In Java, use `BigDecimal`.

## 21.7 Query naming

Good mapper names describe intent:

```text
findPublishedStores
existsBookmarkByStoreAndBuyer
deductStockIfAvailable
updateStatusIfCurrent
findOrderHistoryByOrderAndBuyer
```

Weak names:

```text
getData
doUpdate
query1
```

---

# 22. Database design knowledge

## 22.1 Normalization

Normalization reduces duplicate and inconsistent data.

Example:

- Product master name belongs in `tb_product`.
- Store-specific price and quantity belong in `tb_store_product_detail`.

This allows several stores to sell the same product at different prices.

## 22.2 One-to-one, one-to-many, many-to-many

### One-to-many

One supplier has many stores:

```text
tb_supplier_account 1 → many tb_store
```

### Many-to-many

A store has many categories, and a category can belong to many stores.

Bridge table:

```text
tb_store_category(store_id, category_id)
```

Recommended constraint:

```sql
UNIQUE (store_id, category_id)
```

## 22.3 Natural key versus surrogate key

Surrogate key:

```text
id = 123
```

Natural key:

```text
email = supplier@example.com
```

H-Phsar uses numeric IDs as primary keys. Business fields such as email may also need unique constraints.

## 22.4 Audit columns

Common fields:

```text
created_date
updated_date
created_by
updated_by
```

Use consistent types and naming.

## 22.5 Status table versus text enum

Current style:

```text
tb_order.status_id → tb_status.id
```

Benefit:

- Central status reference.

Risk:

- Magic numeric IDs leak into code.

Use status names or a well-defined enum in Java rather than scattering numbers such as `1`, `2`, and `6`.

## 22.6 Migration knowledge to learn

Professional backend developers should understand:

- versioned schema migrations
- adding nullable columns safely
- backfilling data
- adding constraints after cleanup
- creating indexes on large tables
- rollback and forward-fix strategies

In this project, Flyway should eventually become the single source of truth for schema changes.

---

# 23. Debugging checklist

## 23.1 Query returns no rows

Check:

1. Is the table correct?
2. Are parameter values correct?
3. Is the row active/published?
4. Is a join removing the row?
5. Is `NULL` compared incorrectly?
6. Is the status ID/name correct?
7. Is the date range correct?

Run the query in smaller steps.

## 23.2 Query returns duplicate rows

Check:

- Did you join two one-to-many relationships?
- Is the join condition incomplete?
- Is a bookmark/rating table missing buyer filtering?
- Should the schema have a unique constraint?
- Are you hiding a design error with `DISTINCT`?

Do not automatically add `DISTINCT` before understanding the duplicates.

## 23.3 Aggregate is too large

Usually caused by row multiplication before `SUM`.

Inspect joined rows before aggregating:

```sql
SELECT o.id, od.id, b.id
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id
LEFT JOIN tb_bookmark b ON b.store_id = o.store_id;
```

## 23.4 MyBatis binding error

Check:

- Java `@Param` name
- XML `#{name}`
- property path such as `#{request.name}`
- nullable JDBC type where needed
- mapper namespace
- statement ID

## 23.5 Slow query

1. Run with real representative parameters.
2. Use `EXPLAIN (ANALYZE, BUFFERS)` safely.
3. Check row estimates versus actual rows.
4. Check missing indexes.
5. Check functions applied to indexed columns.
6. Check leading `%` searches.
7. Check N+1 queries.
8. Check excessive offset pagination.

## 23.6 Before changing a mapper query

```text
[ ] Find every service method that calls it
[ ] Confirm parameter source
[ ] Run current SQL with test data
[ ] Write expected result
[ ] Change the smallest part
[ ] Add mapper integration test
[ ] Run full tests
[ ] Review generated SQL and diff
```

---

# 24. Practice path

Do these exercises in order. Do not read the answers first.

## Level 1 — Fundamentals

1. Return `id`, `name`, and `created_date` from `tb_category`.
2. Return published and active stores only.
3. Return products whose quantity is between 1 and 10.
4. Find buyers whose profile image is missing.
5. List the ten newest stores.
6. Count all buyer accounts.
7. Check whether buyer `3` bookmarked store `5`.
8. Find products containing the word `rice`, case-insensitively.

## Level 2 — Joins

9. Show every store with its supplier email.
10. Show store products with product name, price, and quantity.
11. Show every order with its buyer email and status name.
12. Show every order item with product name and subtotal.
13. Show every published store and whether buyer `3` bookmarked it.
14. Find stores that have no products.
15. Find buyers that have no orders.

## Level 3 — Aggregation

16. Count orders per store.
17. Calculate average rating per store, including stores with no ratings.
18. Calculate total value for one order.
19. Calculate completed revenue per store.
20. Show categories whose total ordered quantity is greater than 100.
21. Count pending, processing, and completed orders in one query.
22. Calculate monthly revenue for one store.

## Level 4 — Writes and integrity

23. Insert a category and return the new row.
24. Update a store only when it belongs to the authenticated supplier.
25. Delete a bookmark only when it belongs to the buyer.
26. Create a unique bookmark constraint.
27. Create a non-negative quantity constraint.
28. Write an upsert that ignores a duplicate bookmark.
29. Write a transaction that updates order status and inserts a notification.

## Level 5 — Professional SQL

30. Write an atomic stock deduction query.
31. Write a conditional order status transition.
32. Write a CTE for completed orders and revenue.
33. Rank stores by completed revenue.
34. Return the latest order for each buyer with `ROW_NUMBER`.
35. Calculate daily revenue and a running total.
36. Create an index for store order lists filtered by status and sorted by newest.
37. Write a keyset-pagination query for stores.
38. Write a MyBatis `<foreach>` query for a list of store IDs.
39. Replace dynamic `ORDER BY ${column}` with `<choose>`.
40. Design one integration test proving a SQL injection string is treated as data.

## Weekly learning plan

### Week 1

- Tables, rows, columns
- `SELECT`
- `WHERE`
- `NULL`
- Exercises 1–8

### Week 2

- Primary and foreign keys
- `INNER JOIN`
- `LEFT JOIN`
- Exercises 9–15

### Week 3

- Aggregates
- `GROUP BY`
- `HAVING`
- Exercises 16–22

### Week 4

- `INSERT`, `UPDATE`, `DELETE`
- `RETURNING`
- Constraints
- Exercises 23–28

### Week 5

- Transactions
- Ownership conditions
- Atomic updates
- Exercises 29–31

### Week 6

- Subqueries
- `EXISTS`
- CTEs
- Exercise 32

### Week 7

- Indexes
- `EXPLAIN`
- Pagination performance
- Exercises 36–37

### Week 8

- MyBatis safe binding
- Dynamic SQL
- Result maps
- N+1
- Exercises 38–40

### Week 9 and later

- Window functions
- Concurrency
- Isolation and locks
- Query tuning
- Database migrations

---

# 25. Answers

## A1

```sql
SELECT id, name, created_date
FROM tb_category;
```

## A2

```sql
SELECT id, name
FROM tb_store
WHERE is_publish = true
  AND is_active = true;
```

## A3

```sql
SELECT id, product_id, qty
FROM tb_store_product_detail
WHERE qty BETWEEN 1 AND 10;
```

## A4

```sql
SELECT id, buyer_account_id, first_name, last_name
FROM tb_buyer_info
WHERE profile_image IS NULL;
```

## A5

```sql
SELECT id, name, created_date
FROM tb_store
ORDER BY created_date DESC, id DESC
LIMIT 10;
```

## A6

```sql
SELECT COUNT(*)
FROM tb_buyer_account;
```

## A7

```sql
SELECT EXISTS (
    SELECT 1
    FROM tb_bookmark
    WHERE buyer_account_id = 3
      AND store_id = 5
);
```

## A8

```sql
SELECT id, name
FROM tb_product
WHERE name ILIKE '%rice%';
```

MyBatis:

```xml
WHERE name ILIKE CONCAT('%', #{name}, '%')
```

## A9

```sql
SELECT s.id,
       s.name,
       a.email AS supplier_email
FROM tb_store s
JOIN tb_supplier_account a
  ON a.id = s.supplier_account_id;
```

## A10

```sql
SELECT spd.id AS store_product_id,
       p.name AS product_name,
       spd.price,
       spd.qty
FROM tb_store_product_detail spd
JOIN tb_product p ON p.id = spd.product_id;
```

## A11

```sql
SELECT o.id,
       b.email AS buyer_email,
       st.name AS status
FROM tb_order o
JOIN tb_buyer_account b ON b.id = o.buyer_account_id
JOIN tb_status st ON st.id = o.status_id;
```

## A12

```sql
SELECT o.id AS order_id,
       p.name AS product_name,
       od.qty,
       od.unit_price,
       od.qty * od.unit_price AS subtotal
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id
JOIN tb_store_product_detail spd ON spd.id = od.store_product_id
JOIN tb_product p ON p.id = spd.product_id;
```

## A13

```sql
SELECT s.id,
       s.name,
       (b.id IS NOT NULL) AS is_bookmarked
FROM tb_store s
LEFT JOIN tb_bookmark b
  ON b.store_id = s.id
 AND b.buyer_account_id = 3
WHERE s.is_publish = true;
```

## A14

```sql
SELECT s.id, s.name
FROM tb_store s
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_store_product_detail spd
    WHERE spd.store_id = s.id
);
```

## A15

```sql
SELECT b.id, b.email
FROM tb_buyer_account b
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_order o
    WHERE o.buyer_account_id = b.id
);
```

## A16

```sql
SELECT store_id, COUNT(*) AS order_count
FROM tb_order
GROUP BY store_id
ORDER BY order_count DESC;
```

## A17

```sql
SELECT s.id,
       s.name,
       COALESCE(AVG(r.rated_star), 0) AS average_rating
FROM tb_store s
LEFT JOIN tb_rating_detail r ON r.store_id = s.id
GROUP BY s.id, s.name;
```

## A18

```sql
SELECT COALESCE(SUM(qty * unit_price), 0) AS order_total
FROM tb_order_detail
WHERE order_id = #{orderId};
```

## A19

```sql
SELECT o.store_id,
       SUM(od.qty * od.unit_price) AS revenue
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id
JOIN tb_status st ON st.id = o.status_id
WHERE st.name = 'COMPLETED'
GROUP BY o.store_id;
```

## A20

```sql
SELECT c.id,
       c.name,
       SUM(od.qty) AS total_quantity
FROM tb_order_detail od
JOIN tb_store_product_detail spd ON spd.id = od.store_product_id
JOIN tb_category c ON c.id = spd.category_id
GROUP BY c.id, c.name
HAVING SUM(od.qty) > 100;
```

## A21

```sql
SELECT o.store_id,
       COUNT(*) FILTER (WHERE st.name = 'PENDING') AS pending_count,
       COUNT(*) FILTER (WHERE st.name = 'PROCESSING') AS processing_count,
       COUNT(*) FILTER (WHERE st.name = 'COMPLETED') AS completed_count
FROM tb_order o
JOIN tb_status st ON st.id = o.status_id
GROUP BY o.store_id;
```

## A22

```sql
SELECT date_trunc('month', o.created_date) AS month,
       SUM(od.qty * od.unit_price) AS revenue
FROM tb_order o
JOIN tb_order_detail od ON od.order_id = o.id
JOIN tb_status st ON st.id = o.status_id
WHERE o.store_id = #{storeId}
  AND st.name = 'COMPLETED'
GROUP BY date_trunc('month', o.created_date)
ORDER BY month;
```

## A23

```sql
INSERT INTO tb_category (name, created_date, updated_date)
VALUES (#{name}, NOW(), NOW())
RETURNING id, name, created_date, updated_date;
```

## A24

```sql
UPDATE tb_store
SET name = #{name},
    description = #{description},
    updated_date = NOW()
WHERE id = #{storeId}
  AND supplier_account_id = #{supplierId};
```

## A25

```sql
DELETE FROM tb_bookmark
WHERE store_id = #{storeId}
  AND buyer_account_id = #{buyerId};
```

## A26

```sql
ALTER TABLE tb_bookmark
ADD CONSTRAINT uq_bookmark_store_buyer
UNIQUE (store_id, buyer_account_id);
```

## A27

```sql
ALTER TABLE tb_store_product_detail
ADD CONSTRAINT chk_store_product_qty_non_negative
CHECK (qty >= 0);
```

## A28

```sql
INSERT INTO tb_bookmark (store_id, buyer_account_id)
VALUES (#{storeId}, #{buyerId})
ON CONFLICT (store_id, buyer_account_id) DO NOTHING;
```

## A29

```sql
BEGIN;

UPDATE tb_order
SET status_id = #{nextStatusId},
    updated_date = NOW()
WHERE id = #{orderId}
  AND status_id = #{expectedStatusId};

INSERT INTO tb_buyer_notification (
    buyer_id,
    type_id,
    order_id,
    content,
    is_read,
    created_date
)
VALUES (
    #{buyerId},
    #{typeId},
    #{orderId},
    #{content},
    false,
    NOW()
);

COMMIT;
```

In Spring Boot, the service method should use `@Transactional` rather than manually issuing `BEGIN` and `COMMIT` through mapper methods.

## A30

```sql
UPDATE tb_store_product_detail
SET qty = qty - #{requestedQty}
WHERE id = #{storeProductId}
  AND #{requestedQty} > 0
  AND qty >= #{requestedQty};
```

## A31

```sql
UPDATE tb_order
SET status_id = #{nextStatusId},
    updated_date = NOW()
WHERE id = #{orderId}
  AND status_id = #{expectedStatusId};
```

## A32

```sql
WITH completed_orders AS (
    SELECT o.id, o.store_id
    FROM tb_order o
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
)
SELECT co.store_id,
       SUM(od.qty * od.unit_price) AS revenue
FROM completed_orders co
JOIN tb_order_detail od ON od.order_id = co.id
GROUP BY co.store_id;
```

## A33

```sql
WITH store_revenue AS (
    SELECT o.store_id,
           SUM(od.qty * od.unit_price) AS revenue
    FROM tb_order o
    JOIN tb_order_detail od ON od.order_id = o.id
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
    GROUP BY o.store_id
)
SELECT store_id,
       revenue,
       DENSE_RANK() OVER (ORDER BY revenue DESC) AS rank
FROM store_revenue;
```

## A34

```sql
WITH ranked_orders AS (
    SELECT o.*,
           ROW_NUMBER() OVER (
               PARTITION BY buyer_account_id
               ORDER BY created_date DESC, id DESC
           ) AS rn
    FROM tb_order o
)
SELECT *
FROM ranked_orders
WHERE rn = 1;
```

## A35

```sql
WITH daily_revenue AS (
    SELECT o.created_date::date AS sale_date,
           SUM(od.qty * od.unit_price) AS revenue
    FROM tb_order o
    JOIN tb_order_detail od ON od.order_id = o.id
    JOIN tb_status st ON st.id = o.status_id
    WHERE st.name = 'COMPLETED'
      AND o.store_id = #{storeId}
    GROUP BY o.created_date::date
)
SELECT sale_date,
       revenue,
       SUM(revenue) OVER (ORDER BY sale_date) AS running_total
FROM daily_revenue
ORDER BY sale_date;
```

## A36

```sql
CREATE INDEX idx_order_store_status_created
ON tb_order (store_id, status_id, created_date DESC);
```

Confirm the query plan and real usage before adding it to production.

## A37

```sql
SELECT id, name, created_date
FROM tb_store
WHERE (created_date, id) < (#{lastCreatedDate}, #{lastId})
ORDER BY created_date DESC, id DESC
LIMIT #{pageSize};
```

## A38

```xml
<select id="findStoresByIds" resultMap="StoreResultMap">
    SELECT id, name
    FROM tb_store
    WHERE id IN
    <foreach collection="storeIds"
             item="storeId"
             open="("
             separator=","
             close=")">
        #{storeId}
    </foreach>
</select>
```

## A39

```xml
<choose>
    <when test="sortBy == 'name'">ORDER BY p.name</when>
    <when test="sortBy == 'price'">ORDER BY spd.price</when>
    <when test="sortBy == 'quantity'">ORDER BY spd.qty</when>
    <otherwise>ORDER BY spd.id</otherwise>
</choose>
```

## A40

Test idea:

```text
Given two products
When search text is: ' OR '1'='1
Then the query treats the text literally
And it does not return all products
And database data is unchanged
```

Use a real PostgreSQL Testcontainer and the real MyBatis mapper.

---

# 26. Quick reference

## SQL query order

```sql
WITH        common_table_expressions
SELECT      result_columns
FROM        main_table
JOIN        related_table ON relationship
WHERE       row_filters
GROUP BY    grouping_columns
HAVING      group_filters
WINDOW      named_windows
ORDER BY    sorting
LIMIT       maximum_rows
OFFSET      skipped_rows;
```

## Logical understanding order

A simplified way to understand query processing:

```text
FROM / JOIN
→ WHERE
→ GROUP BY
→ HAVING
→ SELECT
→ DISTINCT
→ ORDER BY
→ LIMIT / OFFSET
```

## MyBatis safety card

```xml
#{value}  <!-- prepared value: preferred -->
${value}  <!-- raw text: never use with uncontrolled input -->
```

```xml
<if test="condition">...</if>
<where>...</where>
<set>...</set>
<choose>...</choose>
<foreach collection="items" item="item">#{item}</foreach>
```

## Data integrity card

```sql
NOT NULL
CHECK (qty >= 0)
UNIQUE (store_id, buyer_account_id)
PRIMARY KEY (id)
FOREIGN KEY (order_id) REFERENCES tb_order(id)
```

## Transaction card

```text
BEGIN
→ perform related writes
→ COMMIT when all succeed
→ ROLLBACK when one fails
```

Spring Boot:

```java
@Transactional
```

## Performance card

```text
Use real parameters
→ EXPLAIN (ANALYZE, BUFFERS)
→ inspect rows and scans
→ improve query or index
→ measure again
```

## The most important H-Phsar SQL knowledge

Learn these especially well:

1. `INNER JOIN` and `LEFT JOIN`
2. `GROUP BY` and `SUM(qty * unit_price)`
3. Ownership conditions in `UPDATE` and `DELETE`
4. Transactions for orders, stock, and notifications
5. `CHECK`, `UNIQUE`, and foreign keys
6. Atomic stock deduction
7. Conditional order status updates
8. Indexes for buyer/store/status/date searches
9. `EXPLAIN (ANALYZE, BUFFERS)`
10. Safe MyBatis `#{}` parameter binding
11. `<foreach>` instead of raw ID lists
12. Result-map N+1 awareness
13. Date half-open ranges
14. CTEs and window functions for reports

---

# Research basis

This guide was expanded using the current official PostgreSQL documentation for the SQL tutorial, queries, constraints, indexes, transactions, concurrency, query plans, and window functions, plus the official MyBatis documentation for mapper XML, parameter binding, result maps, and dynamic SQL.

The examples remain focused on the H-Phsar domain so that SQL practice directly improves your Spring Boot and MyBatis project skills.
