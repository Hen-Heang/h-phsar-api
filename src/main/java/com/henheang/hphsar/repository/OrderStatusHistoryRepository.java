package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.order.OrderStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderStatusHistoryRepository {

    int insertStatusHistory(@Param("orderId") Integer orderId,
                             @Param("previousStatus") String previousStatus,
                             @Param("newStatus") String newStatus,
                             @Param("changedByAccountId") Integer changedByAccountId,
                             @Param("changedByRole") String changedByRole,
                             @Param("reason") String reason);

    List<OrderStatusHistory> findByOrderId(Integer orderId);
}
