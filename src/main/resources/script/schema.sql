-- ═══════════════════════════════════════════════════════════════
--  H-Phsar — Full Database Schema (PostgreSQL)
--  Safe to run multiple times: CREATE TABLE IF NOT EXISTS + ON CONFLICT DO NOTHING
--  Usage: psql -U postgres -d <db_name> -f schema.sql
-- ═══════════════════════════════════════════════════════════════

-- ── Reference / lookup tables ──────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_role (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tb_status (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tb_category (
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL UNIQUE,
    image        TEXT,
    is_active    BOOLEAN   DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_product (
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active    BOOLEAN   DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS tb_product_category (
    id          SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL REFERENCES tb_category (id) ON DELETE CASCADE,
    product_id  INTEGER NOT NULL REFERENCES tb_product  (id) ON DELETE CASCADE
);

-- ── Accounts ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_supplier_account (
    id           SERIAL PRIMARY KEY,
    role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     TEXT         NOT NULL,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_verified  BOOLEAN      DEFAULT FALSE,
    is_active    BOOLEAN      DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS tb_buyer_account (
    id           SERIAL PRIMARY KEY,
    role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     TEXT         NOT NULL,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_verified  BOOLEAN      DEFAULT FALSE,
    is_active    BOOLEAN      DEFAULT TRUE
);

-- ── Profiles ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_supplier_info (
    id                   SERIAL PRIMARY KEY,
    supplier_account_id INTEGER NOT NULL REFERENCES tb_supplier_account (id) ON DELETE CASCADE,
    first_name           VARCHAR(150),
    last_name            VARCHAR(150),
    gender               VARCHAR(20),
    profile_image        TEXT,
    primary_phone_number VARCHAR(50),
    created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_buyer_info (
    id                   SERIAL PRIMARY KEY,
    buyer_account_id  INTEGER NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
    first_name           VARCHAR(150),
    last_name            VARCHAR(150),
    gender               VARCHAR(20),
    address              TEXT,
    primary_phone_number VARCHAR(50),
    profile_image        TEXT,
    created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_supplier_phone (
    id                  SERIAL PRIMARY KEY,
    supplier_info_id INTEGER NOT NULL REFERENCES tb_supplier_info (id) ON DELETE CASCADE,
    phone_number        VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_buyer_phone (
    id               SERIAL PRIMARY KEY,
    buyer_info_id INTEGER NOT NULL REFERENCES tb_buyer_info (id) ON DELETE CASCADE,
    phone_number     VARCHAR(50) NOT NULL
);

-- ── Stores ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_store (
    id                     SERIAL PRIMARY KEY,
    supplier_account_id INTEGER      NOT NULL REFERENCES tb_supplier_account (id) ON DELETE CASCADE,
    name                   VARCHAR(255) NOT NULL,
    banner_image           TEXT,
    description            TEXT,
    address                TEXT,
    is_publish             BOOLEAN      DEFAULT TRUE,
    is_active              BOOLEAN      DEFAULT TRUE,
    phone                  VARCHAR(50),
    created_date           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_store_phone (
    id       SERIAL PRIMARY KEY,
    store_id INTEGER NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    phone    VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_store_category (
    id          SERIAL PRIMARY KEY,
    store_id    INTEGER NOT NULL REFERENCES tb_store    (id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES tb_category (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tb_store_product_detail (
    id           SERIAL PRIMARY KEY,
    store_id     INTEGER          NOT NULL REFERENCES tb_store   (id) ON DELETE CASCADE,
    product_id   INTEGER          NOT NULL REFERENCES tb_product (id) ON DELETE CASCADE,
    qty          INTEGER          NOT NULL DEFAULT 0 CHECK (qty >= 0),
    price        DOUBLE PRECISION DEFAULT 0,
    is_publish   BOOLEAN          DEFAULT FALSE,
    image        TEXT,
    category_id  INTEGER REFERENCES tb_category (id),
    description  TEXT,
    is_active    BOOLEAN          DEFAULT TRUE,
    created_date TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- ── Product imports ────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_product_import (
    id           SERIAL PRIMARY KEY,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    store_id     INTEGER   NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tb_product_import_detail (
    id                SERIAL PRIMARY KEY,
    product_id        INTEGER          NOT NULL REFERENCES tb_product        (id) ON DELETE CASCADE,
    product_import_id INTEGER          NOT NULL REFERENCES tb_product_import (id) ON DELETE CASCADE,
    qty               INTEGER          NOT NULL,
    price             DOUBLE PRECISION NOT NULL,
    category_id       INTEGER REFERENCES tb_category (id)
);

-- ── Orders ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_order (
    id                  SERIAL PRIMARY KEY,
    buyer_account_id INTEGER          NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
    store_id            INTEGER          NOT NULL REFERENCES tb_store             (id) ON DELETE CASCADE,
    status_id           INTEGER          NOT NULL REFERENCES tb_status            (id),
    created_date        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_date        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    total_price         DOUBLE PRECISION DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tb_order_detail (
    id               SERIAL PRIMARY KEY,
    order_id         INTEGER          NOT NULL REFERENCES tb_order              (id) ON DELETE CASCADE,
    qty              INTEGER          NOT NULL,
    unit_price       DOUBLE PRECISION NOT NULL,
    store_product_id INTEGER          NOT NULL REFERENCES tb_store_product_detail (id),
    UNIQUE (order_id, store_product_id)
);

CREATE INDEX IF NOT EXISTS idx_order_detail_order_id ON tb_order_detail (order_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON tb_order (status_id);

-- Step 3C — append-only order status history. changed_by_account_id has no FK:
-- suppliers and buyers live in separate account tables, so a single FK can't
-- conditionally target either (see DatabaseInitializer for the full rationale).
CREATE TABLE IF NOT EXISTS tb_order_status_history (
    id                    SERIAL PRIMARY KEY,
    order_id              INTEGER NOT NULL REFERENCES tb_order(id) ON DELETE CASCADE,
    previous_status_id    INTEGER REFERENCES tb_status(id),
    new_status_id         INTEGER NOT NULL REFERENCES tb_status(id),
    changed_by_account_id INTEGER,
    changed_by_role       VARCHAR(30) NOT NULL,
    reason                VARCHAR(500),
    changed_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON tb_order_status_history (order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_changed_at ON tb_order_status_history (changed_at);

-- ── Ratings and bookmarks ──────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_rating_detail (
    id           SERIAL PRIMARY KEY,
    store_id     INTEGER NOT NULL REFERENCES tb_store          (id) ON DELETE CASCADE,
    buyer_account_id INTEGER NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
    rated_star   INTEGER CHECK (rated_star BETWEEN 1 AND 5),
    comment      TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_bookmark (
    id                  SERIAL PRIMARY KEY,
    store_id            INTEGER NOT NULL REFERENCES tb_store           (id) ON DELETE CASCADE,
    buyer_account_id INTEGER NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
    created_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Notifications ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_notification_type (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    template TEXT
);

CREATE TABLE IF NOT EXISTS tb_supplier_notification (
    id             SERIAL PRIMARY KEY,
    supplier_id INTEGER NOT NULL REFERENCES tb_supplier_account (id) ON DELETE CASCADE,
    type_id        INTEGER NOT NULL REFERENCES tb_notification_type   (id),
    order_id       INTEGER,
    content        TEXT,
    is_read        BOOLEAN   DEFAULT FALSE,
    created_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_buyer_notification (
    id           SERIAL PRIMARY KEY,
    buyer_id  INTEGER NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
    type_id      INTEGER NOT NULL REFERENCES tb_notification_type (id),
    order_id     INTEGER,
    content      TEXT,
    is_read      BOOLEAN   DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── OTPs ───────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tb_supplier_otp (
    id                     SERIAL PRIMARY KEY,
    supplier_account_id INTEGER      NOT NULL REFERENCES tb_supplier_account (id) ON DELETE CASCADE,
    otp_code               INTEGER      NOT NULL,
    supplier_email      VARCHAR(255) NOT NULL,
    created_date           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_buyer_otp (
    id                  SERIAL PRIMARY KEY,
    buyer_account_id INTEGER      NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
    otp_code            INTEGER      NOT NULL,
    buyer_email      VARCHAR(255) NOT NULL,
    created_date        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ── Seed data ──────────────────────────────────────────────────

INSERT INTO tb_role (id, name) VALUES
    (1, 'SUPPLIER'),
    (2, 'BUYER')
ON CONFLICT (id) DO NOTHING;

-- Step 3C order lifecycle (see DatabaseInitializer#migrateOrderLifecycleStatuses
-- for the full audit trail of why these ids/names are what they are — in short,
-- they match what the mapper layer has always actually done with each id, not
-- the original, drifted seed names). ids 4/5 (formerly SHIPPING/DELIVERED) are
-- retired: no code path ever wrote them, so a fresh schema doesn't seed them.
INSERT INTO tb_status (id, name) VALUES
    (1, 'PENDING'),
    (2, 'PROCESSING'),
    (3, 'DISPATCHED'),
    (6, 'COMPLETED'),
    (7, 'CANCELLED'),
    (8, 'DRAFT'),
    (9, 'CART'),
    (10, 'REJECTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_notification_type (id, name, template) VALUES
    (1, 'Order Received',    NULL),
    (2, 'Out of Stock',      NULL),
    (3, 'New Order',         NULL),
    (4, 'Order Accepted',    NULL),
    (5, 'Order Declined',    NULL),
    (6, 'Order Preparing',   NULL),
    (7, 'Order Dispatching', NULL),
    (8, 'Order Arrived',     NULL),
    (9, 'Order Complete',    NULL)
ON CONFLICT (id) DO NOTHING;
