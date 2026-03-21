package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.*;
import com.henheang.stock_flow_commerce.model.Cart.Cart;
import com.henheang.stock_flow_commerce.model.Cart.CartOrder;
import com.henheang.stock_flow_commerce.model.Cart.CartSummery;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.invoice.Invoice;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.order.OrderDetail;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import com.henheang.stock_flow_commerce.repository.*;
import com.henheang.stock_flow_commerce.service.OrderRetailerService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderRetailerServiceImplV1 implements OrderRetailerService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final NotificationRepository notificationRepository;
    private final StoreRepository storeRepository;
    private final DistributorProfileRepository distributorProfileRepository;
    private final RetailerProfileRepository retailerProfileRepository;
    private final OrderRetailerRepository orderRetailerRepository;
    private final OrderDistributorRepository orderDistributorRepository;
    private final ProductDistributorRepository productDistributorRepository;

    public OrderRetailerServiceImplV1(NotificationRepository notificationRepository, StoreRepository storeRepository, DistributorProfileRepository distributorProfileRepository, RetailerProfileRepository retailerProfileRepository, OrderRetailerRepository orderRetailerRepository, OrderDistributorRepository orderDistributorRepository, ProductDistributorRepository productDistributorRepository) {
        this.notificationRepository = notificationRepository;
        this.storeRepository = storeRepository;
        this.distributorProfileRepository = distributorProfileRepository;
        this.retailerProfileRepository = retailerProfileRepository;
        this.orderRetailerRepository = orderRetailerRepository;
        this.orderDistributorRepository = orderDistributorRepository;
        this.productDistributorRepository = productDistributorRepository;
    }

    @Override
    public List<ProductOrder> addProductToCart(Integer storeId, List<CartOrder> orders) {
        Set<Integer> cartOrderSet = orders.stream().map(CartOrder::getProductId).collect(Collectors.toSet());
//        System.out.println(orders.size());
//        System.out.println(cartOrderSet.size());
//        Check if size is equal
        if (orders.size() != cartOrderSet.size()) {
            throw new BadRequestException("2 or more duplicate product in cart.");
        }
        for (CartOrder order : orders) {
            if (order.getProductId() > 2147483646 || order.getQty() > 2147483646) {
                throw new BadRequestException("Integer value can not exceed 2147483646");
            }
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        if (!appUser.getIsVerified()) {
            throw new ConflictException("User is not verified. Can not perform order operation.");
        }
        if (!orderRetailerRepository.checkUserInfo(retailerId)) {
            throw new ConflictException("User have no profile. Please setup user profile to make order.");
        }
        // check if
        // if retailer have cart in other store beside this store throw error
        if (orderRetailerRepository.checkForCartInOtherStore(storeId, retailerId)) {
            throw new ConflictException("One cart is processing. Can only order once at a time. Please kindly wait for this order to be accepted.");
        }
        // check if this retailer have cart in this store
        Integer orderId;
        if (!orderRetailerRepository.checkForCart(storeId, retailerId)) {
            orderId = orderRetailerRepository.createCart(storeId, retailerId);
        } else {
            // Cart and Order is of the same table to Cart = Order (status id 9)
            orderId = orderRetailerRepository.getOrderIdByStoreIdAndRetailerId(storeId, retailerId);
//            throw new ConflictException("One cart is processing. Can only order once at a time. Please kindly wait for this order to be accepted.");
        }
        int count = 1;
        List<ProductOrder> productOrders = new ArrayList<>();
        for (CartOrder order : orders) {
            Integer productId = order.getProductId();
            if (!productDistributorRepository.checkStoreHasProduct(storeId, productId)) {
                // delete order detail and then order
                String confirmDeleteOrder = orderRetailerRepository.deleteOrder(orderId);
                if (confirmDeleteOrder == null) {
                    throw new InternalServerErrorException("Fail to delete import.");
                }
                throw new NotFoundException("Can not find this product id. Fail on count: " + count);
            }
            Integer qty = order.getQty();
            if (qty == 0 && !orderRetailerRepository.productIsInCart(productId, orderId) && orders.size() > 1) {
                continue;
            } else if (qty == 0 && !orderRetailerRepository.productIsInCart(productId, orderId) && orders.size() < 2) {
                throw new BadRequestException("Product can not have quantity of 0");
            }
            // check stock
            if (!orderRetailerRepository.checkStock(productId, qty)) {
                // delete order detail and then order
                String confirmDeleteOrder = orderRetailerRepository.deleteOrder(orderId);
                if (confirmDeleteOrder == null) {
                    throw new InternalServerErrorException("Fail to delete import detail.");
                }
                throw new ConflictException("Not enough product in stock. Fail on count: " + count);
            }
            // Get product unit price
            Double price = orderRetailerRepository.getProductPrice(productId);
            // Add to cart if product is already in
            String confirm = "2";
            if (orderRetailerRepository.productIsInCart(productId, orderId)) {
                if (qty == 0) {
                    orderRetailerRepository.removeProductFromCart(productId, orderId);
                } else {
                    confirm = orderRetailerRepository.updateProductQtyFromCart(productId, orderId, qty, price);
                }
            } else {
                confirm = orderRetailerRepository.addProductToCart(orderId, productId, qty, price);
            }
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Add to cart failed. Fail on count: " + count);
            }
            ProductOrder productOrder = orderRetailerRepository.getProductFromCart(orderId, productId);
            if (productOrder == null) {
                // delete order detail and then order
                String confirmDeleteOrder = orderRetailerRepository.deleteOrder(orderId);
                if (confirmDeleteOrder == null) {
                    throw new InternalServerErrorException("Fail to delete import detail.3");
                }
                throw new InternalServerErrorException("Fetch product failed. Fail on count: " + count);
            }
            productOrders.add(productOrder);
            count++;
        }
        return productOrders;
    }

    @Override
    public String removeProductFromCart(Integer productId) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        // check if this retailer have cart in this store
        if (!orderRetailerRepository.checkForAnyCart(retailerId)) {
            throw new NotFoundException("No cart is found.");
        }
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        // check if no product in the cart throw error
        if (!orderRetailerRepository.productIsInCart(productId, cartId)) {
            throw new BadRequestException("This product is not in the cart.");
        }
        String confirm = orderRetailerRepository.removeProductFromCart(productId, cartId);
        if (confirm == null) {
            throw new InternalServerErrorException("Fail to remove product from cart");
        }
        return "Removed product from cart.";
    }

    @Override
    public ProductOrder updateProductInCart(Integer productId, Integer qty) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        Integer storeId = orderRetailerRepository.getStoreIdByOrderId(cartId);
        if (!productDistributorRepository.checkStoreHasProduct(storeId, productId)) {
            throw new NotFoundException("This product is not found in this store.");
        }
        // check if this retailer have cart in this store
        if (!orderRetailerRepository.checkForCart(storeId, retailerId)) {
            throw new NotFoundException("No cart is found.");
        }
        // if qty is 0, remove
        if (qty == 0) {
            orderRetailerRepository.removeProductFromCart(productId, cartId);
            throw new OKException("Removed product from cart.");
        }
        // check stock
        if (!orderRetailerRepository.checkStock(productId, qty)) {
            throw new ConflictException("Not enough product in stock.");
        }
        Double price = orderRetailerRepository.getProductPrice(productId);
        String confirm = orderRetailerRepository.updateProductQtyFromCart(productId, cartId, qty, price);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Add to cart failed.");
        }
        return orderRetailerRepository.getProductFromCart(cartId, productId);
    }

    @Override
    public Cart viewCartDetail(Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
//        Integer orderId = orderRetailerRepository.getOrderIdByStoreIdAndRetailerId(storeId, retailerId);
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not found.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }

        Cart cart = new Cart();
