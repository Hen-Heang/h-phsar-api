# Database Relationships — H-Phsar API

> PostgreSQL schema for a supplier-buyer wholesale platform.
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
| **Supplier** | `tb_supplier_account` | `tb_supplier_info` |
| **Buyer** | `tb_buyer_account` | `tb_buyer_info` |

A **Supplier** owns stores and sells products.
A **Buyer** browses stores and places orders.

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

#### Supplier
```
tb_supplier_account  (1) ──────── (1)  tb_supplier_info
        id  ◄─────────────────────────────  supplier_account_id
```
```sql
-- tb_supplier_info
supplier_account_id INTEGER NOT NULL REFERENCES tb_supplier_account(id) ON DELETE CASCADE
```
| Column in `tb_supplier_info` | Description |
|---|---|
| `supplier_account_id` | Links to the account |
| `first_name`, `last_name` | Personal name |
| `gender` | Gender |
| `profile_image` | Profile picture URL |
| `primary_phone_number` | Main phone number |

#### Buyer
```
tb_buyer_account  (1) ──────── (1)  tb_buyer_info
        id  ◄──────────────────────────  buyer_account_id
```
```sql
-- tb_buyer_info
buyer_account_id INTEGER NOT NULL REFERENCES tb_buyer_account(id) ON DELETE CASCADE
```
| Column in `tb_buyer_info` | Description |
|---|---|
| `buyer_account_id` | Links to the account |
| `first_name`, `last_name` | Personal name |
| `gender` | Gender |
| `address` | Delivery address |
| `profile_image` | Profile picture URL |
| `primary_phone_number` | Main phone number |

---

## One-to-Many Relationships (1:N)

### 1. Supplier → Stores
One supplier can own many stores.

```
tb_supplier_account  (1) ──────── (N)  tb_store
        id  ◄────────────────────────────  supplier_account_id
```
```sql
-- tb_store
supplier_account_id INTEGER NOT NULL REFERENCES tb_supplier_account(id) ON DELETE CASCADE
```
> Delete the supplier account → all their stores are deleted automatically.

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
| `is_publish` | Visible to buyers or not |
| `image` | Product image in this store |
| `description` | Product description |

---

### 3. Buyer → Orders
One buyer can place many orders.

```
tb_buyer_account  (1) ──────── (N)  tb_order
        id  ◄──────────────────────────  buyer_account_id
```

---

### 4. Store → Orders
One store can receive many orders from different buyers.

```
tb_store  (1) ──────── (N)  tb_order
    id  ◄──────────────────  store_id
```
```sql
-- tb_order
buyer_account_id INTEGER NOT NULL REFERENCES tb_buyer_account(id) ON DELETE CASCADE,
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
tb_supplier_info  (1) ──────── (N)  tb_supplier_phone
        id  ◄────────────────────────── supplier_info_id

tb_buyer_info  (1) ──────── (N)  tb_buyer_phone
        id  ◄───────────────────────── buyer_info_id
```
```sql
-- tb_supplier_phone
supplier_info_id INTEGER NOT NULL REFERENCES tb_supplier_info(id) ON DELETE CASCADE,
phone_number        VARCHAR(50) NOT NULL

-- tb_buyer_phone
buyer_info_id INTEGER NOT NULL REFERENCES tb_buyer_info(id) ON DELETE CASCADE,
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
One store can receive many ratings from different buyers.

```
tb_store  (1) ──────── (N)  tb_rating_detail
    id  ◄──────────────────  store_id
```
```sql
-- tb_rating_detail
store_id         INTEGER NOT NULL REFERENCES tb_store(id)            ON DELETE CASCADE,
buyer_account_id INTEGER NOT NULL REFERENCES tb_buyer_account(id) ON DELETE CASCADE,
rated_star       INTEGER CHECK (rated_star BETWEEN 1 AND 5),
comment     TEXT
```
> `CHECK (rated_star BETWEEN 1 AND 5)` — PostgreSQL rejects any value outside 1 to 5.

---

### 9. Store → Bookmarks
One store can be bookmarked by many buyers.

```
tb_store  (1) ──────── (N)  tb_bookmark
    id  ◄──────────────────  store_id
