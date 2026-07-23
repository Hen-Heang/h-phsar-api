package com.henheang.hphsar.service;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.repository.SupplierOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.henheang.hphsar.support.TestDataFactory.countBuyerNotifications;
import static com.henheang.hphsar.support.TestDataFactory.getOrderStatus;
import static com.henheang.hphsar.support.TestDataFactory.getStoreProductQty;
import static com.henheang.hphsar.support.TestDataFactory.insertBuyerAccount;
import static com.henheang.hphsar.support.TestDataFactory.insertOrder;
import static com.henheang.hphsar.support.TestDataFactory.insertOrderDetail;
import static com.henheang.hphsar.support.TestDataFactory.insertProduct;
import static com.henheang.hphsar.support.TestDataFactory.insertStore;
import static com.henheang.hphsar.support.TestDataFactory.insertStoreProduct;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 3B — regression coverage for concurrency-safe stock deduction.
 * <p>
 * Covers: the atomic guarded UPDATE in isolation (Phase 15), multi-item
 * rollback under the Step 3A transaction boundary (Phase 16), a real
 * concurrent race against PostgreSQL (Phase 17-18), and the defense-in-depth
 * database CHECK constraint (Phase 19). Uses the real Testcontainers Postgres
 * instance from {@link AbstractIntegrationTest} — concurrency behavior is not
 * meaningful against an in-memory database.
 */
class StockDeductionIT extends AbstractIntegrationTest {

    private static final int PENDING_STATUS_ID = 1;
    private static final int ORDER_ACCEPTED_NOTIFICATION_TYPE = 4;

    @Autowired
    private SupplierOrderRepository supplierOrderRepository;

    @Autowired
    private SupplierOrderService supplierOrderService;

    @Autowired
    private JdbcTemplate jdbc;

    private int supplierAccountId;
    private int storeId;
    private int buyerAccountId;

    @BeforeEach
    void seedSupplierStoreAndBuyer() {
        supplierAccountId = insertSupplierAccount(jdbc, "supplier-" + System.nanoTime() + "@example.test");
        storeId = insertStore(jdbc, supplierAccountId, "Stock Test Store");
        buyerAccountId = insertBuyerAccount(jdbc, "buyer-" + System.nanoTime() + "@example.test");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAsSupplier() {
        AppUser principal = new AppUser(supplierAccountId, "supplier@example.test", "test-hash", "SUPPLIER", 1, true, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private int seedStoreProduct(int qty) {
        int productId = insertProduct(jdbc, "Product " + System.nanoTime());
        return insertStoreProduct(jdbc, storeId, productId, qty);
    }

    // ── Phase 15: the atomic guarded UPDATE, in isolation ───────────────────

    @Test
    void deductStockIfAvailable_sufficientStock_succeedsAndReducesQty() {
        int storeProductId = seedStoreProduct(10);

        int affected = supplierOrderRepository.deductStockIfAvailable(storeProductId, 4);

        assertEquals(1, affected);
        assertEquals(6, getStoreProductQty(jdbc, storeProductId));
    }

    @Test
    void deductStockIfAvailable_insufficientStock_failsAndLeavesQtyUnchanged() {
        int storeProductId = seedStoreProduct(10);

        int affected = supplierOrderRepository.deductStockIfAvailable(storeProductId, 11);

        assertEquals(0, affected);
        assertEquals(10, getStoreProductQty(jdbc, storeProductId));
    }

    @Test
    void deductStockIfAvailable_neverDrivesQuantityNegative() {
        int storeProductId = seedStoreProduct(3);

        // Repeated over-deduction attempts must all be rejected — qty must never go negative.
        for (int i = 0; i < 5; i++) {
            int affected = supplierOrderRepository.deductStockIfAvailable(storeProductId, 4);
            assertEquals(0, affected);
        }
        assertEquals(3, getStoreProductQty(jdbc, storeProductId));
    }

    @Test
    void acceptPendingOrder_zeroQuantityLineItem_isRejectedAndStockUnchanged() {
        int storeProductId = seedStoreProduct(10);
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING_STATUS_ID);
        // Seeded directly via raw JDBC: the normal cart flow can never produce a
        // qty=0 order line, but the service must still defend against corrupt/legacy data.
        insertOrderDetail(jdbc, orderId, storeProductId, 0, 1.0);
        loginAsSupplier();

        assertThrows(ConflictException.class, () -> supplierOrderService.acceptPendingOrder(orderId, storeId));

        assertEquals(10, getStoreProductQty(jdbc, storeProductId));
        assertEquals(PENDING_STATUS_ID, getOrderStatus(jdbc, orderId));
    }

    @Test
    void acceptPendingOrder_negativeQuantityLineItem_isRejectedAndStockUnchanged() {
        int storeProductId = seedStoreProduct(10);
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING_STATUS_ID);
        insertOrderDetail(jdbc, orderId, storeProductId, -1, 1.0);
        loginAsSupplier();

        assertThrows(ConflictException.class, () -> supplierOrderService.acceptPendingOrder(orderId, storeId));

        assertEquals(10, getStoreProductQty(jdbc, storeProductId));
        assertEquals(PENDING_STATUS_ID, getOrderStatus(jdbc, orderId));
    }

    // ── Phase 16: multi-item rollback ───────────────────────────────────────

    @Test
    void acceptPendingOrder_oneOfMultipleItemsInsufficient_rollsBackWholeOrder() {
        int productA = seedStoreProduct(10); // available 10
        int productB = seedStoreProduct(2);  // available 2
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING_STATUS_ID);
        insertOrderDetail(jdbc, orderId, productA, 3, 1.0); // requests 3 — fine alone
        insertOrderDetail(jdbc, orderId, productB, 5, 1.0); // requests 5 — insufficient
        loginAsSupplier();

        assertThrows(ConflictException.class, () -> supplierOrderService.acceptPendingOrder(orderId, storeId));

        // Nothing partially applied: neither product's stock moved, order is still pending,
        // and the acceptance notification was never committed.
        assertEquals(10, getStoreProductQty(jdbc, productA));
        assertEquals(2, getStoreProductQty(jdbc, productB));
        assertEquals(PENDING_STATUS_ID, getOrderStatus(jdbc, orderId));
        assertEquals(0, countBuyerNotifications(jdbc, orderId, ORDER_ACCEPTED_NOTIFICATION_TYPE));
    }

