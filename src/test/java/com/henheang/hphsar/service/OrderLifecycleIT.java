package com.henheang.hphsar.service;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.ForbiddenException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static com.henheang.hphsar.support.TestDataFactory.getStatusHistory;
import static com.henheang.hphsar.support.TestDataFactory.insertBuyerAccount;
import static com.henheang.hphsar.support.TestDataFactory.insertOrder;
import static com.henheang.hphsar.support.TestDataFactory.insertOrderDetail;
import static com.henheang.hphsar.support.TestDataFactory.insertProduct;
import static com.henheang.hphsar.support.TestDataFactory.insertStore;
import static com.henheang.hphsar.support.TestDataFactory.insertStoreProduct;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 3C — end-to-end order lifecycle coverage: full workflow (Phase 23),
 * rejection (24), cancellation (25), concurrent transition (26), and
 * ownership (27). Uses the real Testcontainers Postgres instance — the
 * concurrent test specifically needs real row locking, not an in-memory
 * substitute.
 * <p>
 * Statuses referenced by id in fixtures/assertions (Step 3C mapping):
 * 1=PENDING, 2=PROCESSING, 3=DISPATCHED, 6=COMPLETED, 7=CANCELLED, 8=DRAFT,
 * 9=CART, 10=REJECTED.
 */
class OrderLifecycleIT extends AbstractIntegrationTest {

    private static final int CART = 9;
    private static final int DRAFT = 8;
    private static final int PENDING = 1;
    private static final int PROCESSING = 2;
    private static final int DISPATCHED = 3;
    private static final int COMPLETED = 6;
    private static final int REJECTED = 10;
    private static final int CANCELLED = 7;
    private static final int ORDER_ACCEPTED_NOTIFICATION_TYPE = 4;
    private static final int ORDER_DECLINED_NOTIFICATION_TYPE = 5;

    @Autowired
    private BuyerOrderService buyerOrderService;

    @Autowired
    private SupplierOrderService supplierOrderService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private JdbcTemplate jdbc;

    private int supplierAccountId;
    private int storeId;
    private int buyerAccountId;

