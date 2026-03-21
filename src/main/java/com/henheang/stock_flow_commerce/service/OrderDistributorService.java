package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.invoice.Invoice;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.order.OrderDetail;

import java.text.ParseException;
import java.util.List;

public interface OrderDistributorService {
    Integer findTotalPage(Integer totalOrder, Integer pageSize);
    List<Order> getAllOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException;
    Integer getTotalOrder(Integer storeId);
    Integer getTotalNewOrder(Integer storeId);
    Integer getTotalPreparingOrder(Integer storeId);
    Integer getTotalDispatchingOrder(Integer storeId);
    List<Order> getNewOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException;
    List<Order> getPreparingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException;
    List<Order> getDispatchingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException;
    List<Order> getConfirmingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException;
    Integer getTotalConfirmingOrder(Integer storeId);
    List<Order> getCompleteOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException;
    Integer getTotalCompleteOrder(Integer storeId);
    String acceptPendingOrder(Integer orderId, Integer storeId);
    String declinePendingOrder(Integer orderId, Integer storeId);
    String finishPreparing(Integer orderId, Integer storeId);
    String orderDelivered(Integer orderId, Integer storeId);
    OrderDetail getOrderDetailsByOrderId(Integer id, Integer storeId) throws ParseException;
    Invoice getInvoiceByOrderId(Integer orderId, Integer storeId) throws ParseException;
}
