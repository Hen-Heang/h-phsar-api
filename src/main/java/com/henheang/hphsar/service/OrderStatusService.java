package com.henheang.hphsar.service;

import com.henheang.hphsar.model.appUser.Role;
import com.henheang.hphsar.model.order.OrderStatus;
import com.henheang.hphsar.model.order.OrderStatusHistory;

import java.util.List;

/**
 * The single place that decides whether an order status change is allowed,
 * performs it, and records it. No other class should write tb_order.status_id.
 */
public interface OrderStatusService {

    OrderStatus getCurrentStatus(Integer orderId);

    /**
     * Validates the transition (both the caller's expected current status and
     * the transition-matrix rules), performs it with an atomic conditional
     * UPDATE, and appends one history row — all under one transaction.
     *
     * @param expectedCurrent the status the caller has already verified the order is in
     *                        (e.g. via its own ownership/precondition checks); a mismatch
     *                        here means the order moved since the caller last looked
     * @param actorAccountId  the supplier_account_id or buyer_account_id performing the change
     * @param actorRole       which of those two tables actorAccountId belongs to
     * @param reason          optional human-readable note stored on the history row
     * @return the new status
     */
    OrderStatus transitionOrder(Integer orderId, OrderStatus expectedCurrent, OrderStatus next,
                                 Integer actorAccountId, Role actorRole, String reason);

    List<OrderStatusHistory> getHistory(Integer orderId);
}