    @BeforeEach
    void seedSupplierStoreAndBuyer() {
        supplierAccountId = insertSupplierAccount(jdbc, "supplier-" + System.nanoTime() + "@example.test");
        storeId = insertStore(jdbc, supplierAccountId, "Lifecycle Test Store");
        buyerAccountId = insertBuyerAccount(jdbc, "buyer-" + System.nanoTime() + "@example.test");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAsBuyer() {
        AppUser principal = new AppUser(buyerAccountId, "buyer@example.test", "test-hash", "BUYER", 2, true, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private void loginAsSupplier() {
        AppUser principal = new AppUser(supplierAccountId, "supplier@example.test", "test-hash", "SUPPLIER", 1, true, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private int seedStoreProductAndOrderLine(int orderId, int qty) {
        int productId = insertProduct(jdbc, "Product " + System.nanoTime());
        int storeProductId = insertStoreProduct(jdbc, storeId, productId, 10);
        insertOrderDetail(jdbc, orderId, storeProductId, qty, 1.0);
        return storeProductId;
    }

    // ── Phase 23: full workflow ─────────────────────────────────────────────

    @Test
    void fullWorkflow_cartToCompleted_statusSequenceAndHistoryAreCorrect() throws Exception {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, CART);
        seedStoreProductAndOrderLine(orderId, 3);

        loginAsBuyer();
        buyerOrderService.saveToDraft(); // resolves the buyer's own single active cart internally
        assertEquals(DRAFT, getOrderStatus(jdbc, orderId));

        historyService.updateDraftById(orderId);
        assertEquals(PENDING, getOrderStatus(jdbc, orderId));

        loginAsSupplier();
        supplierOrderService.acceptPendingOrder(orderId, storeId);
        assertEquals(PROCESSING, getOrderStatus(jdbc, orderId));

        supplierOrderService.finishPreparing(orderId, storeId);
        assertEquals(DISPATCHED, getOrderStatus(jdbc, orderId));

        loginAsBuyer();
        buyerOrderService.markOrderAsArrived(orderId);
        assertEquals(COMPLETED, getOrderStatus(jdbc, orderId));

        List<String[]> history = getStatusHistory(jdbc, orderId);
        assertEquals(5, history.size(), "Expected exactly 5 recorded transitions");
        assertEquals("CART", history.get(0)[0]);
        assertEquals("DRAFT", history.get(0)[1]);
        assertEquals("DRAFT", history.get(1)[0]);
        assertEquals("PENDING", history.get(1)[1]);
        assertEquals("PENDING", history.get(2)[0]);
        assertEquals("PROCESSING", history.get(2)[1]);
        assertEquals("PROCESSING", history.get(3)[0]);
        assertEquals("DISPATCHED", history.get(3)[1]);
        assertEquals("DISPATCHED", history.get(4)[0]);
        assertEquals("COMPLETED", history.get(4)[1]);

        assertEquals(List.of("BUYER", "BUYER", "SUPPLIER", "SUPPLIER", "BUYER"),
                history.stream().map(row -> row[2]).toList());
    }

    @Test
    void fullWorkflow_stockDeductedExactlyOnce() {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING);
        int storeProductId = seedStoreProductAndOrderLine(orderId, 4);

        loginAsSupplier();
        supplierOrderService.acceptPendingOrder(orderId, storeId);

        assertEquals(6, (int) jdbc.queryForObject("SELECT qty FROM tb_store_product_detail WHERE id = ?", Integer.class, storeProductId));

        // A second acceptance attempt must not be possible (order is no longer PENDING) —
        // stock must not be deducted twice even if somehow re-invoked.
        assertThrows(NotFoundException.class, () -> supplierOrderService.acceptPendingOrder(orderId, storeId));
        assertEquals(6, (int) jdbc.queryForObject("SELECT qty FROM tb_store_product_detail WHERE id = ?", Integer.class, storeProductId));
    }

    @Test
    void supplierCanNoLongerMarkOrderComplete() {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, DISPATCHED);
        seedStoreProductAndOrderLine(orderId, 1);

        loginAsSupplier();
        assertThrows(ForbiddenException.class, () -> supplierOrderService.orderDelivered(orderId, storeId));
        assertEquals(DISPATCHED, getOrderStatus(jdbc, orderId), "Order must remain dispatched — supplier can no longer complete it");
    }

    // ── Phase 24: rejection ─────────────────────────────────────────────────

    @Test
    void rejection_pendingOrderDeclined_noStockDeductedAndCannotBeAcceptedLater() {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING);
        int storeProductId = seedStoreProductAndOrderLine(orderId, 5);

        loginAsSupplier();
        supplierOrderService.declinePendingOrder(orderId, storeId);

        assertEquals(REJECTED, getOrderStatus(jdbc, orderId));
        assertEquals(10, (int) jdbc.queryForObject("SELECT qty FROM tb_store_product_detail WHERE id = ?", Integer.class, storeProductId),
                "Rejected order must not deduct stock");
        assertEquals(1, countBuyerNotifications(jdbc, orderId, ORDER_DECLINED_NOTIFICATION_TYPE));

        List<String[]> history = getStatusHistory(jdbc, orderId);
        assertEquals(1, history.size());
        assertEquals("PENDING", history.get(0)[0]);
        assertEquals("REJECTED", history.get(0)[1]);

        // A rejected order can never later be accepted.
        assertThrows(NotFoundException.class, () -> supplierOrderService.acceptPendingOrder(orderId, storeId));
    }

    // ── Phase 25: cancellation ──────────────────────────────────────────────

    @Test
    void cancellation_draftCancelledByBuyer() {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, DRAFT);
        loginAsBuyer();

        buyerOrderService.cancelOrder(orderId);

        assertEquals(CANCELLED, getOrderStatus(jdbc, orderId));
    }

    @Test
    void cancellation_pendingOrderCancelledBeforeAcceptance() {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING);
        loginAsBuyer();

        buyerOrderService.cancelOrder(orderId);