//            Order order = orderRetailerRepository.viewCartDetailASC(pageNumber, pageSize, orderId);
        cart.setOrder(orderRetailerRepository.getOrderByOrderId(cartId));
        cart.getOrder().setDate(formatter.format(formatter.parse(cart.getOrder().getDate())));
        cart.setProducts(orderRetailerRepository.getProductOrderByOrderId(cartId, pageNumber, pageSize));
        Integer totalPage = getTotalPage(pageSize);
        if (totalPage < pageSize * pageNumber && cart.getProducts().isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (cart.getOrder() == null || cart.getProducts() == null) {
            throw new InternalServerErrorException("Fail to fetch cart.");
        }
        return cart;
    }

    @Override
    public Integer getTotalPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
//        Integer orderId = orderRetailerRepository.getOrderIdByStoreIdAndRetailerId(storeId, retailerId);
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        Integer totalProduct = orderRetailerRepository.getTotalProduct(cartId);
        int totalPage;
        if (totalProduct % pageSize == 0) {
            totalPage = totalProduct / pageSize;
        } else {
            totalPage = (totalProduct / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public String cancelCart() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not exist.");
        }
        String confirm = orderRetailerRepository.cancelCart(cartId);
        if (confirm == null) {
            throw new InternalServerErrorException("Fail to cancel cart.");
        }
        return "Cart is permanently deleted from record.";
    }

    @Override
    public String saveToDraft() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
