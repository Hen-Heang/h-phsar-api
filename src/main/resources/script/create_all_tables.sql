-- Warehouse Master full table creation script (PostgresSQL)
-- Usage example:
--   psql -U postgres -d dorms -f src/main/resources/script/create_all_tables.sql
--
-- This script is idempotent (`CREATE TABLE IF NOT EXISTS`) and safe to run multiple times.

-- Core reference tables
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
    is_active    BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_product (
                                          id           SERIAL PRIMARY KEY,
                                          name         VARCHAR(255) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active    BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS tb_product_category (
                                                   id          SERIAL PRIMARY KEY,
                                                   category_id INTEGER NOT NULL REFERENCES tb_category (id) ON DELETE CASCADE,
    product_id  INTEGER NOT NULL REFERENCES tb_product (id) ON DELETE CASCADE
    );

-- Accounts and profiles
CREATE TABLE IF NOT EXISTS tb_distributor_account (
                                                      id           SERIAL PRIMARY KEY,
                                                      role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     TEXT         NOT NULL,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_verified  BOOLEAN      DEFAULT FALSE,
    is_active    BOOLEAN      DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS tb_retailer_account (
                                                   id           SERIAL PRIMARY KEY,
                                                   role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     TEXT         NOT NULL,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_verified  BOOLEAN      DEFAULT FALSE,
    is_active    BOOLEAN      DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS tb_distributor_info (
                                                   id                     SERIAL PRIMARY KEY,
                                                   distributor_account_id INTEGER      NOT NULL REFERENCES tb_distributor_account (id) ON DELETE CASCADE,
    first_name             VARCHAR(150),
    last_name              VARCHAR(150),
    gender                 VARCHAR(20),
    profile_image          TEXT,
    primary_phone_number   VARCHAR(50),
    created_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_retailer_info (
                                                id                     SERIAL PRIMARY KEY,
                                                retailer_account_id    INTEGER      NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
    first_name             VARCHAR(150),
    last_name              VARCHAR(150),
    gender                 VARCHAR(20),
    address                TEXT,
    primary_phone_number   VARCHAR(50),
    profile_image          TEXT,
    created_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_distributor_phone (
                                                    id                    SERIAL PRIMARY KEY,
                                                    distributor_info_id   INTEGER NOT NULL REFERENCES tb_distributor_info (id) ON DELETE CASCADE,
    phone_number          VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS tb_retailer_phone (
                                                 id                 SERIAL PRIMARY KEY,
                                                 retailer_info_id   INTEGER NOT NULL REFERENCES tb_retailer_info (id) ON DELETE CASCADE,
    phone_number       VARCHAR(50) NOT NULL
    );

-- Stores and catalog
CREATE TABLE IF NOT EXISTS tb_store (
                                        id                     SERIAL PRIMARY KEY,
                                        distributor_account_id INTEGER      NOT NULL REFERENCES tb_distributor_account (id) ON DELETE CASCADE,
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
                                              id        SERIAL PRIMARY KEY,
                                              store_id  INTEGER NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    phone     VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS tb_store_category (
                                                 id          SERIAL PRIMARY KEY,
                                                 store_id    INTEGER NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES tb_category (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS tb_store_product_detail (
                                                       id           SERIAL PRIMARY KEY,
                                                       store_id     INTEGER      NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    product_id   INTEGER      NOT NULL REFERENCES tb_product (id) ON DELETE CASCADE,
    qty          INTEGER      DEFAULT 0,
    price        DOUBLE PRECISION DEFAULT 0,
    is_publish   BOOLEAN      DEFAULT FALSE,
    image        TEXT,
    category_id  INTEGER REFERENCES tb_category (id),
    description  TEXT,
    is_active    BOOLEAN      DEFAULT TRUE,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
    );

-- Imports
CREATE TABLE IF NOT EXISTS tb_product_import (
                                                 id           SERIAL PRIMARY KEY,
                                                 created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 store_id     INTEGER   NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS tb_product_import_detail (
                                                        id                SERIAL PRIMARY KEY,
                                                        product_id        INTEGER NOT NULL REFERENCES tb_product (id) ON DELETE CASCADE,
    product_import_id INTEGER NOT NULL REFERENCES tb_product_import (id) ON DELETE CASCADE,
    qty               INTEGER NOT NULL,
    price             DOUBLE PRECISION NOT NULL,
    category_id       INTEGER REFERENCES tb_category (id)
    );

-- Orders
CREATE TABLE IF NOT EXISTS tb_order (
                                        id                  SERIAL PRIMARY KEY,
                                        retailer_account_id INTEGER      NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
    store_id            INTEGER      NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    status_id           INTEGER      NOT NULL REFERENCES tb_status (id),
    created_date        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    total_price         DOUBLE PRECISION DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS tb_order_detail (
                                               id                SERIAL PRIMARY KEY,
                                               order_id          INTEGER NOT NULL REFERENCES tb_order (id) ON DELETE CASCADE,
    qty               INTEGER NOT NULL,
    unit_price        DOUBLE PRECISION NOT NULL,
    store_product_id  INTEGER NOT NULL REFERENCES tb_store_product_detail (id)
    );

-- Ratings and bookmarks
CREATE TABLE IF NOT EXISTS tb_rating_detail (
                                                id             SERIAL PRIMARY KEY,
                                                store_id       INTEGER NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    retailer_id    INTEGER NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
    rated_star     INTEGER CHECK (rated_star BETWEEN 1 AND 5),
    comment        TEXT,
    created_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_bookmark (
                                           id                   SERIAL PRIMARY KEY,
                                           store_id             INTEGER NOT NULL REFERENCES tb_store (id) ON DELETE CASCADE,
    retailer_account_id  INTEGER NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
    created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Notifications
CREATE TABLE IF NOT EXISTS tb_notification_type (
                                                    id   SERIAL PRIMARY KEY,
                                                    name VARCHAR(100) NOT NULL UNIQUE,
    template TEXT
    );

CREATE TABLE IF NOT EXISTS tb_distributor_notification (
                                                           id                SERIAL PRIMARY KEY,
                                                           distributor_id    INTEGER NOT NULL REFERENCES tb_distributor_account (id) ON DELETE CASCADE,
    type_id           INTEGER NOT NULL REFERENCES tb_notification_type (id),
    content           TEXT,
    is_read           BOOLEAN DEFAULT FALSE,
    created_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_retailer_notification (
                                                        id             SERIAL PRIMARY KEY,
                                                        retailer_id    INTEGER NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
    type_id        INTEGER NOT NULL REFERENCES tb_notification_type (id),
    content        TEXT,
    is_read        BOOLEAN DEFAULT FALSE,
    created_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- OTPs
CREATE TABLE IF NOT EXISTS tb_distributor_otp (
                                                  id                       SERIAL PRIMARY KEY,
                                                  distributor_account_id   INTEGER      NOT NULL REFERENCES tb_distributor_account (id) ON DELETE CASCADE,
    otp_code                 INTEGER      NOT NULL,
    distributor_email        VARCHAR(255) NOT NULL,
    created_date             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tb_retailer_otp (
                                               id                    SERIAL PRIMARY KEY,
                                               retailer_account_id   INTEGER      NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
    otp_code              INTEGER      NOT NULL,
    retailer_email        VARCHAR(255) NOT NULL,
    created_date          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
    );

-- Seed basic lookup data
INSERT INTO tb_role (id, name) VALUES (1, 'DISTRIBUTOR') ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_role (id, name) VALUES (2, 'RETAILER') ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_status (id, name) VALUES
                                     (1, 'PENDING'),
                                     (2, 'PROCESSING'),
                                     (3, 'CONFIRMED'),
                                     (4, 'SHIPPING'),
                                     (5, 'DELIVERED'),
                                     (6, 'COMPLETED'),
                                     (7, 'CANCELLED'),
                                     (8, 'REJECTED'),
                                     (9, 'DRAFT')
    ON CONFLICT (id) DO NOTHING;
