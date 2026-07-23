package com.henheang.hphsar.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * The generic order-status engine's repository — deliberately separate from
 * BuyerOrderRepository/SupplierOrderRepository, which stay focused on their
 * own actor-specific queries. Every method here works by status NAME
 * (tb_status.name), never by numeric id — tb_status stays the schema's source
 * of truth (Option A, Step 3C Phase 8), but no Java code should ever again
 * need to know what number a status is.
 */
@Mapper
public interface OrderStatusRepository {

    /**
     * Current status name for an order, or null if the order doesn't exist.
     */
    String findStatusNameByOrderId(Integer orderId);

    /**
     * Optimistic conditional transition: only applies if the order is still in
     * exactly {@code currentStatus} at the moment of to write. Returns the
     * affected-row count — 1 = transitioned, 0 = the order moved (or vanished)
     * since it was last read, so the caller must not assume success.
     */
    int updateStatusIfCurrent(@Param("orderId") Integer orderId,
                               @Param("currentStatus") String currentStatus,
                               @Param("newStatus") String newStatus);
}
