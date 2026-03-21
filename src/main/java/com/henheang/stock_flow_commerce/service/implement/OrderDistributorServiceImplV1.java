package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.ConflictException;
import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.invoice.Invoice;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.order.OrderDetail;
import com.henheang.stock_flow_commerce.repository.NotificationRepository;
import com.henheang.stock_flow_commerce.repository.OrderDistributorRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.OrderDistributorService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

@Service
public class OrderDistributorServiceImplV1 implements OrderDistributorService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final OrderDistributorRepository orderDistributorRepository;
    private final StoreRepository storeRepository;
    private final NotificationRepository notificationRepository;

    public OrderDistributorServiceImplV1(OrderDistributorRepository orderDistributorRepository, StoreRepository storeRepository, NotificationRepository notificationRepository) {
        this.orderDistributorRepository = orderDistributorRepository;
        this.storeRepository = storeRepository;
        this.notificationRepository = notificationRepository;
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
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderDistributorRepository.getAllOrderCurrentUserSortByCreatedDateASC(pageNumber, pageSize, storeId);
        } else {
            orders = orderDistributorRepository.getAllOrderCurrentUserSortByCreatedDateDESC(pageNumber, pageSize, storeId);
        }
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
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderDistributorRepository.getNewOrderCurrentUserSortByCreatedDateASC(pageNumber, pageSize, storeId);
        } else {
            orders = orderDistributorRepository.getNewOrderCurrentUserSortByCreatedDateDESC(pageNumber, pageSize, storeId);
        }
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
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderDistributorRepository.getPreparingOrderCurrentUserSortByCreatedDateASC(pageNumber, pageSize, storeId);
        } else {
            orders = orderDistributorRepository.getPreparingOrderCurrentUserSortByCreatedDateDESC(pageNumber, pageSize, storeId);
        }
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
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderDistributorRepository.getDispatchingOrderCurrentUserSortByCreatedDateASC(pageNumber, pageSize, storeId);
        } else {
            orders = orderDistributorRepository.getDispatchingOrderCurrentUserSortByCreatedDateDESC(pageNumber, pageSize, storeId);
        }
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
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderDistributorRepository.getConfirmingOrderCurrentUserSortByCreatedDateASC(pageNumber, pageSize, storeId);
        } else {
            orders = orderDistributorRepository.getConfirmingOrderCurrentUserSortByCreatedDateDESC(pageNumber, pageSize, storeId);
        }
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
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderDistributorRepository.getCompleteOrderCurrentUserSortByCreatedDateASC(pageNumber, pageSize, storeId);
        } else {
            orders = orderDistributorRepository.getCompleteOrderCurrentUserSortByCreatedDateDESC(pageNumber, pageSize, storeId);
        }
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
        return orderDistributorRepository.getTotalCompleteOrder(storeId);
    }

    @Override
    public Integer getTotalConfirmingOrder(Integer storeId) {
        return orderDistributorRepository.getTotalConfirmingOrder(storeId);
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
        return orderDistributorRepository.getCurrentStoreTotalOrder(storeId);
    }

    @Override
    public Integer getTotalNewOrder(Integer storeId) {
        return orderDistributorRepository.getCurrentStoreTotalNewOrder(storeId);
    }

    @Override
    public Integer getTotalPreparingOrder(Integer storeId) {
        return orderDistributorRepository.getCurrentStoreTotalPreparingOrder(storeId);
    }

    @Override
    public Integer getTotalDispatchingOrder(Integer storeId) {
        return orderDistributorRepository.getTotalDispatchingOrder(storeId);
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
        String confirm = orderDistributorRepository.acceptPendingOrder(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to accept order.");
        }
        // get order detail
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(orderId);
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
            Integer notificationCheck = notificationRepository.createDistributorNotification(currentUserId, 2, orderDetail.getOrder().getId(), "Product out of stock.", "Out of stock.", "Product #"+id+" are out of stock.", false);
            if (notificationCheck == null){
                notificationRepository.deleteNotification(notificationCheck);
                throw new InternalServerErrorException("Fail to create notification for out of stock");
            }
        }
        // create notification
        Integer check = 0;
        try {
            check = notificationRepository.createRetailerNotification(orderDetail.getOrder().getRetailerId(), 4, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() +" was accepted.", "Order accepted.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " was accepted and is being prepared. Please keep in contact and monitor the order progress.", false);
        } catch (Exception e) {
            notificationRepository.deleteNotification(check);
            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
        }
        return "Successfully accept order.";
    }

    private void deductStock(Integer orderId, Integer storeId) {
        List<Integer> productIds = orderDistributorRepository.getAllProductIdFromProductDetails(orderId);
        for (Integer product : productIds) {
            orderDistributorRepository.deductStock(orderId, product, storeId);
        }
    }

    private boolean checkAvailableProduct(Integer orderId) {
        // count how many order detail product qty is <= store product detail qty. eligible mean meet requirement
        Integer eligibleCount = orderDistributorRepository.productEligibleCount(orderId);
        // Select all product from product detail by order id
        Integer productDetailCount = orderDistributorRepository.getProductDetailCount(orderId);
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
        String confirm = orderDistributorRepository.declinePendingOrder(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to decline order.");
        }
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(orderId);
        Integer check = 0;
        try {
            check = notificationRepository.createRetailerNotification(orderDetail.getOrder().getRetailerId(), 5, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" was declined.", "Order declined.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " was declined. Please try to order from other store or contact the store.", false);
        } catch (Exception e) {
            notificationRepository.deleteNotification(check);
            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
        }
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
        String confirm = orderDistributorRepository.finishPreparing(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to update order.");
        }
        // create notification for delivering
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(orderId);
        Integer delivering = notificationRepository.createRetailerNotification(orderDetail.getOrder().getRetailerId(), 7, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" is being delivered.", "Order is being delivered.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " is being delivered. Your order will arrive shortly.", false);
        if (delivering == null){
            throw new InternalServerErrorException("Fail to create notification.");
        }
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
        String confirm = orderDistributorRepository.orderDelivered(orderId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to update order.");
        }
        // create confirming notification / order arrived
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(orderId);
        Integer delivering = notificationRepository.createRetailerNotification(orderDetail.getOrder().getRetailerId(), 8, orderDetail.getOrder().getId(), "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName()+" has arrived.", "Order has arrived.", "Your order #" + orderDetail.getOrder().getId() + " at store "+ orderDetail.getOrder().getStoreName() + " has arrived. Please confirm that you have received your order.", false);
        if (delivering == null){
            throw new InternalServerErrorException("Fail to create notification.");
        }
        return "Finish Dispatching. Order is delivered.";
    }

    @Override
    public OrderDetail getOrderDetailsByOrderId(Integer id, Integer storeId) throws ParseException {
        // check if the order is preparing or exist
        if (!checkOrderExist(id, storeId)) {
            throw new NotFoundException("Order not found.");
        }
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(id);
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
        Invoice invoice = orderDistributorRepository.getInvoiceByOrderId(orderId);
        if (invoice == null) {
            throw new InternalServerErrorException("Fail to fetch order invoice.");
        }
        invoice.getOrder().setDate(formatter.format(formatter.parse(invoice.getOrder().getDate())));
        return invoice;
    }

    private boolean checkForCompleteOrder(Integer orderId) {
        return orderDistributorRepository.checkForCompleteOrder(orderId);
    }

    private boolean checkForDispatchOrder(Integer orderId) {
        return orderDistributorRepository.checkForDispatchOrder(orderId);
    }

    private boolean checkForPreparingOrder(Integer orderId) {
        return orderDistributorRepository.checkForPreparingOrder(orderId);
    }

    Boolean checkOrderExist(Integer orderId, Integer storeId) {
        return orderDistributorRepository.checkOrderExist(orderId, storeId);
    }

    Boolean checkForPendingOrder(Integer orderId) {
        return orderDistributorRepository.checkForPendingOrder(orderId);
    }
}
