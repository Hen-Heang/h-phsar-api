package com.henheang.hphsar.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
 *   - tb_store_product_detail.qty: enforced NOT NULL + CHECK (qty >= 0) (Step 3B —
 *     defense in depth behind the atomic guarded UPDATE that's the real concurrency fix)
 *   - tb_order_detail: UNIQUE(order_id, store_product_id) when no existing violation,
 *     plus an index on order_id used by stock deduction during order acceptance
 *   - tb_refresh_token: opaque refresh tokens (hashed) backing POST /authorization/refresh
 */
@Slf4j
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

        createAdminAccountTable();
        createRefreshTokenTable();

        enforceStoreProductQtyInvariant();
        enforceOrderDetailUniqueness();
        addOrderDetailOrderIdIndex();

        migrateOrderLifecycleStatuses();
        createOrderStatusHistoryTable();
    }

    /**
     * STEP 3C — order lifecycle status correction.
     * <p>
     * The ORIGINAL seed (still in the very first jdbcTemplate.execute block
     * below, in the class's initial rows) named the nine statuses PENDING,
     * PROCESSING, CONFIRMED, SHIPPING, DELIVERED, COMPLETED, CANCELLED,
     * REJECTED, DRAFT for ids 1-9. Auditing every status_id reference in the
     * mapper layer showed the CODE never agreed with those names:
     * <p>
     * - id 9 is used everywhere as the active, editable CART (checkForCart,
     *   createCart, cancelCart, isCartExist, ...), not "DRAFT".
     * - id 8 is used as the saved DRAFT (saveToDraft, submitDraftByIdAndBuyerId,
     *   existsDraftByIdAndBuyerId, ...) in most places — but SupplierOrderMapper's
     *   declinePendingOrder ALSO writes id 8 to mean "supplier rejected this
     *   order". Two unrelated terminal/non-terminal meanings sharing one id is
     *   the single most severe finding of the audit: a rejected order and a
     *   saved draft were indistinguishable by status_id alone.
     * - id 3 is used as DISPATCHED (finishPreparing writes it, getDispatchingOrders
     *   reads it), not "CONFIRMED".
     * - ids 4 (SHIPPING) and 5 (DELIVERED) are read by several precondition/report
     *   queries but NO code path ever writes them — status_id 4 and 5 are dead,
     *   unreachable states. (One concrete downstream bug this caused: buyer's
     *   confirmTransaction() required status 4 to proceed, which could never be
     *   true, making that endpoint — and the invoice view gated on status 5 —
     *   permanently unreachable.)
     * <p>
     * This method (a) splits the id-8 collision by moving genuinely-rejected
     * orders to a new id 10 = REJECTED, using evidence already in the
     * database (a rejected order always has a "Order Declined" (type_id=5)
     * buyer notification — saved drafts never generate any notification at
     * all), and (b) renames ids 3/8/9 to match what the code has always
     * actually done with them. ids 4/5 are left alone (orphaned/unused) rather
     * than reused, since any pre-existing row sitting on those unreachable
     * ids deserves a human look, not a guess.
     * <p>
     * Every step is guarded to be a no-op on the next boot. Transitional
     * technical debt — see the class Javadoc on why this isn't a Flyway
     * migration yet.
     */
    private void migrateOrderLifecycleStatuses() {
        // Order matters: tb_status.name is UNIQUE, so each rename must free up
        // its target name before another row claims it, and the id-8 split
        // must happen before the old "REJECTED" name is reused for id 10.

        // 1) Free "DRAFT" (currently on id 9) by renaming id 9 to its real meaning, CART.
        jdbcTemplate.execute("UPDATE tb_status SET name = 'CART' WHERE id = 9 AND name = 'DRAFT';");

        // 2) Now safe: rename id 8 from "REJECTED" to its dominant real meaning, DRAFT.
        //    This also frees the "REJECTED" name for step 4.
        jdbcTemplate.execute("UPDATE tb_status SET name = 'DRAFT' WHERE id = 8 AND name = 'REJECTED';");

        // 3) Independent rename: id 3 "CONFIRMED" -> DISPATCHED.
        jdbcTemplate.execute("UPDATE tb_status SET name = 'DISPATCHED' WHERE id = 3 AND name = 'CONFIRMED';");

        // 4) Introduce the dedicated REJECTED id, now that the name is free.
        jdbcTemplate.execute("INSERT INTO tb_status (id, name) VALUES (10, 'REJECTED') ON CONFLICT (id) DO NOTHING;");

        // 5) Split the id-8 collision: move truly-rejected orders to the new id.
        //    Evidence: declinePendingOrder is the ONLY code path that ever creates
        //    a buyer notification of type_id 5 ("Order Declined"); saveToDraft
        //    creates no notification at all. Presence of that notification is
        //    reliable proof this order was rejected, not saved as a draft.
        Integer rejectedToMigrate = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM tb_order o
                WHERE o.status_id = 8
                  AND EXISTS (SELECT 1 FROM tb_buyer_notification n
                              WHERE n.order_id = o.id AND n.type_id = 5)
                """, Integer.class);
        Integer remainingAsDraft = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM tb_order o
                WHERE o.status_id = 8
                  AND NOT EXISTS (SELECT 1 FROM tb_buyer_notification n
                                  WHERE n.order_id = o.id AND n.type_id = 5)
                """, Integer.class);
        if (rejectedToMigrate != null && rejectedToMigrate > 0) {
            log.warn("Order lifecycle migration: moving {} order(s) with status_id=8 to the new " +
                    "REJECTED id=10 (evidence: an 'Order Declined' notification exists for each). " +
                    "{} other order(s) remain classified as DRAFT (no such notification found).",
                    rejectedToMigrate, remainingAsDraft);
        }
        jdbcTemplate.execute("""
                UPDATE tb_order o
                SET status_id = 10
                WHERE o.status_id = 8
                  AND EXISTS (SELECT 1 FROM tb_buyer_notification n
                              WHERE n.order_id = o.id AND n.type_id = 5);
                """);
    }

    /**
     * STEP 3C — append-only order status history (audit trail). Every status
     * change from now on is recorded by OrderStatusServiceImpl; nothing else
     * writes to this table. changed_by_account_id intentionally has no foreign
     * key: suppliers and buyers live in two separate account tables, and one
     * FK can't conditionally point at either without the unified-account
     * redesign, which is out of scope here (Option A from the audit — the
     * lowest-migration-risk choice).
     */
    private void createOrderStatusHistoryTable() {
        jdbcTemplate.execute("""
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
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON tb_order_status_history(order_id);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_order_status_history_changed_at ON tb_order_status_history(changed_at);");
        // Supported by real query patterns: nearly every order listing/count query
        // filters by status_id (getPendingOrders, getDispatchingOrders, dashboard
        // counts, buyer/supplier history, ...).
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_order_status ON tb_order(status_id);");
    }

    /**
     * STEP 3B — non-negative/non-null stock invariant.
     * <p>
     * This is defense in depth, not the concurrency fix itself: the atomic
     * guarded UPDATE in SupplierOrderRepository#deductStockIfAvailable is what
     * actually prevents overselling. This CHECK constraint only guarantees the
     * column can never end up negative/null through any OTHER write path
     * (including ones added later by mistake).
     * <p>
     * Transitional technical debt: this belongs in a proper Flyway migration.
     * It lives here, guarded and idempotent, only because Flyway isn't wired up
     * yet in this project (see class Javadoc) — do not treat this as the
     * long-term home for schema changes.
     */
    private void enforceStoreProductQtyInvariant() {
        Integer invalidRows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM tb_store_product_detail WHERE qty IS NULL OR qty < 0
                """, Integer.class);
        if (invalidRows != null && invalidRows > 0) {
            // Not a silent conversion: negative/null qty is already an invalid,
            // corrupt state under the app's model (qty represents available units).
            // Clamping to 0 is the safest recovery, and it's logged so operators
            // can investigate how it happened.
            log.warn("Normalizing {} tb_store_product_detail row(s) with NULL/negative qty to 0 " +
                    "before enforcing the non-negative stock constraint.", invalidRows);
            jdbcTemplate.execute("UPDATE tb_store_product_detail SET qty = 0 WHERE qty IS NULL OR qty < 0;");
        }

        jdbcTemplate.execute("ALTER TABLE tb_store_product_detail ALTER COLUMN qty SET DEFAULT 0;");
        jdbcTemplate.execute("ALTER TABLE tb_store_product_detail ALTER COLUMN qty SET NOT NULL;");

        Boolean checkConstraintExists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_store_product_qty_non_negative')
                """, Boolean.class);
        if (Boolean.FALSE.equals(checkConstraintExists)) {
            jdbcTemplate.execute("""
                    ALTER TABLE tb_store_product_detail
                    ADD CONSTRAINT chk_store_product_qty_non_negative CHECK (qty >= 0);
                    """);
        }
    }

    /**
     * STEP 3B — one order should not carry two separate line items for the same
     * store-specific product. Only added when no existing data already violates
     * it — this project has never had a way to create such duplicates through
     * normal use (the cart flow checks productIsInCart before insert-vs-update),
     * but we don't assume the database is clean. If a violation is found, the
     * constraint is skipped (not force-applied) and logged for manual cleanup.
     */
    private void enforceOrderDetailUniqueness() {
        Boolean constraintExists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_order_detail_order_store_product')
                """, Boolean.class);
        if (Boolean.TRUE.equals(constraintExists)) {
            return;
        }

        Integer duplicateGroups = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT order_id, store_product_id
                    FROM tb_order_detail
                    GROUP BY order_id, store_product_id
                    HAVING COUNT(*) > 1
                ) dup
                """, Integer.class);
        if (duplicateGroups != null && duplicateGroups > 0) {
            log.warn("Skipping UNIQUE(order_id, store_product_id) on tb_order_detail: {} existing " +
                    "duplicate group(s) found. Resolve manually, then this constraint will apply on next boot.",
                    duplicateGroups);
            return;
        }

        jdbcTemplate.execute("""
                ALTER TABLE tb_order_detail
                ADD CONSTRAINT uq_order_detail_order_store_product UNIQUE (order_id, store_product_id);
                """);
    }

    /**
     * STEP 3B — order acceptance reads every line item for an order
     * (tb_order_detail.order_id) inside the transaction that also holds the
     * per-item stock-deduction locks; an unindexed scan here directly extends
     * that critical section. Postgres does not auto-index foreign key columns.
     */
    private void addOrderDetailOrderIdIndex() {
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_order_detail_order_id ON tb_order_detail (order_id);");
    }

    /**
     * ADMIN role support (login, plus Phase 2 supplier/buyer account management
     * in AdminAccountController — see service/implement/AdminAccountServiceImpl.java).
     * <p>
     * tb_admin_account mirrors tb_supplier_account/tb_buyer_account so the existing
     * shared login flow (JwtUserDetailsServiceImpl) can treat admin the same way.
     * There is no public registration endpoint for this table by design — the first
     * admin row must be inserted manually (see docs), since anyone hitting a public
     * "create admin" endpoint would be a privilege-escalation hole.
     */
    private void createAdminAccountTable() {
        jdbcTemplate.execute("INSERT INTO tb_role (id, name) VALUES (3, 'ADMIN') ON CONFLICT (id) DO NOTHING;");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_admin_account (
                    id           SERIAL PRIMARY KEY,
                    role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
                    email        VARCHAR(255) NOT NULL UNIQUE,
                    password     TEXT         NOT NULL,
                    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                    is_verified  BOOLEAN      DEFAULT TRUE,
                    is_active    BOOLEAN      DEFAULT TRUE
                );
                """);
    }

    /**
     * Refresh-token flow support (access + refresh tokens, POST /authorization/refresh,
     * POST /authorization/logout — see RefreshTokenService / JwtAuthenticationController).
     * <p>
     * A single table, not split per role like the account tables: this is a
     * cross-cutting session concept, not an account type, so email + role_id
     * together identify the account (email is what loadUserByUsername keys off
     * of; role_id is captured at issuance so it doesn't need a re-lookup on
     * refresh). token_hash is a SHA-256 hex digest of the raw token — the raw
     * value itself is never persisted.
     */
    private void createRefreshTokenTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_refresh_token (
                    id           SERIAL PRIMARY KEY,
                    email        VARCHAR(255) NOT NULL,
                    role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
                    token_hash   VARCHAR(64)  NOT NULL UNIQUE,
                    expires_at   TIMESTAMP    NOT NULL,
                    revoked_at   TIMESTAMP,
                    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
                );
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_refresh_token_email ON tb_refresh_token(email);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at ON tb_refresh_token(expires_at);");
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
