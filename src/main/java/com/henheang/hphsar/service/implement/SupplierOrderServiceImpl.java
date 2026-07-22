package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.invoice.Invoice;
import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.order.OrderDetail;
import com.henheang.hphsar.repository.NotificationRepository;
import com.henheang.hphsar.repository.SupplierOrderRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.SupplierOrderService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

@Service
public class SupplierOrderServiceImpl implements SupplierOrderService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SupplierOrderRepository supplierOrderRepository;
    private final StoreRepository storeRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SupplierOrderServiceImpl(SupplierOrderRepository supplierOrderRepository, StoreRepository storeRepository, NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.supplierOrderRepository = supplierOrderRepository;
        this.storeRepository = storeRepository;
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public List<Order> getAllOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getAllOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no order currently.");
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (orders.isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        for (Order order : orders) {
            order.setDate(formatter.format(formatter.parse(order.getDate())));
        }
        return orders;
    }


    @Override
    public List<Order> getNewOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getPendingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalNewOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no order currently.");
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        for (Order order : orders) {
            order.setDate(formatter.format(formatter.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getPreparingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getPreparingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalPreparingOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no order currently.");
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        for (Order order : orders) {
            order.setDate(formatter.format(formatter.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getDispatchingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getDispatchingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalDispatchingOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no order currently.");
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        for (Order order : orders) {
            order.setDate(formatter.format(formatter.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getConfirmingOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch order
        List<Order> orders = supplierOrderRepository.getConfirmingOrders(sort, pageNumber, pageSize, storeId);
        // find total order
        Integer totalOrder = getTotalConfirmingOrder(storeId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no order currently.");
        }
        // find total page
        Integer totalPage = findTotalPage(totalOrder, pageSize);
        // check out of range
        if (totalOrder < pageSize * pageNumber && orders.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        System.out.println(orders);
        if (orders.isEmpty()) {
            throw new NotFoundException("Order not found");
        }
        for (Order order : orders) {
            order.setDate(formatter.format(formatter.parse(order.getDate())));
        }
        return orders;
    }

    @Override
    public List<Order> getCompleteOrderCurrentUserSortByCreatedDate(String sort, Integer pageNumber, Integer pageSize, Integer storeId) throws ParseException {
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
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
            throw new NotFoundException("Order not found");
        }
        for (Order order : orders) {
            order.setDate(formatter.format(formatter.parse(order.getDate())));
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
        int totalPage;
        if (totalOrder % pageSize == 0) {
            totalPage = totalOrder / pageSize;
        } else {
            totalPage = (totalOrder / pageSize) + 1;
        }
        return totalPage;
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
    public String acceptPendingOrder(Integer orderId, Integer storeId) {
        // check if the order is pending or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        if (!checkForPendingOrder(orderId)) {
            throw new NotFoundException("This order is not pending");
        }
        // check if stock is available
        if (!checkAvailableProduct(orderId)) {
            throw new ConflictException("Not enough product in stock.");
        }
        // accept order
        String confirm = supplierOrderRepository.acceptPendingOrder(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to accept order.");
        }
        // get order detail
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        // deduct stock
        deductStock(orderId, storeId);
        // check stock
        List<Integer> productIds = storeRepository.checkStock(orderId);
        StringBuilder id = new StringBuilder();
        for (Integer x : productIds){
            id.append(" ").append(x);
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (!productIds.isEmpty()){
            Integer notificationCheck = notificationRepository.createSupplierNotification(currentUserId, 2, orderDetail.getOrder().getId(), "Product out of stock.", "Out of stock.", "Product #"+id+" are out of stock.", false);
            if (notificationCheck == null){
                notificationRepository.deleteNotification(notificationCheck);
                throw new InternalServerErrorException("Fail to create notification for out of stock");
            }
        }
        // create notification
        Integer check = 0;
        try {
            check = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 4, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() +" was accepted.", "Order accepted.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " was accepted and is being prepared. Please keep in contact and monitor the order progress.", false);
        } catch (Exception e) {
            notificationRepository.deleteNotification(check);
            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Successfully accept order.";
    }

    private void deductStock(Integer orderId, Integer storeId) {
        List<Integer> productIds = supplierOrderRepository.getAllProductIdFromProductDetails(orderId);
        for (Integer product : productIds) {
            supplierOrderRepository.deductStock(orderId, product, storeId);
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
    public String declinePendingOrder(Integer orderId, Integer storeId) {
        // check if the order is pending or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        if (!checkForPendingOrder(orderId)) {
            throw new NotFoundException("This order is not pending");
        }
        // decline order
        String confirm = supplierOrderRepository.declinePendingOrder(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to decline order.");
        }
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        Integer check = 0;
        try {
            check = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 5, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" was declined.", "Order declined.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " was declined. Please try to order from other store or contact the store.", false);
        } catch (Exception e) {
            notificationRepository.deleteNotification(check);
            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Successfully decline order.";
    }

    @Override
    public String finishPreparing(Integer orderId, Integer storeId) {
        // check if the order is preparing or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        if (!checkForPreparingOrder(orderId)) {
            throw new NotFoundException("This order is not preparing");
        }
        // decline order
        String confirm = supplierOrderRepository.finishPreparing(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to update order.");
        }
        // create notification for delivering
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        Integer delivering = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 7, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" is being delivered.", "Order is being delivered.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " is being delivered. Your order will arrive shortly.", false);
        if (delivering == null){
            throw new InternalServerErrorException("Fail to create notification.");
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Finish preparing.";
    }

    @Override
    public String orderDelivered(Integer orderId, Integer storeId) {
        // check if the order is preparing or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        if (!checkForDispatchOrder(orderId)) {
            throw new NotFoundException("This order is not pending");
        }
        // order arrived
        String confirm = supplierOrderRepository.orderDelivered(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to update order.");
        }
        // create completion notification — order is done, no buyer confirmation needed
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(orderId);
        Integer delivering = notificationRepository.createBuyerNotification(orderDetail.getOrder().getBuyerId(), 9, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" is complete.", "Order complete.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " has been delivered and is now complete.", false);
        if (delivering == null){
            throw new InternalServerErrorException("Fail to create notification.");
        }
        messagingTemplate.convertAndSend("/topic/notifications/" + orderDetail.getOrder().getBuyerId(), "NEW_NOTIFICATION");
        return "Finish Dispatching. Order is delivered.";
    }

    @Override
    public OrderDetail getOrderDetailsByOrderId(Integer id, Integer storeId) throws ParseException {
        // check if the order is preparing or exist
        if (!checkOrderExist(id, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(id);
        if (orderDetail == null) {
            throw new InternalServerErrorException("Fail to fetch order details.");
        }
        orderDetail.getOrder().setDate(formatter.format(formatter.parse(orderDetail.getOrder().getDate())));
        return orderDetail;
    }

    @Override
    public Invoice getInvoiceByOrderId(Integer orderId, Integer storeId) throws ParseException {
        // check if the order is preparing or exist
        if (!checkOrderExist(orderId, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        if (!checkForCompleteOrder(orderId)) {
            throw new BadRequestException("Invoice not yet generated. Please complete order to view invoice.");
        }
        Invoice invoice = supplierOrderRepository.getInvoiceByOrderId(orderId);
        if (invoice == null) {
            throw new InternalServerErrorException("Fail to fetch order invoice.");
        }
        invoice.getOrder().setDate(formatter.format(formatter.parse(invoice.getOrder().getDate())));
        return invoice;
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
