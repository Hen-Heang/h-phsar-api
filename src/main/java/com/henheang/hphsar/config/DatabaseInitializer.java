package com.henheang.hphsar.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseInitializer — Auto Schema Setup on Startup
 * <p>
 * Runs SQL statements once when the application starts to ensure the
 * database schema is ready before any request is handled.
 * <p>
 * Why use this instead of Flyway/Liquibase?
 *   - Simple approach for small, safe schema additions using IF NOT EXISTS
 *   - Safe to run multiple times (no side effects)
 *   - Good for adding columns or tables that may be missing in older deployments
 * <p>
 * When does it run?
 *   @PostConstruct → runs automatically after Spring injects all dependencies,
 *   but before the app starts handling HTTP requests.
 * <p>
 * SUPPLIER/BUYER RENAME (terminology migration):
 *   This class also carries the one-time rename of every "distributor"/"retailer"
 *   named table and column to "supplier"/"buyer". The renames use
 *   ALTER TABLE IF EXISTS ... RENAME TO/RENAME COLUMN so they are safe to run
 *   against a database that still has the old names (renames it once, then is a
 *   no-op on every later boot) AND safe to run against a brand-new database that
 *   already has the new names (nothing to rename, no-op immediately).
 * <p>
 * What it sets up:
 *   - tb_supplier_otp: stores OTP codes sent to suppliers (for password reset / verification)
 *   - tb_buyer_otp: stores OTP codes sent to buyers
 *   - tb_store.is_active: adds a soft-delete column if missing
 *   - tb_store.phone: adds phone column if missing
 */