    // ── Phase 17: concurrent deduction against real PostgreSQL ──────────────

    @Test
    void deductStockIfAvailable_twoConcurrentDeductions_exactlyOneSucceeds() throws Exception {
        int storeProductId = seedStoreProduct(5);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Callable<Integer> attempt = () -> {
                barrier.await(5, TimeUnit.SECONDS); // both threads fire the UPDATE as close together as possible
                return supplierOrderRepository.deductStockIfAvailable(storeProductId, 4);
            };

            Future<Integer> resultA = pool.submit(attempt);
            Future<Integer> resultB = pool.submit(attempt);

            int affectedA = resultA.get(10, TimeUnit.SECONDS);
            int affectedB = resultB.get(10, TimeUnit.SECONDS);

            // Exactly one deduction succeeds (affected rows = 1), the other is rejected (0).
            assertEquals(1, affectedA + affectedB, "Exactly one of the two concurrent deductions must succeed");
            assertNotEquals(affectedA, affectedB);
            assertEquals(1, getStoreProductQty(jdbc, storeProductId));
        } finally {
            pool.shutdownNow();
        }
    }

    // ── Phase 18: concurrent order acceptance against real PostgreSQL ───────

    @Test
    void acceptPendingOrder_twoOrdersCompetingForSameStock_exactlyOneAccepted() throws Exception {
        int storeProductId = seedStoreProduct(5);
        int orderA = insertOrder(jdbc, buyerAccountId, storeId, PENDING_STATUS_ID);
        int orderB = insertOrder(jdbc, buyerAccountId, storeId, PENDING_STATUS_ID);
        insertOrderDetail(jdbc, orderA, storeProductId, 4, 1.0);
        insertOrderDetail(jdbc, orderB, storeProductId, 4, 1.0);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Callable<String> acceptA = () -> {
                loginAsSupplier();
                barrier.await(5, TimeUnit.SECONDS);
                return supplierOrderService.acceptPendingOrder(orderA, storeId);
            };
            Callable<String> acceptB = () -> {
                loginAsSupplier();
                barrier.await(5, TimeUnit.SECONDS);
                return supplierOrderService.acceptPendingOrder(orderB, storeId);
            };

            Future<String> futureA = pool.submit(acceptA);
            Future<String> futureB = pool.submit(acceptB);

            int succeeded = 0;
            int failed = 0;
            for (Future<String> future : List.of(futureA, futureB)) {
                try {
                    future.get(15, TimeUnit.SECONDS);
                    succeeded++;
                } catch (ExecutionException e) {
                    assertTrue(e.getCause() instanceof ConflictException,
                            "Losing acceptance must fail with the business ConflictException, not a raw DB error: " + e.getCause());
                    failed++;
                }
            }

            assertEquals(1, succeeded, "Exactly one order should be accepted");
            assertEquals(1, failed, "Exactly one order should fail to accept");
            assertEquals(1, getStoreProductQty(jdbc, storeProductId));

            // Total accepted-notifications across both orders must be exactly 1 — no partial/double notification.
            int totalAcceptedNotifications = countBuyerNotifications(jdbc, orderA, ORDER_ACCEPTED_NOTIFICATION_TYPE)
                    + countBuyerNotifications(jdbc, orderB, ORDER_ACCEPTED_NOTIFICATION_TYPE);
            assertEquals(1, totalAcceptedNotifications);

            // The losing order remains pending — it was never partially accepted.
            int pendingCount = (getOrderStatus(jdbc, orderA) == PENDING_STATUS_ID ? 1 : 0)
                    + (getOrderStatus(jdbc, orderB) == PENDING_STATUS_ID ? 1 : 0);
            assertEquals(1, pendingCount);
        } finally {
            pool.shutdownNow();
        }
    }

    // ── Phase 19: database CHECK constraint (defense in depth) ──────────────

    @Test
    void directNegativeQtyUpdate_isRejectedByDatabaseConstraint() {
        int storeProductId = seedStoreProduct(10);

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE tb_store_product_detail SET qty = -1 WHERE id = ?", storeProductId));

        // The rejected statement must not have partially applied.
        assertEquals(10, getStoreProductQty(jdbc, storeProductId));
    }

    @Test
    void directNonNegativeQtyUpdate_succeeds() {
        int storeProductId = seedStoreProduct(10);

        jdbc.update("UPDATE tb_store_product_detail SET qty = 0 WHERE id = ?", storeProductId);

        assertEquals(0, getStoreProductQty(jdbc, storeProductId));
    }
}
