package com.henheang.hphsar.model.order;

/**
 * The order lifecycle, Step 3C. Every value has exactly one meaning and the
 * database is the source of truth for the id↔name mapping (tb_status) — this
 * enum only carries the name and the transition rules. Never persist
 * {@link #ordinal()}; always persist/compare by {@link #name()}.
 * <p>
 * CART: buyer's active, editable shopping cart (not yet a "real" order).
 * DRAFT: buyer saved the cart for later, not currently being edited.
 * PENDING: submitted to the supplier, awaiting accept/reject.
 * PROCESSING: supplier accepted, preparing the order.
 * DISPATCHED: supplier has sent the order out.
 * COMPLETED: buyer confirmed receipt — terminal.
 * REJECTED: supplier declined a pending order — terminal.
 * CANCELLED: buyer cancelled a draft or a not-yet-accepted pending order — terminal.
 * <p>
 * DELIVERED was evaluated as a separate checkpoint between DISPATCHED and
 * COMPLETED (a distinct "package physically arrived" event ahead of "buyer
 * confirms the transaction"). The codebase has no second actor/event in that
 * gap — dispatch is the supplier's last responsibility, and the buyer's
 * receipt confirmation is the only thing that follows — so a separate
 * DELIVERED state would carry no real workflow meaning. It was left out.
 */
public enum OrderStatus {
    CART,
    DRAFT,
    PENDING,
    PROCESSING,
    DISPATCHED,
    COMPLETED,
    REJECTED,
    CANCELLED;

    /**
     * The transition matrix. Every transition not listed here is invalid.
     * Deliberately does not depend on ordinal order — CART..CANCELLED above is
     * declaration order for readability only, not a linear pipeline (REJECTED/
     * CANCELLED are branches, not later stages).
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CART -> next == DRAFT || next == PENDING;
            case DRAFT -> next == PENDING || next == CANCELLED;
            case PENDING -> next == PROCESSING || next == REJECTED || next == CANCELLED;
            case PROCESSING -> next == DISPATCHED;
            case DISPATCHED -> next == COMPLETED;
            case COMPLETED, REJECTED, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }
}