        assertEquals(CANCELLED, getOrderStatus(jdbc, orderId));
    }

    @Test
    void cancellation_afterProcessingIsRejected() {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PROCESSING);
        loginAsBuyer();

        assertThrows(NotFoundException.class, () -> buyerOrderService.cancelOrder(orderId));
        assertEquals(PROCESSING, getOrderStatus(jdbc, orderId));
    }

    // ── Phase 26: concurrent transition (accept vs reject race) ─────────────

    @Test
    void concurrentAcceptAndReject_exactlyOneSucceeds() throws Exception {
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING);
        seedStoreProductAndOrderLine(orderId, 1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Callable<String> accept = () -> {
                loginAsSupplier();
                barrier.await(5, TimeUnit.SECONDS);
                return supplierOrderService.acceptPendingOrder(orderId, storeId);
            };
            Callable<String> reject = () -> {
                loginAsSupplier();
                barrier.await(5, TimeUnit.SECONDS);
                return supplierOrderService.declinePendingOrder(orderId, storeId);
            };

            Future<String> acceptFuture = pool.submit(accept);
            Future<String> rejectFuture = pool.submit(reject);

            int succeeded = 0, failed = 0;
            for (Future<String> future : List.of(acceptFuture, rejectFuture)) {
                try {
                    future.get(15, TimeUnit.SECONDS);
                    succeeded++;
                } catch (ExecutionException e) {
                    assertTrue(e.getCause() instanceof ConflictException || e.getCause() instanceof NotFoundException,
                            "Losing action must fail with a business exception, not a raw DB error: " + e.getCause());
                    failed++;
                }
            }

            assertEquals(1, succeeded, "Exactly one of accept/reject should succeed");
            assertEquals(1, failed, "Exactly one of accept/reject should fail");

            int finalStatus = getOrderStatus(jdbc, orderId);
            assertTrue(finalStatus == PROCESSING || finalStatus == REJECTED,
                    "Final status must be PROCESSING or REJECTED, was " + finalStatus);

            List<String[]> history = getStatusHistory(jdbc, orderId);
            assertEquals(1, history.size(), "Exactly one history record — no duplicate/partial transitions");
        } finally {
            pool.shutdownNow();
        }
    }

    // ── Phase 27: ownership ─────────────────────────────────────────────────

    @Test
    void ownership_supplierCannotAcceptAnotherStoresOrder() {
        int otherSupplier = insertSupplierAccount(jdbc, "other-supplier-" + System.nanoTime() + "@example.test");
        int otherStoreId = insertStore(jdbc, otherSupplier, "Other Store");
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING);
        seedStoreProductAndOrderLine(orderId, 1);

        loginAsSupplier();
        assertThrows(NotFoundException.class, () -> supplierOrderService.acceptPendingOrder(orderId, otherStoreId));
        assertEquals(PENDING, getOrderStatus(jdbc, orderId));
    }

    @Test
    void ownership_buyerCannotCancelAnotherBuyersOrder() {
        int otherBuyer = insertBuyerAccount(jdbc, "other-buyer-" + System.nanoTime() + "@example.test");
        int orderId = insertOrder(jdbc, otherBuyer, storeId, PENDING);
        loginAsBuyer(); // logged in as buyerAccountId, not otherBuyer

        assertThrows(NotFoundException.class, () -> buyerOrderService.cancelOrder(orderId));
        assertEquals(PENDING, getOrderStatus(jdbc, orderId));
    }

    @Test
    void ownership_buyerCannotViewAnotherBuyersOrderHistory() {
        int otherBuyer = insertBuyerAccount(jdbc, "other-buyer2-" + System.nanoTime() + "@example.test");
        int orderId = insertOrder(jdbc, otherBuyer, storeId, PENDING);
        loginAsBuyer();

        assertThrows(NotFoundException.class, () -> buyerOrderService.getOrderHistory(orderId));
    }

    @Test
    void ownership_supplierCannotViewUnrelatedOrderHistory() {
        int otherSupplier = insertSupplierAccount(jdbc, "other-supplier2-" + System.nanoTime() + "@example.test");
        int otherStoreId = insertStore(jdbc, otherSupplier, "Unrelated Store");
        int orderId = insertOrder(jdbc, buyerAccountId, storeId, PENDING);
        loginAsSupplier(); // this supplier does not own otherStoreId

        assertThrows(NotFoundException.class, () -> supplierOrderService.getOrderHistory(orderId, otherStoreId));
    }
}
