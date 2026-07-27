package com.henheang.hphsar.service.implement;
import com.henheang.hphsar.common.ExceptionMessages;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.ForbiddenException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.appUser.Role;
import com.henheang.hphsar.model.invoice.Invoice;
import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.order.OrderDetail;
import com.henheang.hphsar.model.order.OrderStatus;
import com.henheang.hphsar.model.order.OrderStatusHistory;
import com.henheang.hphsar.model.order.OrderStockLine;
import com.henheang.hphsar.repository.NotificationRepository;
import com.henheang.hphsar.repository.SupplierOrderRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.OrderStatusService;
import com.henheang.hphsar.service.SupplierOrderService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import com.henheang.hphsar.utils.PaginationUtils;
import com.henheang.hphsar.utils.SortDirectionUtils;
import com.henheang.hphsar.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SupplierOrderServiceImpl implements SupplierOrderService {
    private final SupplierOrderRepository supplierOrderRepository;
    private final StoreRepository storeRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderStatusService orderStatusService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public Integer getCurrentStoreId() {
        return storeRepository.getStoreIdByUserId(currentUserProvider.getCurrentUserId());
    }

    @Override
    public List<Order> getAllOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getAllOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException(ExceptionMessages.THERE_IS_NO_ORDER_CURRENTLY);
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (orders.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        for (Order order : orders) {
            order.setDate(DateTimeUtil.format(DateTimeUtil.parse(order.getDate())));
        }
        return orders;
    }


    @Override
    public List<Order> getNewOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getPendingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalNewOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException(ExceptionMessages.THERE_IS_NO_ORDER_CURRENTLY);
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        for (Order order : orders) {
            order.setDate(DateTimeUtil.format(DateTimeUtil.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getPreparingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getPreparingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalPreparingOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException(ExceptionMessages.THERE_IS_NO_ORDER_CURRENTLY);
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        for (Order order : orders) {
            order.setDate(DateTimeUtil.format(DateTimeUtil.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getDispatchingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getDispatchingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalDispatchingOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException(ExceptionMessages.THERE_IS_NO_ORDER_CURRENTLY);
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        for (Order order : orders) {
            order.setDate(DateTimeUtil.format(DateTimeUtil.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getConfirmingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getConfirmingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalConfirmingOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException(ExceptionMessages.THERE_IS_NO_ORDER_CURRENTLY);
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        for (Order order : orders) {
            order.setDate(DateTimeUtil.format(DateTimeUtil.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getCompleteOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getCompleteOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalCompleteOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no complete order currently.");
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        for (Order order : orders) {
            order.setDate(DateTimeUtil.format(DateTimeUtil.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public Integer getTotalCompleteOrder(Integer storeId) {
        return supplierOrderRepository.getTotalCompleteOrder(storeId);
    }

    @Override
    public Integer getTotalConfirmingOrder(Integer storeId) {
        return supplierOrderRepository.getTotalConfirmingOrder(storeId);
    }

    @Override
    public Integer findTotalPage(Integer totalOrder, Integer pageSize) {
        return PaginationUtils.totalPages(totalOrder, pageSize);
    }

    @Override
    public Integer getTotalOrder(Integer storeId) {
        return supplierOrderRepository.getCurrentStoreTotalOrder(storeId);
    }

    @Override
    public Integer getTotalNewOrder(Integer storeId) {
        return supplierOrderRepository.getCurrentStoreTotalNewOrder(storeId);
    }

    @Override
    public Integer getTotalPreparingOrder(Integer storeId) {
        return supplierOrderRepository.getCurrentStoreTotalPreparingOrder(storeId);
    }

    @Override
    public Integer getTotalDispatchingOrder(Integer storeId) {
        return supplierOrderRepository.getTotalDispatchingOrder(storeId);
    }

    @Override
    @Transactional
    public String acceptPendingOrder(Integer orderId, Integer storeId) {
        // check if the order is pending or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        if (!checkForPendingOrder(orderId)) {
            throw new NotFoundException(ExceptionMessages.THIS_ORDER_IS_NOT_PENDING);
        }
        // Informational pre-check only — gives a fast, friendly rejection for the
        // common case. It is NOT the authoritative stock decision: it reads then
        // compares, which is racy under concurrency. deductStockForOrder() below
        // (one atomic guarded UPDATE per line item) is what actually enforces
        // stock safety, and its failure is what must trigger rollback.
        if (!checkAvailableProduct(orderId)) {
            throw new ConflictException(ExceptionMessages.NOT_ENOUGH_PRODUCT_IN_STOCK);
        }
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        // accept order — centralized transition + history (Step 3C)
        orderStatusService.transitionOrder(orderId, OrderStatus.PENDING, OrderStatus.PROCESSING, currentUserId, Role.SUPPLIER, "Supplier accepted order");
        // get order detail
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        // Authoritative, concurrency-safe stock deduction. If any line item can't
        // be deducted, this throws — the @Transactional boundary on this method
        // rolls back the order-acceptance status change above along with it, so
        // the order remains pending and no notification is committed.
        deductStockForOrder(orderId);
        // check stock
        List<Integer> productIds = storeRepository.checkStock(orderId);
        StringBuilder id = new StringBuilder();
        for (Integer x : productIds){
            id.append(" ").append(x);
        }
        if (!productIds.isEmpty()){
            Integer notificationCheck = notificationRepository.createSupplierNotification(currentUserId, 2, orderDetail.getOrder().getId(), "Product out of stock.", "Out of stock.", "Product #"+id+" are out of stock.", false);
            if (notificationCheck == null){
                throw new InternalServerErrorException("Fail to create notification for out of stock");
            }
        }
        // create notification
        Integer check;
        try {
            check = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 4, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() +" was accepted.", "Order accepted.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " was accepted and is being prepared. Please keep in contact and monitor the order progress.", false);
        } catch (Exception e) {
            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
        }
        if (check == null) {
            throw new InternalServerErrorException(ExceptionMessages.FAIL_TO_CREATE_NOTIFICATION);
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Successfully accept order.";
    }

    /**
     * Deducts stock for every line item on the order, one atomic guarded UPDATE
     * at a time. Each call re-checks "qty >= quantity" in the database at write
     * time — the authoritative concurrency-safe decision. If any line item's
     * affected-row count isn't exactly 1 (insufficient stock, or the row is
     * gone), this throws immediately; multiple order items therefore succeed or
     * fail as one unit under the caller's transaction.
     */
    private void deductStockForOrder(Integer orderId) {
        List<OrderStockLine> lines = supplierOrderRepository.getOrderStockLines(orderId);
        for (OrderStockLine line : lines) {
            // Defend against invalid quantities already sitting in the database
            // (legacy/corrupt rows) — not a substitute for request-level validation,
            // just a last line of defense before we ever touch the database write.
            if (line.getQty() == null || line.getQty() <= 0) {
                throw new ConflictException("Invalid quantity recorded for product #" + line.getStoreProductId() + ".");
            }
            int updated = supplierOrderRepository.deductStockIfAvailable(line.getStoreProductId(), line.getQty());
            if (updated != 1) {
                throw new ConflictException("Insufficient stock for product #" + line.getStoreProductId() + ".");
            }
        }
    }

    private boolean checkAvailableProduct(Integer orderId) {
        // count how many order detail product qty is <= store product detail qty. eligible mean meet requirement
        Integer eligibleCount = supplierOrderRepository.productEligibleCount(orderId);
        // Select all products from product detail by order id
        Integer productDetailCount = supplierOrderRepository.getProductDetailCount(orderId);
        // if eligibleCount != productDetailCount return false
        return Objects.equals(eligibleCount, productDetailCount);
    }

    @Override
    @Transactional
    public String declinePendingOrder(Integer orderId, Integer storeId) {
        // check if the order is pending or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        if (!checkForPendingOrder(orderId)) {
            throw new NotFoundException(ExceptionMessages.THIS_ORDER_IS_NOT_PENDING);
        }
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        // decline order — centralized transition + history (Step 3C)
        orderStatusService.transitionOrder(orderId, OrderStatus.PENDING, OrderStatus.REJECTED, currentUserId, Role.SUPPLIER, "Supplier declined order");
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        Integer check;
        try {
            check = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 5, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" was declined.", "Order declined.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " was declined. Please try to order from other store or contact the store.", false);
        } catch (Exception e) {
            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
        }
        if (check == null) {
            throw new InternalServerErrorException(ExceptionMessages.FAIL_TO_CREATE_NOTIFICATION);
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Successfully decline order.";
    }

    @Override
    @Transactional
    public String finishPreparing(Integer orderId, Integer storeId) {
        // check if the order is preparing or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        if (!checkForPreparingOrder(orderId)) {
            throw new NotFoundException("This order is not preparing");
        }
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        // dispatch order — centralized transition + history (Step 3C)
        orderStatusService.transitionOrder(orderId, OrderStatus.PROCESSING, OrderStatus.DISPATCHED, currentUserId, Role.SUPPLIER, "Supplier dispatched order");
        // create notification for delivering
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        Integer delivering = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 7, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" is being delivered.", "Order is being delivered.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " is being delivered. Your order will arrive shortly.", false);
        if (delivering == null){
            throw new InternalServerErrorException(ExceptionMessages.FAIL_TO_CREATE_NOTIFICATION);
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Finish preparing.";
    }

    /**
     * Retired (Step 3C): this used to perform the exact same DISPATCHED ->
     * COMPLETED transition as the buyer's markOrderAsArrived/confirmTransaction
     * — two actors independently able to complete the same order, one of the
     * audit's flagged duplicate actions. The redesigned lifecycle gives the
     * buyer sole ownership of "confirm receipt"; supplier responsibility ends
     * at dispatch (see finishPreparing). The endpoint stays mapped (old
     * clients get a clear, typed rejection instead of a 404) but no longer
     * performs any transition.
     */
    @Override
    public String orderDelivered(Integer orderId, Integer storeId) {
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        throw new ForbiddenException("Only the buyer may confirm receipt of a dispatched order. Supplier responsibility ends at dispatch.");
    }

    @Override
    public OrderDetail getOrderDetailsByOrderId(Integer id, Integer storeId) throws ParseException {
        // check if the order is preparing or exist
        if (!checkOrderExist(id, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(id);
        if (orderDetail == null) {
            throw new InternalServerErrorException(ExceptionMessages.FAIL_TO_FETCH_ORDER_DETAILS);
        }
        orderDetail.getOrder().setDate(DateTimeUtil.format(DateTimeUtil.parse(orderDetail.getOrder().getDate())));
        return orderDetail;
    }

    @Override
    public Invoice getInvoiceByOrderId(Integer orderId, Integer storeId) throws ParseException {
        // check if the order is preparing or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        if (!checkForCompleteOrder(orderId)) {
            throw new BadRequestException("Invoice not yet generated. Please complete order to view invoice.");
        }
        Invoice invoice = supplierOrderRepository.getInvoiceByOrderId(orderId);
        if (invoice == null) {
            throw new InternalServerErrorException(ExceptionMessages.FAIL_TO_FETCH_ORDER_INVOICE);
        }
        invoice.getOrder().setDate(DateTimeUtil.format(DateTimeUtil.parse(invoice.getOrder().getDate())));
        return invoice;
    }

    @Override
    public List<OrderStatusHistory> getOrderHistory(Integer orderId, Integer storeId) {
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException(ExceptionMessages.ORDER_NOT_FOUND);
        }
        return orderStatusService.getHistory(orderId);
    }

    private boolean checkForCompleteOrder(Integer orderId) {
        return supplierOrderRepository.checkForCompleteOrder(orderId);
    }

    private boolean checkForDispatchOrder(Integer orderId) {
        return supplierOrderRepository.checkForDispatchOrder(orderId);
    }

    private boolean checkForPreparingOrder(Integer orderId) {
        return supplierOrderRepository.checkForPreparingOrder(orderId);
    }

    Boolean checkOrderExist(Integer orderId, Integer storeId) {
        return supplierOrderRepository.checkOrderExist(orderId, storeId);
    }

    Boolean checkForPendingOrder(Integer orderId) {
        return supplierOrderRepository.checkForPendingOrder(orderId);
    }
}
