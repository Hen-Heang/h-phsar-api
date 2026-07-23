package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.Role;
import com.henheang.hphsar.model.order.OrderStatus;
import com.henheang.hphsar.model.order.OrderStatusHistory;
import com.henheang.hphsar.repository.OrderStatusHistoryRepository;
import com.henheang.hphsar.repository.OrderStatusRepository;
import com.henheang.hphsar.service.OrderStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderStatusServiceImpl(OrderStatusRepository orderStatusRepository,
                                   OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderStatusRepository = orderStatusRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatus getCurrentStatus(Integer orderId) {
        String name = orderStatusRepository.findStatusNameByOrderId(orderId);
        if (name == null) {
            throw new NotFoundException("Order not found.");
        }
        return parseStatus(name);
    }

    @Override
    @Transactional
    public OrderStatus transitionOrder(Integer orderId, OrderStatus expectedCurrent, OrderStatus next,
                                        Integer actorAccountId, Role actorRole, String reason) {
        OrderStatus current = getCurrentStatus(orderId);
        // Informational check first — gives a clear, specific error for the
        // common "someone already changed this" case. NOT the concurrency
        // guard: updateStatusIfCurrent below (one atomic guarded UPDATE) is
        // the authoritative check, same pattern as Step 3B's stock deduction.
        if (current != expectedCurrent) {
            throw new ConflictException("Order status has changed. Expected " + expectedCurrent + " but was " + current + ".");
        }
        if (!current.canTransitionTo(next)) {
            throw new ConflictException("Invalid order status transition: " + current + " -> " + next + ".");
        }

        int updated = orderStatusRepository.updateStatusIfCurrent(orderId, current.name(), next.name());
        if (updated != 1) {
            throw new ConflictException("Order status was updated by another request.");
        }

        orderStatusHistoryRepository.insertStatusHistory(orderId, current.name(), next.name(), actorAccountId, actorRole.name(), reason);
        return next;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getHistory(Integer orderId) {
        return orderStatusHistoryRepository.findByOrderId(orderId);
    }

    private OrderStatus parseStatus(String name) {
        try {
            return OrderStatus.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new InternalServerErrorException("Unknown order status in database: " + name);
        }
    }
}
