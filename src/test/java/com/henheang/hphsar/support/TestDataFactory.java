package com.henheang.hphsar.support;

import org.springframework.jdbc.core.JdbcTemplate;

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

    public static int insertOrder(JdbcTemplate jdbc, int buyerAccountId, int storeId, int statusId) {
        return jdbc.queryForObject(
                "INSERT INTO tb_order (buyer_account_id, store_id, status_id) VALUES (?, ?, ?) RETURNING id",
                Integer.class, buyerAccountId, storeId, statusId);
    }

    public static Integer getOrderStatus(JdbcTemplate jdbc, int orderId) {
        return jdbc.queryForObject("SELECT status_id FROM tb_order WHERE id = ?", Integer.class, orderId);
    }

    public static boolean orderExists(JdbcTemplate jdbc, int orderId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM tb_order WHERE id = ?)", Boolean.class, orderId));
    }
}
