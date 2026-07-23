package com.henheang.hphsar.model.order;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 3C Phase 22 — pure unit coverage of the transition matrix. No Spring
 * context, no database: this is the one guarantee that holds regardless of
 * environment (see OrderLifecycleIT for the Testcontainers-backed coverage).
 */
class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({
            "CART, DRAFT",
            "CART, PENDING",
            "DRAFT, PENDING",
            "DRAFT, CANCELLED",
            "PENDING, PROCESSING",
            "PENDING, REJECTED",
            "PENDING, CANCELLED",
            "PROCESSING, DISPATCHED",
            "DISPATCHED, COMPLETED",
    })
    void allowedTransitions_succeed(OrderStatus from, OrderStatus to) {
        assertTrue(from.canTransitionTo(to), from + " -> " + to + " should be allowed");
    }

    @ParameterizedTest(name = "{0} -> {1} is rejected")
    @CsvSource({
            "CART, COMPLETED",
            "CART, PROCESSING",
            "CART, DISPATCHED",
            "CART, REJECTED",
            "CART, CANCELLED",
            "PENDING, COMPLETED",
            "PENDING, DISPATCHED",
            "REJECTED, PROCESSING",
            "CANCELLED, PENDING",
            "COMPLETED, PROCESSING",
            "DISPATCHED, REJECTED",
            "DISPATCHED, CANCELLED",
            "DISPATCHED, PENDING",
            "PROCESSING, COMPLETED",
            "PROCESSING, PENDING",
            "PROCESSING, REJECTED",
            "DRAFT, DISPATCHED",
            "DRAFT, COMPLETED",
    })
    void forbiddenTransitions_areRejected(OrderStatus from, OrderStatus to) {
        assertFalse(from.canTransitionTo(to), from + " -> " + to + " should be rejected");
    }

    @ParameterizedTest(name = "{0} is terminal")
    @CsvSource({"COMPLETED", "REJECTED", "CANCELLED"})
    void terminalStatuses_haveNoOutgoingTransitions(OrderStatus status) {
        for (OrderStatus next : OrderStatus.values()) {
            assertFalse(status.canTransitionTo(next), status + " -> " + next + " should be rejected (terminal)");
        }
        assertTrue(status.isTerminal());
    }

    @ParameterizedTest(name = "{0} is not terminal")
    @CsvSource({"CART", "DRAFT", "PENDING", "PROCESSING", "DISPATCHED"})
    void nonTerminalStatuses_reportNotTerminal(OrderStatus status) {
        assertFalse(status.isTerminal());
    }
}
