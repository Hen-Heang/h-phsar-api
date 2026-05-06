# Database Relationships — H-Phsar API

> PostgreSQL schema for a distributor-retailer wholesale platform.
> All tables use `SERIAL PRIMARY KEY` (auto-increment) and `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`.

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Relationship Types Explained](#relationship-types-explained)
3. [One-to-One Relationships](#one-to-one-relationships-11)
4. [One-to-Many Relationships](#one-to-many-relationships-1n)
5. [Many-to-Many Relationships](#many-to-many-relationships-mn)
6. [Full Entity Relationship Diagram](#full-entity-relationship-diagram)
7. [Summary Table](#summary-table)
8. [ON DELETE CASCADE Explained](#on-delete-cascade-explained)

---

## Database Overview

The database has two main actors:

| Actor | Account Table | Info Table |
|---|---|---|
| **Distributor** | `tb_distributor_account` | `tb_distributor_info` |
| **Retailer** | `tb_retailer_account` | `tb_retailer_info` |

A **Distributor** owns stores and sells products.
A **Retailer** browses stores and places orders.

---

## Relationship Types Explained

| Type | Meaning | Example |
|---|---|---|
| **1:1** (One-to-One) | One row in A links to exactly one row in B | One account has one profile |
| **1:N** (One-to-Many) | One row in A links to many rows in B | One store has many products |
| **M:N** (Many-to-Many) | Many rows in A link to many rows in B — needs a junction table | One product belongs to many categories |

---

## One-to-One Relationships (1:1)

### Account → Info Profile

Each account has exactly **one** personal info record.

#### Distributor
```
tb_distributor_account  (1) ──────── (1)  tb_distributor_info
        id  ◄─────────────────────────────  distributor_account_id
```
```sql
-- tb_distributor_info
distributor_account_id INTEGER NOT NULL REFERENCES tb_distributor_account(id) ON DELETE CASCADE
```
| Column in `tb_distributor_info` | Description |
|---|---|
| `distributor_account_id` | Links to the account |
| `first_name`, `last_name` | Personal name |
| `gender` | Gender |
| `profile_image` | Profile picture URL |
| `primary_phone_number` | Main phone number |

#### Retailer
```
tb_retailer_account  (1) ──────── (1)  tb_retailer_info
        id  ◄──────────────────────────  retailer_account_id
```
```sql
-- tb_retailer_info
retailer_account_id INTEGER NOT NULL REFERENCES tb_retailer_account(id) ON DELETE CASCADE
```
| Column in `tb_retailer_info` | Description |
|---|---|
| `retailer_account_id` | Links to the account |
| `first_name`, `last_name` | Personal name |
| `gender` | Gender |
| `address` | Delivery address |
| `profile_image` | Profile picture URL |
| `primary_phone_number` | Main phone number |

---

## One-to-Many Relationships (1:N)

### 1. Distributor → Stores
One distributor can own many stores.

```
tb_distributor_account  (1) ──────── (N)  tb_store
        id  ◄────────────────────────────  distributor_account_id
```
```sql
-- tb_store
distributor_account_id INTEGER NOT NULL REFERENCES tb_distributor_account(id) ON DELETE CASCADE
```
> Delete the distributor account → all their stores are deleted automatically.

---

### 2. Store → Products (Catalog)
One store can list many products for sale.

```
tb_store  (1) ──────── (N)  tb_store_product_detail
    id  ◄──────────────────  store_id
```
```sql
-- tb_store_product_detail
store_id   INTEGER NOT NULL REFERENCES tb_store(id)   ON DELETE CASCADE,
product_id INTEGER NOT NULL REFERENCES tb_product(id) ON DELETE CASCADE
```
| Column | Description |
|---|---|
| `qty` | Stock quantity |
| `price` | Selling price |
| `is_publish` | Visible to retailers or not |
| `image` | Product image in this store |
| `description` | Product description |

---

### 3. Retailer → Orders
One retailer can place many orders.

```
tb_retailer_account  (1) ──────── (N)  tb_order
        id  ◄──────────────────────────  retailer_account_id
```

---

### 4. Store → Orders
One store can receive many orders from different retailers.

```
tb_store  (1) ──────── (N)  tb_order
    id  ◄──────────────────  store_id
```
```sql
-- tb_order
retailer_account_id INTEGER NOT NULL REFERENCES tb_retailer_account(id) ON DELETE CASCADE,
store_id            INTEGER NOT NULL REFERENCES tb_store(id)            ON DELETE CASCADE,
status_id           INTEGER NOT NULL REFERENCES tb_status(id)
```
| Status ID | Status Name |
|---|---|
| 1 | PENDING |
| 2 | PROCESSING |
| 3 | CONFIRMED |
| 4 | SHIPPING |
| 5 | DELIVERED |
| 6 | COMPLETED |
| 7 | CANCELLED |
| 8 | REJECTED |
| 9 | DRAFT |

---

### 5. Order → Order Details (Line Items)
One order contains many products (line items).

```
tb_order  (1) ──────── (N)  tb_order_detail
    id  ◄──────────────────  order_id
```
```sql
-- tb_order_detail
order_id         INTEGER NOT NULL REFERENCES tb_order(id)                ON DELETE CASCADE,
store_product_id INTEGER NOT NULL REFERENCES tb_store_product_detail(id)
```
| Column | Description |
|---|---|
| `qty` | How many units ordered |
| `unit_price` | Price at the time of order |
| `store_product_id` | Which product from which store |

---

### 6. Info → Phone Numbers
One person can have many phone numbers.

```
tb_distributor_info  (1) ──────── (N)  tb_distributor_phone
        id  ◄────────────────────────── distributor_info_id

tb_retailer_info  (1) ──────── (N)  tb_retailer_phone
        id  ◄───────────────────────── retailer_info_id
```
```sql
-- tb_distributor_phone
distributor_info_id INTEGER NOT NULL REFERENCES tb_distributor_info(id) ON DELETE CASCADE,
phone_number        VARCHAR(50) NOT NULL

-- tb_retailer_phone
retailer_info_id INTEGER NOT NULL REFERENCES tb_retailer_info(id) ON DELETE CASCADE,
phone_number     VARCHAR(50) NOT NULL
```

---

### 7. Store → Phone Numbers
One store can also have many contact phone numbers.

```
tb_store  (1) ──────── (N)  tb_store_phone
    id  ◄──────────────────  store_id
```

---

### 8. Store → Ratings
One store can receive many ratings from different retailers.

```
tb_store  (1) ──────── (N)  tb_rating_detail
    id  ◄──────────────────  store_id
```
```sql
-- tb_rating_detail
store_id    INTEGER NOT NULL REFERENCES tb_store(id)            ON DELETE CASCADE,
retailer_id INTEGER NOT NULL REFERENCES tb_retailer_account(id) ON DELETE CASCADE,
rated_star  INTEGER CHECK (rated_star BETWEEN 1 AND 5),
comment     TEXT
```
> `CHECK (rated_star BETWEEN 1 AND 5)` — PostgreSQL rejects any value outside 1 to 5.

---

### 9. Store → Bookmarks
One store can be bookmarked by many retailers.

```
tb_store  (1) ──────── (N)  tb_bookmark
    id  ◄──────────────────  store_id
```
```sql
-- tb_bookmark
store_id            INTEGER NOT NULL REFERENCES tb_store(id)            ON DELETE CASCADE,
retailer_account_id INTEGER NOT NULL REFERENCES tb_retailer_account(id) ON DELETE CASCADE
```

---

### 10. Account → Notifications
Each account receives many notifications.

```
tb_distributor_account  (1) ──────── (N)  tb_distributor_notification
tb_retailer_account     (1) ──────── (N)  tb_retailer_notification
```
```sql
-- tb_distributor_notification
distributor_id INTEGER NOT NULL REFERENCES tb_distributor_account(id) ON DELETE CASCADE,
type_id        INTEGER NOT NULL REFERENCES tb_notification_type(id),
order_id       INTEGER,   -- optional: which order triggered this
is_read        BOOLEAN DEFAULT FALSE
```

| Notification Type ID | Name |
|---|---|
| 1 | Order Received |
| 2 | Out of Stock |
| 3 | New Order |
| 4 | Order Accepted |
| 5 | Order Declined |
| 6 | Order Preparing |
| 7 | Order Dispatching |
| 8 | Order Arrived |
| 9 | Order Complete |

---

### 11. Account → OTPs
Each account can have many OTP records (for email verification / password reset).

```
tb_distributor_account  (1) ──────── (N)  tb_distributor_otp
tb_retailer_account     (1) ──────── (N)  tb_retailer_otp
```

---

## Many-to-Many Relationships (M:N)

Many-to-many needs a **junction table** in the middle.

### 1. Product ↔ Category

One product can belong to many categories.
One category can contain many products.

```
tb_product  (N) ──── tb_product_category ──── (N)  tb_category
    id  ◄────────────  product_id                    ──────►  id
                       category_id
```
```sql
-- Junction table: tb_product_category
CREATE TABLE tb_product_category (
    id          SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL REFERENCES tb_category(id) ON DELETE CASCADE,
    product_id  INTEGER NOT NULL REFERENCES tb_product(id)  ON DELETE CASCADE
);
```

**Example data:**
| product_id | category_id |
|---|---|
| 1 (Rice) | 3 (Food) |
| 1 (Rice) | 5 (Grain) |
| 2 (Shampoo) | 7 (Beauty) |

---

### 2. Store ↔ Category

One store can sell products in many categories.
One category can appear in many stores.

```
tb_store  (N) ──── tb_store_category ──── (N)  tb_category
    id  ◄──────────  store_id                    ──────►  id
                     category_id
```
```sql
-- Junction table: tb_store_category
CREATE TABLE tb_store_category (
    id          SERIAL PRIMARY KEY,
    store_id    INTEGER NOT NULL REFERENCES tb_store(id)    ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES tb_category(id) ON DELETE CASCADE
);
```

---

## Full Entity Relationship Diagram

```
tb_role
  │
  ├──► tb_distributor_account ──────────────────────────────► tb_distributor_info
  │           │                                                        │
  │           │                                               tb_distributor_phone
  │           │
  │           ├──► tb_store ──────────────────────────────► tb_store_product_detail
  │           │        │                                            │
  │           │        ├──► tb_store_phone                    tb_product ◄─── tb_product_category ──► tb_category
  │           │        ├──► tb_store_category ──────────────────────────────────────────────────────► tb_category
  │           │        ├──► tb_rating_detail ◄── tb_retailer_account
  │           │        ├──► tb_bookmark      ◄── tb_retailer_account
  │           │        └──► tb_order ──────────────────────────────► tb_order_detail
  │           │                 │                                           │
  │           │                 └──► tb_retailer_account                   └──► tb_store_product_detail
  │           │
  │           └──► tb_distributor_notification
  │           └──► tb_distributor_otp
  │
  └──► tb_retailer_account ───────────────────────────────► tb_retailer_info
              │                                                     │
              │                                            tb_retailer_phone
              │
              ├──► tb_order
              ├──► tb_retailer_notification
              ├──► tb_retailer_otp
              ├──► tb_rating_detail
              └──► tb_bookmark

tb_status ──► tb_order
tb_notification_type ──► tb_distributor_notification
tb_notification_type ──► tb_retailer_notification
```

---

## Summary Table

| # | Table A | Relationship | Table B | Junction Table |
|---|---|---|---|---|
| 1 | `tb_distributor_account` | 1:1 | `tb_distributor_info` | — |
| 2 | `tb_retailer_account` | 1:1 | `tb_retailer_info` | — |
| 3 | `tb_distributor_account` | 1:N | `tb_store` | — |
| 4 | `tb_store` | 1:N | `tb_store_product_detail` | — |
| 5 | `tb_retailer_account` | 1:N | `tb_order` | — |
| 6 | `tb_store` | 1:N | `tb_order` | — |
| 7 | `tb_order` | 1:N | `tb_order_detail` | — |
| 8 | `tb_distributor_info` | 1:N | `tb_distributor_phone` | — |
| 9 | `tb_retailer_info` | 1:N | `tb_retailer_phone` | — |
| 10 | `tb_store` | 1:N | `tb_store_phone` | — |
| 11 | `tb_store` | 1:N | `tb_rating_detail` | — |
| 12 | `tb_store` | 1:N | `tb_bookmark` | — |
| 13 | `tb_distributor_account` | 1:N | `tb_distributor_notification` | — |
| 14 | `tb_retailer_account` | 1:N | `tb_retailer_notification` | — |
| 15 | `tb_distributor_account` | 1:N | `tb_distributor_otp` | — |
| 16 | `tb_retailer_account` | 1:N | `tb_retailer_otp` | — |
| 17 | `tb_product` | M:N | `tb_category` | `tb_product_category` |
| 18 | `tb_store` | M:N | `tb_category` | `tb_store_category` |

---

## ON DELETE CASCADE Explained

Most foreign keys in this project use `ON DELETE CASCADE`. Here is what happens:

```
DELETE FROM tb_distributor_account WHERE id = 5;
```

This one delete automatically triggers:
```
tb_distributor_account (id=5) deleted
  └── tb_distributor_info (where distributor_account_id=5) deleted
        └── tb_distributor_phone (where distributor_info_id=...) deleted
  └── tb_store (where distributor_account_id=5) deleted
        └── tb_store_product_detail (where store_id=...) deleted
        └── tb_order (where store_id=...) deleted
              └── tb_order_detail (where order_id=...) deleted
        └── tb_store_phone deleted
        └── tb_store_category deleted
        └── tb_rating_detail deleted
        └── tb_bookmark deleted
  └── tb_distributor_notification deleted
  └── tb_distributor_otp deleted
```

> One delete at the top cascades down like a waterfall through all child tables automatically.
