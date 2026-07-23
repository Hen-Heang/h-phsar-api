package com.henheang.hphsar.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One append-only row per order status change. previousStatus is null for the
 * very first row (CART creation has no "previous" status). changedByAccountId
 * is the supplier_account_id or buyer_account_id depending on changedByRole —
 * suppliers and buyers live in separate account tables, so this is a plain
 * column (no FK) rather than one FK that could point at either table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {
    private Integer id;
    private Integer orderId;
    private String previousStatus;
    private String newStatus;
    private Integer changedByAccountId;
    private String changedByRole;
    private String reason;
    private String changedAt;
}
