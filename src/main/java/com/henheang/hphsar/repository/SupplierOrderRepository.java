package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.invoice.Invoice;
import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.order.OrderDetail;
import com.henheang.hphsar.model.order.OrderStockLine;
import com.henheang.hphsar.model.product.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplierOrderRepository {

    List<Order> getAllOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    List<Order> getPendingOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    List<Order> getPreparingOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    List<Order> getDispatchingOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    List<Order> getConfirmingOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    List<Order> getCompleteOrders(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    Integer getCurrentStoreTotalPreparingOrder(Integer storeId);

    Double getTotalOrderAmount(Integer orderId);

    String getStatusByStatusId(Integer statusId);

    Integer getCurrentStoreTotalOrder(Integer storeId);

    Integer getCurrentStoreTotalNewOrder(Integer storeId);

    Integer getTotalDispatchingOrder(Integer storeId);

    Integer getTotalConfirmingOrder(Integer storeId);

    Integer getTotalCompleteOrder(Integer storeId);

    Boolean checkOrderExist(Integer orderId, Integer storeId);

    Boolean checkForPendingOrder(Integer orderId);

    Boolean checkForPreparingOrder(Integer orderId);

    Boolean checkForDispatchOrder(Integer orderId);

    OrderDetail getOrderDetailsByOrderId(Integer id);

    ProductOrder getProductOrderForOrderDetail(Integer orderId);

    List<ProductOrder> getProductOrderByOrderId(Integer orderId);

    Order getOrderByOrderId(Integer orderId);

    Boolean checkForCompleteOrder(Integer orderId);

    Invoice getInvoiceByOrderId(Integer orderId);

    Integer productEligibleCount(Integer orderId);

    Integer getProductDetailCount(Integer orderId);

    /**
     * The line items (store-specific inventory row + requested qty) that must be
     * deducted when this order is accepted.
     */
    List<OrderStockLine> getOrderStockLines(Integer orderId);

    /**
     * Authoritative, concurrency-safe stock deduction: one atomic guarded UPDATE.
     * The WHERE clause re-checks {@code qty >= quantity} at the moment of the write,
     * so the database — not a prior Java-side read — decides whether the deduction
     * is allowed. Returns the affected-row count: 1 = deducted, 0 = insufficient
     * stock (or the row no longer exists).
     */
    int deductStockIfAvailable(@Param("storeProductId") Integer storeProductId, @Param("quantity") Integer quantity);

    Integer getSupplierIdByOrderId(Integer cartId);
}