```
```sql
-- tb_bookmark
store_id            INTEGER NOT NULL REFERENCES tb_store(id)            ON DELETE CASCADE,
buyer_account_id INTEGER NOT NULL REFERENCES tb_buyer_account(id) ON DELETE CASCADE
```

---

### 10. Account → Notifications
Each account receives many notifications.

```
tb_supplier_account  (1) ──────── (N)  tb_supplier_notification
tb_buyer_account     (1) ──────── (N)  tb_buyer_notification
```
```sql
-- tb_supplier_notification
supplier_id INTEGER NOT NULL REFERENCES tb_supplier_account(id) ON DELETE CASCADE,
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
tb_supplier_account  (1) ──────── (N)  tb_supplier_otp
tb_buyer_account     (1) ──────── (N)  tb_buyer_otp
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
  ├──► tb_supplier_account ──────────────────────────────► tb_supplier_info
  │           │                                                        │
  │           │                                               tb_supplier_phone
  │           │
  │           ├──► tb_store ──────────────────────────────► tb_store_product_detail
  │           │        │                                            │
  │           │        ├──► tb_store_phone                    tb_product ◄─── tb_product_category ──► tb_category
  │           │        ├──► tb_store_category ──────────────────────────────────────────────────────► tb_category
  │           │        ├──► tb_rating_detail ◄── tb_buyer_account
  │           │        ├──► tb_bookmark      ◄── tb_buyer_account
  │           │        └──► tb_order ──────────────────────────────► tb_order_detail
  │           │                 │                                           │
  │           │                 └──► tb_buyer_account                   └──► tb_store_product_detail
  │           │
  │           └──► tb_supplier_notification
  │           └──► tb_supplier_otp
  │
  └──► tb_buyer_account ───────────────────────────────► tb_buyer_info
              │                                                     │
              │                                            tb_buyer_phone
              │
              ├──► tb_order
              ├──► tb_buyer_notification
              ├──► tb_buyer_otp
              ├──► tb_rating_detail
              └──► tb_bookmark

tb_status ──► tb_order
tb_status ──► tb_order_status_history (previous_status_id, new_status_id)
tb_order ──► tb_order_status_history
tb_notification_type ──► tb_supplier_notification
tb_notification_type ──► tb_buyer_notification
```

`tb_order_status_history` (Step 3C, append-only audit trail) and the current
tb_status id/name mapping are documented in full in
[`docs/ORDER_WORKFLOW.md`](ORDER_WORKFLOW.md) — that is the source of truth
for order-status meanings; treat any status names here as illustrative only.

---

## Summary Table

| # | Table A | Relationship | Table B | Junction Table |
|---|---|---|---|---|
| 1 | `tb_supplier_account` | 1:1 | `tb_supplier_info` | — |
| 2 | `tb_buyer_account` | 1:1 | `tb_buyer_info` | — |
| 3 | `tb_supplier_account` | 1:N | `tb_store` | — |
| 4 | `tb_store` | 1:N | `tb_store_product_detail` | — |
| 5 | `tb_buyer_account` | 1:N | `tb_order` | — |
| 6 | `tb_store` | 1:N | `tb_order` | — |
| 7 | `tb_order` | 1:N | `tb_order_detail` | — |
| 8 | `tb_supplier_info` | 1:N | `tb_supplier_phone` | — |
| 9 | `tb_buyer_info` | 1:N | `tb_buyer_phone` | — |
| 10 | `tb_store` | 1:N | `tb_store_phone` | — |
| 11 | `tb_store` | 1:N | `tb_rating_detail` | — |
| 12 | `tb_store` | 1:N | `tb_bookmark` | — |
| 13 | `tb_supplier_account` | 1:N | `tb_supplier_notification` | — |
| 14 | `tb_buyer_account` | 1:N | `tb_buyer_notification` | — |
| 15 | `tb_supplier_account` | 1:N | `tb_supplier_otp` | — |
| 16 | `tb_buyer_account` | 1:N | `tb_buyer_otp` | — |
| 17 | `tb_product` | M:N | `tb_category` | `tb_product_category` |
| 18 | `tb_store` | M:N | `tb_category` | `tb_store_category` |

---

## ON DELETE CASCADE Explained

Most foreign keys in this project use `ON DELETE CASCADE`. Here is what happens:

```
DELETE FROM tb_supplier_account WHERE id = 5;
```

This one delete automatically triggers:
```
tb_supplier_account (id=5) deleted
  └── tb_supplier_info (where supplier_account_id=5) deleted
        └── tb_supplier_phone (where supplier_info_id=...) deleted
  └── tb_store (where supplier_account_id=5) deleted
        └── tb_store_product_detail (where store_id=...) deleted
        └── tb_order (where store_id=...) deleted
              └── tb_order_detail (where order_id=...) deleted
        └── tb_store_phone deleted
        └── tb_store_category deleted
        └── tb_rating_detail deleted
        └── tb_bookmark deleted
  └── tb_supplier_notification deleted
  └── tb_supplier_otp deleted
```

> One delete at the top cascades down like a waterfall through all child tables automatically.