@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeDatabaseSchema() {

        renameLegacyDistributorRetailerObjects();

        // OTP table for suppliers — used to verify email or reset password
        // Links to tb_supplier_account and deletes OTP when account is deleted
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_supplier_otp (
                    id                     SERIAL PRIMARY KEY,
                    supplier_account_id    INTEGER NOT NULL REFERENCES tb_supplier_account (id) ON DELETE CASCADE,
                    otp_code               INTEGER NOT NULL,
                    supplier_email         VARCHAR(255) NOT NULL,
                    created_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // OTP table for buyers — same purpose as supplier OTP
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_buyer_otp (
                    id                   SERIAL PRIMARY KEY,
                    buyer_account_id     INTEGER NOT NULL REFERENCES tb_buyer_account (id) ON DELETE CASCADE,
                    otp_code             INTEGER NOT NULL,
                    buyer_email          VARCHAR(255) NOT NULL,
                    created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // Add is_active column to tb_store (soft delete — FALSE means store is deactivated, not deleted)
        jdbcTemplate.execute("""
                ALTER TABLE tb_store
                ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
                """);

        // Add phone column to tb_store for store contact information
        jdbcTemplate.execute("""
                ALTER TABLE tb_store
                ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
                """);

        // Ensure notification tables exist with the correct schema.
        // Creates them fresh if missing; renames FK columns if they were created
        // with old names (buyer_account_id / supplier_account_id).
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_notification_type (
                    id       SERIAL PRIMARY KEY,
                    name     VARCHAR(100) NOT NULL UNIQUE,
                    template TEXT
                );
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_supplier_notification (
                    id             SERIAL PRIMARY KEY,
                    supplier_id    INTEGER NOT NULL REFERENCES tb_supplier_account(id) ON DELETE CASCADE,
                    type_id        INTEGER NOT NULL REFERENCES tb_notification_type(id),
                    content        TEXT,
                    is_read        BOOLEAN DEFAULT FALSE,
                    created_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_buyer_notification (
                    id          SERIAL PRIMARY KEY,
                    buyer_id    INTEGER NOT NULL REFERENCES tb_buyer_account(id) ON DELETE CASCADE,
                    type_id     INTEGER NOT NULL REFERENCES tb_notification_type(id),
                    content     TEXT,
                    is_read     BOOLEAN DEFAULT FALSE,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // Seed order status lookup table (idempotent)
        jdbcTemplate.execute("""
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
                """);

        // Seed notification types used throughout the order flow (idempotent via ON CONFLICT DO NOTHING)
        jdbcTemplate.execute("""
                INSERT INTO tb_notification_type (id, name, template) VALUES
                    (1,  'Order Received',    NULL),
                    (2,  'Out of Stock',      NULL),
                    (3,  'New Order',         NULL),
                    (4,  'Order Accepted',    NULL),
                    (5,  'Order Declined',    NULL),
                    (6,  'Order Preparing',   NULL),
                    (7,  'Order Dispatching', NULL),
                    (8,  'Order Arrived',     NULL),
                    (9,  'Order Complete',    NULL)
                ON CONFLICT (id) DO NOTHING;
                """);

        // Add order_id FK to notification tables so notifications can link to specific orders
        jdbcTemplate.execute("""
                ALTER TABLE tb_supplier_notification
                ADD COLUMN IF NOT EXISTS order_id INTEGER;
                """);

        jdbcTemplate.execute("""
                ALTER TABLE tb_buyer_notification
                ADD COLUMN IF NOT EXISTS order_id INTEGER;
                """);

        // Rename old FK/type columns to match the mapper if tables were created with legacy names
        // (covers both the very old "*_account_id" name and the once-already-migrated "distributor_id"/"retailer_id" name)
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'tb_supplier_notification'
                          AND column_name = 'distributor_account_id'
                    ) THEN
                        ALTER TABLE tb_supplier_notification
                            RENAME COLUMN distributor_account_id TO supplier_id;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'tb_supplier_notification'
                          AND column_name = 'distributor_id'
                    ) THEN
                        ALTER TABLE tb_supplier_notification
                            RENAME COLUMN distributor_id TO supplier_id;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'tb_buyer_notification'
                          AND column_name = 'retailer_account_id'
                    ) THEN
                        ALTER TABLE tb_buyer_notification
                            RENAME COLUMN retailer_account_id TO buyer_id;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'tb_buyer_notification'
                          AND column_name = 'retailer_id'
                    ) THEN
                        ALTER TABLE tb_buyer_notification
                            RENAME COLUMN retailer_id TO buyer_id;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'tb_supplier_notification'
                          AND column_name = 'notification_type_id'
                    ) THEN
                        ALTER TABLE tb_supplier_notification
                            RENAME COLUMN notification_type_id TO type_id;
                    END IF;

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'tb_buyer_notification'
                          AND column_name = 'notification_type_id'
                    ) THEN
                        ALTER TABLE tb_buyer_notification
                            RENAME COLUMN notification_type_id TO type_id;
                    END IF;
                END $$;
                """);

        // Keep the role labels in sync with the new terminology (roleId values are unchanged: 1, 2)
        jdbcTemplate.execute("UPDATE tb_role SET name = 'SUPPLIER' WHERE id = 1;");
        jdbcTemplate.execute("UPDATE tb_role SET name = 'BUYER' WHERE id = 2;");
    }

    /**
     * One-time rename of every "distributor"/"retailer" named table and FK column
     * to "supplier"/"buyer", for databases that were set up before this migration.
     * <p>
     * ALTER TABLE IF EXISTS ... RENAME TO ... is safe to run on every boot:
     * the first run renames the table, every run after that finds nothing under
     * the old name and does nothing. Renaming a table does not break its foreign
     * keys — Postgres tracks them by object id, not by name.
     */
    private void renameLegacyDistributorRetailerObjects() {

        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_distributor_account RENAME TO tb_supplier_account;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_retailer_account RENAME TO tb_buyer_account;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_distributor_info RENAME TO tb_supplier_info;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_retailer_info RENAME TO tb_buyer_info;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_distributor_phone RENAME TO tb_supplier_phone;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_retailer_phone RENAME TO tb_buyer_phone;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_distributor_otp RENAME TO tb_supplier_otp;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_retailer_otp RENAME TO tb_buyer_otp;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_distributor_notification RENAME TO tb_supplier_notification;");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_retailer_notification RENAME TO tb_buyer_notification;");

        // Column renames — guarded individually since ALTER TABLE ... RENAME COLUMN
        // has no native IF EXISTS clause for the column itself.
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_supplier_info' AND column_name = 'distributor_account_id') THEN
                        ALTER TABLE tb_supplier_info RENAME COLUMN distributor_account_id TO supplier_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_buyer_info' AND column_name = 'retailer_account_id') THEN
                        ALTER TABLE tb_buyer_info RENAME COLUMN retailer_account_id TO buyer_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_supplier_phone' AND column_name = 'distributor_info_id') THEN
                        ALTER TABLE tb_supplier_phone RENAME COLUMN distributor_info_id TO supplier_info_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_buyer_phone' AND column_name = 'retailer_info_id') THEN
                        ALTER TABLE tb_buyer_phone RENAME COLUMN retailer_info_id TO buyer_info_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_supplier_otp' AND column_name = 'distributor_account_id') THEN
                        ALTER TABLE tb_supplier_otp RENAME COLUMN distributor_account_id TO supplier_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_supplier_otp' AND column_name = 'distributor_email') THEN
                        ALTER TABLE tb_supplier_otp RENAME COLUMN distributor_email TO supplier_email;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_buyer_otp' AND column_name = 'retailer_account_id') THEN
                        ALTER TABLE tb_buyer_otp RENAME COLUMN retailer_account_id TO buyer_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_buyer_otp' AND column_name = 'retailer_email') THEN
                        ALTER TABLE tb_buyer_otp RENAME COLUMN retailer_email TO buyer_email;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_store' AND column_name = 'distributor_account_id') THEN
                        ALTER TABLE tb_store RENAME COLUMN distributor_account_id TO supplier_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_order' AND column_name = 'retailer_account_id') THEN
                        ALTER TABLE tb_order RENAME COLUMN retailer_account_id TO buyer_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_bookmark' AND column_name = 'retailer_account_id') THEN
                        ALTER TABLE tb_bookmark RENAME COLUMN retailer_account_id TO buyer_account_id;
                    END IF;

                    -- tb_rating_detail: the original create_all_tables.sql/schema.sql/table.sql called this
                    -- column "retailer_id" while every mapper query actually used "retailer_account_id" —
                    -- pre-existing drift between the baseline script and the real schema. Both legacy names
                    -- are handled here; the column is now called "buyer_account_id" everywhere.
                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_rating_detail' AND column_name = 'retailer_id') THEN
                        ALTER TABLE tb_rating_detail RENAME COLUMN retailer_id TO buyer_account_id;
                    END IF;

                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_name = 'tb_rating_detail' AND column_name = 'retailer_account_id') THEN
                        ALTER TABLE tb_rating_detail RENAME COLUMN retailer_account_id TO buyer_account_id;
                    END IF;
                END $$;
                """);
    }
}
