package com.henheang.hphsar.service;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.repository.HistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.henheang.hphsar.support.TestDataFactory.getOrderStatus;
import static com.henheang.hphsar.support.TestDataFactory.insertBuyerAccount;
import static com.henheang.hphsar.support.TestDataFactory.insertOrder;
import static com.henheang.hphsar.support.TestDataFactory.insertStore;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static com.henheang.hphsar.support.TestDataFactory.orderExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the draft-order IDOR fix: a buyer may only
 * read/update/submit/delete a draft they themselves own. Covers both the
 * MyBatis/repository layer (ownership enforced in the SQL WHERE clause) and
 * the service-authorization layer (HistoryServiceImplV1), per the audit
 * finding that the service resolved the current buyer id but never passed
 * it into the draft queries.
 */
class DraftOrderOwnershipIT extends AbstractIntegrationTest {

    private static final int DRAFT_STATUS_ID = 8;
    private static final int PENDING_STATUS_ID = 1;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private JdbcTemplate jdbc;

    private int buyerA;
    private int buyerB;
    private int draftAId;
    private int draftBId;

    @BeforeEach
    void seedTwoBuyersEachWithTheirOwnDraft() {
        int supplier = insertSupplierAccount(jdbc, "supplier@example.test");
        int store = insertStore(jdbc, supplier, "Shared Store");

        buyerA = insertBuyerAccount(jdbc, "buyerA@example.test");
        buyerB = insertBuyerAccount(jdbc, "buyerB@example.test");

        draftAId = insertOrder(jdbc, buyerA, store, DRAFT_STATUS_ID);
        draftBId = insertOrder(jdbc, buyerB, store, DRAFT_STATUS_ID);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(int buyerAccountId) {
        AppUser principal = new AppUser(buyerAccountId, "buyer" + buyerAccountId + "@example.test",
                "test-hash", "BUYER", 2, true, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    // ── Repository / MyBatis level ──────────────────────────────────────────

    @Test
    void repository_existsDraftByIdAndBuyerId_trueOnlyForTheOwningBuyer() {
        assertTrue(historyRepository.existsDraftByIdAndBuyerId(draftAId, buyerA));
        assertFalse(historyRepository.existsDraftByIdAndBuyerId(draftAId, buyerB));
        assertFalse(historyRepository.existsDraftByIdAndBuyerId(999_999, buyerA));
    }

    @Test
    void repository_existsDraftByIdAndBuyerId_falseWhenStatusIsNotDraft() {
        int pendingOrderId = insertOrder(jdbc, buyerA, insertStore(jdbc, insertSupplierAccount(jdbc, "s2@example.test"), "Other Store"), PENDING_STATUS_ID);

        assertFalse(historyRepository.existsDraftByIdAndBuyerId(pendingOrderId, buyerA));
    }

    @Test
    void repository_deleteDraftByIdAndBuyerId_deletesOnlyWhenOwnerMatches() {
        int affectedForStranger = historyRepository.deleteDraftByIdAndBuyerId(draftBId, buyerA);
        assertEquals(0, affectedForStranger);
        assertTrue(orderExists(jdbc, draftBId), "Buyer B's draft must survive Buyer A's delete attempt.");

        int affectedForOwner = historyRepository.deleteDraftByIdAndBuyerId(draftAId, buyerA);
        assertEquals(1, affectedForOwner);
        assertFalse(orderExists(jdbc, draftAId), "The owner's own delete must actually remove the row.");
    }

    @Test
    void repository_submitDraftByIdAndBuyerId_transitionsStatusOnlyWhenOwnerMatches() {
        int affectedForStranger = historyRepository.submitDraftByIdAndBuyerId(draftBId, buyerA);
        assertEquals(0, affectedForStranger);
        assertEquals(DRAFT_STATUS_ID, getOrderStatus(jdbc, draftBId), "Buyer B's draft status must be untouched.");

        int affectedForOwner = historyRepository.submitDraftByIdAndBuyerId(draftAId, buyerA);
        assertEquals(1, affectedForOwner);
        assertEquals(PENDING_STATUS_ID, getOrderStatus(jdbc, draftAId));
    }

    // ── Service authorization level ─────────────────────────────────────────

    @Test
    void service_deleteDraftById_ownerSucceeds() {
        loginAs(buyerA);

        historyService.deleteDraftById(draftAId);

        assertFalse(orderExists(jdbc, draftAId));
    }

    @Test
    void service_deleteDraftById_crossUserAccessIsRejectedAndDraftSurvives() {
        loginAs(buyerA);

        assertThrows(NotFoundException.class, () -> historyService.deleteDraftById(draftBId));
        assertTrue(orderExists(jdbc, draftBId), "A rejected delete must not touch another buyer's draft.");
    }

    @Test
    void service_deleteDraftById_unknownDraftGetsTheSameNotFoundAsCrossUserAccess() {
        loginAs(buyerA);

        NotFoundException crossUser = assertThrows(NotFoundException.class,
                () -> historyService.deleteDraftById(draftBId));
        NotFoundException unknown = assertThrows(NotFoundException.class,
                () -> historyService.deleteDraftById(999_999));

        // Same exception type and message for both cases: the client cannot
        // tell "belongs to someone else" apart from "doesn't exist".
        assertEquals(crossUser.getClass(), unknown.getClass());
        assertEquals(crossUser.getMessage(), unknown.getMessage());
    }

    @Test
    void service_deleteDraftById_wrongStatusOrderIsRejectedAndLeftUnchanged() {
        int pendingOrderId = insertOrder(jdbc, buyerA, insertStore(jdbc, insertSupplierAccount(jdbc, "s3@example.test"), "Third Store"), PENDING_STATUS_ID);
        loginAs(buyerA);

        assertThrows(NotFoundException.class, () -> historyService.deleteDraftById(pendingOrderId));
        assertEquals(PENDING_STATUS_ID, getOrderStatus(jdbc, pendingOrderId),
                "A buyer's own PENDING order must not be deletable through the draft-only endpoint.");
    }

    @Test
    void service_updateDraftById_crossUserAccessIsRejectedAndDraftBUnchanged() {
        loginAs(buyerA);

        assertThrows(NotFoundException.class, () -> historyService.updateDraftById(draftBId));
        assertEquals(DRAFT_STATUS_ID, getOrderStatus(jdbc, draftBId),
                "A rejected submit must not touch another buyer's draft status.");
    }

    @Test
    void service_updateDraftById_unknownDraftIsRejectedTheSameWay() {
        loginAs(buyerA);

        assertThrows(NotFoundException.class, () -> historyService.updateDraftById(999_999));
    }

    @Test
    void service_updateDraftById_ownerPassesTheOwnershipGateAndSubmitSucceeds() throws Exception {
        // Step 3C fix: HistoryServiceImplV1.updateDraftById's conflict check
        // (checkForCartOrPending) used to match the draft being submitted
        // against ITSELF (status 8 is one of the statuses it looks for), so a
        // lone draft with no other active order always hit a spurious conflict
        // once ownership passed — draft submission was permanently unreachable.
        // The check now excludes the draft's own id, so Buyer A's own draft,
        // with no other competing cart/draft/pending order, submits cleanly:
        // DRAFT (8) -> PENDING (1). Cross-user access still gets NotFoundException
        // (see the two tests above), proving the ownership gate is unaffected.
        loginAs(buyerA);

        historyService.updateDraftById(draftAId);

        assertEquals(PENDING_STATUS_ID, getOrderStatus(jdbc, draftAId));
    }

    @Test
    void service_updateDraftById_stillConflictsWhenAnotherActiveOrderExistsInTheSameStore() {
        // The conflict check still fires for its real purpose: a genuinely
        // different competing cart/draft in the same store (not the draft itself).
        loginAs(buyerA);
        int store = jdbc.queryForObject("SELECT store_id FROM tb_order WHERE id = ?", Integer.class, draftAId);
        insertOrder(jdbc, buyerA, store, DRAFT_STATUS_ID);

        assertThrows(ConflictException.class, () -> historyService.updateDraftById(draftAId));
        assertEquals(DRAFT_STATUS_ID, getOrderStatus(jdbc, draftAId));
    }
}