//        Integer orderId = orderRetailerRepository.getOrderIdByStoreIdAndRetailerId(storeId, retailerId);
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not exist.");
        }
        String confirm = orderRetailerRepository.saveToDraft(cartId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Save to draft fail.");
        }
        return "Saved to draft";
    }

    @Override
    public String confirmOrder() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
//        Integer orderId = orderRetailerRepository.getOrderIdByStoreIdAndRetailerId(storeId, retailerId);
        Integer cartId = orderRetailerRepository.getUserCartId(retailerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not exist.");
        }
        String confirm = orderRetailerRepository.confirmOrder(cartId);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to confirm order.");
        }
        // create new order notification for distributor
        Integer distributorId = orderDistributorRepository.getDistributorIdByOrderId(cartId);
        String retailerName = retailerProfileRepository.getRetailerNameById(retailerId);
        Integer newOrder = notificationRepository.createDistributorNotification(distributorId, 3, cartId, "You have new order from retailer " + retailerName, "You have received new order.", "Your store have received a new order from retailer " + retailerName, false);
        if (newOrder == null) {
            throw new InternalServerErrorException("Fail to create new order notification.");
        }
        return "Order confirmed. Waiting for distributor to accept order.";
    }

    @Override
    public List<Order> getOrderActivities(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        List<Order> orders;
        if (sort.equalsIgnoreCase("asc")) {
            orders = orderRetailerRepository.getUserOrderActivitiesOrderByDateASC(retailerId, pageNumber, pageSize);
        } else {
            orders = orderRetailerRepository.getUserOrderActivitiesOrderByDateDESC(retailerId, pageNumber, pageSize);
        }
        Integer totalOrder = orderRetailerRepository.getTotalOrder(retailerId);
        if (totalOrder <= 0) {
            throw new NotFoundException("There is no order currently.");
        }
        Integer totalPage = getTotalOrderPage(pageSize, totalOrder);
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
    public Integer getTotalOrderPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        Integer totalOrder = orderRetailerRepository.getTotalOrder(retailerId);
        int totalPage;
        if (totalOrder % pageSize == 0) {
            totalPage = totalOrder / pageSize;
        } else {
            totalPage = (totalOrder / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public String confirmTransaction(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        // check order exist
        if (!checkOrderExist(id)) {
            throw new NotFoundException("Order does not exist");
        }
        // check if order is in confirming state
        if (!checkOrderIsConfirming(id)) {
            throw new NotFoundException("Can not confirm this transaction. Order is not ready.");
        }
        String confirm = orderRetailerRepository.confirmTransaction(id);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to confirm transaction.");
        }
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(id);
        Integer distributorId = distributorProfileRepository.getDistributorIdByStoreId(orderDetail.getOrder().getStoreId());
//        try {
//            check = notificationRepository.createDistributorNotification(distributorId, 1, orderDetail.getOrder().getId(), "The order #" + orderDetail.getOrder().getId() + " was delivered and received.", "Order received.", "The order #" + orderDetail.getOrder().getId() + " was delivered successfully.", false);
//        } catch (Exception e) {
//            notificationRepository.deleteNotification(check);
//            throw new InternalServerErrorException("Fail to create notification. Reason: " + e);
//        }
        // create order complete for distributor
        String retailerName = retailerProfileRepository.getRetailerNameById(retailerId);
        Integer orderComplete = notificationRepository.createDistributorNotification(distributorId, 9, id, "Retailer " + retailerName + " have comfirm transaction of order #" + id, "Retailer have received the delivery.", "Order #" + id + " is completed. The order have been delivered and Retailer have received the product.", false);
        if (orderComplete == null){
            throw new InternalServerErrorException("Fail to create notification.");
        }
        return "Transaction successfully confirm. Order id " + id;
    }

    @Override
    public Invoice viewInvoiceByOrderId(Integer id) throws ParseException {
        // check order exist
        if (!checkOrderExist(id)) {
            throw new NotFoundException("Order does not exist");
        }
        // check if order is in complete state
        if (!checkOrderIsComplete(id)) {
            throw new NotFoundException("Can not confirm this transaction. Order is not ready.");
        }
        Invoice invoice = orderDistributorRepository.getInvoiceByOrderId(id);
        if (invoice == null) {
            throw new InternalServerErrorException("Fail to fetch order invoice.");
        }
        invoice.getOrder().setDate(formatter.format(formatter.parse(invoice.getOrder().getDate())));
        return invoice;
    }

    @Override
    public OrderDetail getOrderDetailByOrderId(Integer id) throws ParseException {
        // check if order exist
        if (!orderRetailerRepository.checkOrderExist(id)) {
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
    public List<CartSummery> viewAllCarts() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        // check for cart
        if (!orderRetailerRepository.checkForAnyCart(currentUserId)) {
            throw new NotFoundException("Cart not found.");
        }
        // get cart
        List<CartSummery> carts = orderRetailerRepository.getAllCarts(currentUserId);
        // check if null
        if (carts == null || carts.isEmpty()) {
            throw new InternalServerErrorException("Fail to fetch carts");
        }
        return carts;
    }

    @Override
    public String markOrderAsArrived(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        // check if the order is preparing or exist
        if (!orderRetailerRepository.checkOrderExist(id)) {
            throw new NotFoundException("Order not found.");
        }
        if (!orderRetailerRepository.checkForDispatchingOrder(id, currentUserId)) {
            throw new NotFoundException("Order is not dispatching.");
        }
        // order arrived
        String confirm = orderDistributorRepository.orderDelivered(id);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Fail to update order.");
        }
        // create confirming notification / order arrived
        OrderDetail orderDetail = orderDistributorRepository.getOrderDetailsByOrderId(id);
        Integer distributorId = distributorProfileRepository.getDistributorIdByStoreId(orderDetail.getOrder().getStoreId());
        Integer delivering = notificationRepository.createDistributorNotification(distributorId, 8, orderDetail.getOrder().getId(), "Order #" + orderDetail.getOrder().getId() + " has arrived to the destination.", "Order has arrived.", "Order #" + orderDetail.getOrder().getId() + "  has arrived at the destination. Waiting for retailer to confirm the transaction.", false);
        if (delivering == null){
            throw new InternalServerErrorException("Fail to create notification.");
        }
        return "Finish Dispatching. Order is delivered.";
    }

    private boolean checkOrderIsComplete(Integer id) {
        return orderRetailerRepository.checkOrderIsComplete(id);
    }

    private boolean checkOrderExist(Integer id) {
        return orderRetailerRepository.checkOrderExist(id);
    }

    private boolean checkOrderIsConfirming(Integer id) {
        return orderRetailerRepository.orderIsConfirming(id);
    }

    public Integer getTotalOrderPage(Integer pageSize, Integer totalOrder) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        int totalPage;
        if (totalOrder % pageSize == 0) {
            totalPage = totalOrder / pageSize;
        } else {
            totalPage = (totalOrder / pageSize) + 1;
        }
        return totalPage;
    }


    private boolean isCartExist(Integer orderId) {
        return orderRetailerRepository.isCartExist(orderId);
    }


}
