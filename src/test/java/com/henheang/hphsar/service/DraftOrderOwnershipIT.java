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
    void service_updateDraftById_ownerPassesTheOwnershipGateAndReachesTheExistingConflictCheck() {
        // HistoryServiceImplV1.updateDraftById also runs a pre-existing,
        // unrelated conflict check (checkForCartOrPending) which — as written
        // before this fix, and unchanged by it — matches the draft being
        // submitted against itself (status 8 is one of the statuses it looks
        // for), so a lone draft with no other active order always hits that
        // conflict once ownership passes. That business-logic quirk predates
        // this step and is out of scope to redesign here (see the final
        // report's "unrelated findings" section). What this test proves is
        // narrower and squarely in scope: Buyer A's OWN draft reaches that
        // later check at all — i.e. gets past the ownership gate — while a
        // stranger's request never does (see the two tests above, which get
        // NotFoundException instead). That difference in *which* exception
        // is thrown is the observable proof the ownership check is scoped
        // correctly.
        loginAs(buyerA);

        assertThrows(ConflictException.class, () -> historyService.updateDraftById(draftAId));
        assertEquals(DRAFT_STATUS_ID, getOrderStatus(jdbc, draftAId));
    }
}
