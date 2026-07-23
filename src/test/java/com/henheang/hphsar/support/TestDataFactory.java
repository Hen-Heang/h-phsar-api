package com.henheang.hphsar.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Minimal raw-JDBC fixture helpers shared by the SQL-injection and
 * draft-ownership integration tests. Deliberately bypasses the app's own
 * validation/business logic — these tests are about the mapper/repository/
 * service layer under test, not about re-exercising registration or catalog
 * rules to set up fixtures.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static int insertSupplierAccount(JdbcTemplate jdbc, String email) {
        return jdbc.queryForObject(
                "INSERT INTO tb_supplier_account (role_id, email, password, is_verified, is_active) " +
                        "VALUES (1, ?, 'test-hash', true, true) RETURNING id",
                Integer.class, email);
    }

    public static int insertBuyerAccount(JdbcTemplate jdbc, String email) {
        return jdbc.queryForObject(
                "INSERT INTO tb_buyer_account (role_id, email, password, is_verified, is_active) " +
                        "VALUES (2, ?, 'test-hash', true, true) RETURNING id",
                Integer.class, email);
    }

    public static int insertStore(JdbcTemplate jdbc, int supplierAccountId, String name) {
        return jdbc.queryForObject(
                "INSERT INTO tb_store (supplier_account_id, name) VALUES (?, ?) RETURNING id",
                Integer.class, supplierAccountId, name);
    }

    public static int insertCategory(JdbcTemplate jdbc, String name) {
        return jdbc.queryForObject(
                "INSERT INTO tb_category (name) VALUES (?) RETURNING id",
                Integer.class, name);
    }

    public static void linkStoreCategory(JdbcTemplate jdbc, int storeId, int categoryId) {
        jdbc.update("INSERT INTO tb_store_category (store_id, category_id) VALUES (?, ?)", storeId, categoryId);
    }

    public static int insertProduct(JdbcTemplate jdbc, String name) {
        return jdbc.queryForObject(
                "INSERT INTO tb_product (name) VALUES (?) RETURNING id",
                Integer.class, name);
    }

    public static void linkStoreProduct(JdbcTemplate jdbc, int storeId, int productId) {
        jdbc.update("INSERT INTO tb_store_product_detail (store_id, product_id, qty, price, is_publish) " +
                "VALUES (?, ?, 10, 1.0, true)", storeId, productId);
    }

    /** Same as {@link #linkStoreProduct}, but with a caller-chosen qty and returning the store_product_detail id. */
    public static int insertStoreProduct(JdbcTemplate jdbc, int storeId, int productId, int qty) {
        return jdbc.queryForObject(
                "INSERT INTO tb_store_product_detail (store_id, product_id, qty, price, is_publish) " +
                        "VALUES (?, ?, ?, 1.0, true) RETURNING id",
                Integer.class, storeId, productId, qty);
    }

    public static Integer getStoreProductQty(JdbcTemplate jdbc, int storeProductId) {
        return jdbc.queryForObject("SELECT qty FROM tb_store_product_detail WHERE id = ?", Integer.class, storeProductId);
    }

    public static int insertOrder(JdbcTemplate jdbc, int buyerAccountId, int storeId, int statusId) {
        return jdbc.queryForObject(
                "INSERT INTO tb_order (buyer_account_id, store_id, status_id) VALUES (?, ?, ?) RETURNING id",
                Integer.class, buyerAccountId, storeId, statusId);
    }

    /** Inserts a raw order_detail row directly — bypasses cart validation, used to seed deterministic order fixtures. */
    public static void insertOrderDetail(JdbcTemplate jdbc, int orderId, int storeProductId, int qty, double unitPrice) {
        jdbc.update("INSERT INTO tb_order_detail (order_id, qty, unit_price, store_product_id) VALUES (?, ?, ?, ?)",
                orderId, qty, unitPrice, storeProductId);
    }

    public static Integer getOrderStatus(JdbcTemplate jdbc, int orderId) {
        return jdbc.queryForObject("SELECT status_id FROM tb_order WHERE id = ?", Integer.class, orderId);
    }

    public static boolean orderExists(JdbcTemplate jdbc, int orderId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM tb_order WHERE id = ?)", Boolean.class, orderId));
    }

    public static int countBuyerNotifications(JdbcTemplate jdbc, int orderId, int typeId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tb_buyer_notification WHERE order_id = ? AND type_id = ?",
                Integer.class, orderId, typeId);
        return count == null ? 0 : count;
    }

    /** Step 3C: (previousStatus, newStatus, changedByRole) tuples for one order, oldest first. */
    public static List<String[]> getStatusHistory(JdbcTemplate jdbc, int orderId) {
        return jdbc.query("""
                        SELECT ps.name AS previousStatus, ns.name AS newStatus, h.changed_by_role AS changedByRole
                        FROM tb_order_status_history h
                                 LEFT JOIN tb_status ps ON h.previous_status_id = ps.id
                                 JOIN tb_status ns ON h.new_status_id = ns.id
                        WHERE h.order_id = ?
                        ORDER BY h.changed_at ASC, h.id ASC
                        """,
                (rs, rowNum) -> new String[]{rs.getString("previousStatus"), rs.getString("newStatus"), rs.getString("changedByRole")},
                orderId);
    }
}
