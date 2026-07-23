package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.*;
import com.henheang.hphsar.model.Cart.Cart;
import com.henheang.hphsar.model.Cart.CartOrder;
import com.henheang.hphsar.model.Cart.CartSummery;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.appUser.Role;
import com.henheang.hphsar.model.invoice.Invoice;
import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.order.OrderDetail;
import com.henheang.hphsar.model.order.OrderStatus;
import com.henheang.hphsar.model.order.OrderStatusHistory;
import com.henheang.hphsar.model.product.ProductOrder;
import com.henheang.hphsar.repository.*;
import com.henheang.hphsar.service.BuyerOrderService;
import com.henheang.hphsar.service.OrderStatusService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BuyerOrderServiceImpl implements BuyerOrderService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final NotificationRepository notificationRepository;
    private final StoreRepository storeRepository;
    private final SupplierProfileRepository supplierProfileRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final BuyerOrderRepository buyerOrderRepository;
    private final SupplierOrderRepository supplierOrderRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final OrderStatusService orderStatusService;

    public BuyerOrderServiceImpl(NotificationRepository notificationRepository, StoreRepository storeRepository, SupplierProfileRepository supplierProfileRepository, BuyerProfileRepository buyerProfileRepository, BuyerOrderRepository buyerOrderRepository, SupplierOrderRepository supplierOrderRepository, SupplierProductRepository supplierProductRepository, OrderStatusService orderStatusService) {
        this.notificationRepository = notificationRepository;
        this.storeRepository = storeRepository;
        this.supplierProfileRepository = supplierProfileRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.buyerOrderRepository = buyerOrderRepository;
        this.supplierOrderRepository = supplierOrderRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.orderStatusService = orderStatusService;
    }

    @Override
    @Transactional
    public List<ProductOrder> addProductToCart(Integer storeId, List<CartOrder> orders) {
        Set<Integer> cartOrderSet = orders.stream().map(CartOrder::getProductId).collect(Collectors.toSet());
        if (orders.size() != cartOrderSet.size()) {
            throw new BadRequestException("2 or more duplicate product in cart.");
        }
        for (CartOrder order : orders) {
            if (order.getProductId() > 2147483646 || order.getQty() > 2147483646) {
                throw new BadRequestException("Integer value can not exceed 2147483646");
            }
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        if (!appUser.getIsVerified()) {
            throw new ConflictException("User is not verified. Can not perform order operation.");
        }
        if (!buyerOrderRepository.checkUserInfo(buyerId)) {
            throw new ConflictException("User have no profile. Please setup user profile to make order.");
        }
        // if buyer have cart in other store beside this store throw error
        if (buyerOrderRepository.checkForCartInOtherStore(storeId, buyerId)) {
            throw new ConflictException("One cart is processing. Can only order once at a time. Please kindly wait for this order to be accepted.");
        }
        // check if this buyer have cart in this store
        Integer orderId;
        if (!buyerOrderRepository.checkForCart(storeId, buyerId)) {
            orderId = buyerOrderRepository.createCart(storeId, buyerId);
        } else {
            // Cart and Order is of the same table to Cart = Order (status id 9)
            orderId = buyerOrderRepository.getOrderIdByStoreIdAndBuyerId(storeId, buyerId);
        }
        int count = 1;
        List<ProductOrder> productOrders = new ArrayList<>();
        for (CartOrder order : orders) {
            Integer productId = order.getProductId();
            if (!supplierProductRepository.checkStoreHasProduct(storeId, productId)) {
                throw new NotFoundException("Can not find this product id. Fail on count: " + count);
            }
            Integer qty = order.getQty();
            if (qty == 0 && !buyerOrderRepository.productIsInCart(productId, orderId) && orders.size() > 1) {
                continue;
            } else if (qty == 0 && !buyerOrderRepository.productIsInCart(productId, orderId) && orders.size() < 2) {
                throw new BadRequestException("Product can not have quantity of 0");
            }
            // check stock
            if (!buyerOrderRepository.checkStock(productId, qty)) {
                throw new ConflictException("Not enough product in stock. Fail on count: " + count);
            }
            // Get product unit price
            Double price = buyerOrderRepository.getProductPrice(productId);
            // Add to cart if product is already in
            String confirm = "2";
            if (buyerOrderRepository.productIsInCart(productId, orderId)) {
                if (qty == 0) {
                    buyerOrderRepository.removeProductFromCart(productId, orderId);
                } else {
                    confirm = buyerOrderRepository.updateProductQtyFromCart(productId, orderId, qty, price);
                }
            } else {
                confirm = buyerOrderRepository.addProductToCart(orderId, productId, qty, price);
            }
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Add to cart failed. Fail on count: " + count);
            }
            ProductOrder productOrder = buyerOrderRepository.getProductFromCart(orderId, productId);
            if (productOrder == null) {
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
        Integer buyerId = appUser.getId();
        // check if this buyer have cart in this store
        if (!buyerOrderRepository.checkForAnyCart(buyerId)) {
            throw new NotFoundException("No cart is found.");
        }
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        // check if no product in the cart throw error
        if (!buyerOrderRepository.productIsInCart(productId, cartId)) {
            throw new BadRequestException("This product is not in the cart.");
        }
        String confirm = buyerOrderRepository.removeProductFromCart(productId, cartId);
        if (confirm == null) {
            throw new InternalServerErrorException("Fail to remove product from cart");
        }
        return "Removed product from cart.";
    }

    @Override
    public ProductOrder updateProductInCart(Integer productId, Integer qty) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        Integer storeId = buyerOrderRepository.getStoreIdByOrderId(cartId);
        if (!supplierProductRepository.checkStoreHasProduct(storeId, productId)) {
            throw new NotFoundException("This product is not found in this store.");
        }
        // check if this buyer have cart in this store
        if (!buyerOrderRepository.checkForCart(storeId, buyerId)) {
            throw new NotFoundException("No cart is found.");
        }
        // if qty is 0, remove
        if (qty == 0) {
            buyerOrderRepository.removeProductFromCart(productId, cartId);
            throw new OKException("Removed product from cart.");
        }
        // check stock
        if (!buyerOrderRepository.checkStock(productId, qty)) {
            throw new ConflictException("Not enough product in stock.");
        }
        Double price = buyerOrderRepository.getProductPrice(productId);
        String confirm = buyerOrderRepository.updateProductQtyFromCart(productId, cartId, qty, price);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Add to cart failed.");
        }
        return buyerOrderRepository.getProductFromCart(cartId, productId);
    }

    @Override
    public Cart viewCartDetail(Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not found.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }

        Cart cart = new Cart();
        cart.setOrder(buyerOrderRepository.getOrderByOrderId(cartId));
        cart.getOrder().setDate(formatter.format(formatter.parse(cart.getOrder().getDate())));
        cart.setProducts(buyerOrderRepository.getProductOrderByOrderId(cartId, pageNumber, pageSize));
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
        Integer buyerId = appUser.getId();
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        Integer totalProduct = buyerOrderRepository.getTotalProduct(cartId);
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
        Integer buyerId = appUser.getId();
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not exist.");
        }
        String confirm = buyerOrderRepository.cancelCart(cartId);
        if (confirm == null) {
            throw new InternalServerErrorException("Fail to cancel cart.");
        }
        return "Cart is permanently deleted from record.";
    }

    @Override
    @Transactional
    public String saveToDraft() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not exist.");
        }
        orderStatusService.transitionOrder(cartId, OrderStatus.CART, OrderStatus.DRAFT, buyerId, Role.BUYER, "Buyer saved cart as draft");
        return "Saved to draft";
    }

    @Override
    @Transactional
    public String confirmOrder() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        Integer cartId = buyerOrderRepository.getUserCartId(buyerId);
        // check if cart exist
        if (!isCartExist(cartId)) {
            throw new NotFoundException("Cart does not exist.");
        }
        orderStatusService.transitionOrder(cartId, OrderStatus.CART, OrderStatus.PENDING, buyerId, Role.BUYER, "Buyer submitted cart");
        // create new order notification for supplier
        Integer supplierId = supplierOrderRepository.getSupplierIdByOrderId(cartId);
        String buyerName = buyerProfileRepository.getBuyerNameById(buyerId);
        Integer newOrder = notificationRepository.createSupplierNotification(supplierId, 3, cartId, "You have new order from buyer " + buyerName, "You have received new order.", "Your store have received a new order from buyer " + buyerName, false);
        if (newOrder == null) {
            throw new InternalServerErrorException("Fail to create new order notification.");
        }
        return "Order confirmed. Waiting for supplier to accept order.";
    }

    @Override
    public List<Order> getOrderActivities(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        // check sort spelling
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        List<Order> orders = buyerOrderRepository.getUserOrderActivities(sort, buyerId, pageNumber, pageSize);
        Integer totalOrder = buyerOrderRepository.getTotalOrder(buyerId);
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
        Integer buyerId = appUser.getId();
        Integer totalOrder = buyerOrderRepository.getTotalOrder(buyerId);
        int totalPage;
        if (totalOrder % pageSize == 0) {
            totalPage = totalOrder / pageSize;
        } else {
            totalPage = (totalOrder / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    @Transactional
    public String confirmTransaction(Integer id) {
        // Deprecated alias of markOrderAsArrived: the audit found both endpoints
        // performed (or, in this method's case, were meant to perform) the exact
        // same buyer action — confirming receipt of a dispatched order. Kept as a
        // separate route only so any existing caller of PUT /{id}/receive keeps
        // working; both now delegate to the same centralized transition instead
        // of maintaining two copies of the same business logic. Previously this
        // method required status_id=4, a status nothing ever set — it was
        // permanently unreachable.
        confirmReceipt(id);
        return "Transaction successfully confirm. Order id " + id;
    }

    @Override
    public Invoice viewInvoiceByOrderId(Integer id) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        // check order exist AND belongs to this buyer (ownership check — prevents IDOR)
        if (!buyerOrderRepository.checkOrderExistForBuyer(id, buyerId)) {
            throw new NotFoundException("Order does not exist");
        }
        // check if order is in complete state
        if (!checkOrderIsComplete(id)) {
            throw new NotFoundException("Can not confirm this transaction. Order is not ready.");
        }
        Invoice invoice = supplierOrderRepository.getInvoiceByOrderId(id);
        if (invoice == null) {
            throw new InternalServerErrorException("Fail to fetch order invoice.");
        }
        invoice.getOrder().setDate(formatter.format(formatter.parse(invoice.getOrder().getDate())));
        return invoice;
    }

    @Override
    public OrderDetail getOrderDetailByOrderId(Integer id) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        // check order exist AND belongs to this buyer (ownership check — prevents IDOR)
        if (!buyerOrderRepository.checkOrderExistForBuyer(id, buyerId)) {
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
    public List<CartSummery> viewAllCarts() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        // check for cart
        if (!buyerOrderRepository.checkForAnyCart(currentUserId)) {
            throw new NotFoundException("Cart not found.");
        }
        // get cart
        List<CartSummery> carts = buyerOrderRepository.getAllCarts(currentUserId);
        // check if null
        if (carts == null || carts.isEmpty()) {
            throw new InternalServerErrorException("Fail to fetch carts");
        }
        return carts;
    }

    @Override
    @Transactional
    public String markOrderAsArrived(Integer id) {
        // The canonical buyer receipt-confirmation action (DISPATCHED -> COMPLETED).
        // Supplier's own former "order delivered" action performed this identical
        // transition — retired: per the redesigned lifecycle, supplier
        // responsibility ends at dispatch, and only the buyer confirms receipt.
        confirmReceipt(id);
        return "Finish Dispatching. Order is delivered.";
    }

    private void confirmReceipt(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        // Ownership + precondition combined (prevents IDOR and enforces DISPATCHED in one check).
        if (!buyerOrderRepository.checkForDispatchingOrder(id, buyerId)) {
            throw new NotFoundException("Order not found or is not ready to confirm.");
        }
        orderStatusService.transitionOrder(id, OrderStatus.DISPATCHED, OrderStatus.COMPLETED, buyerId, Role.BUYER, "Buyer confirmed receipt");
        OrderDetail orderDetail = supplierOrderRepository.getOrderDetailsByOrderId(id);
        Integer supplierId = supplierProfileRepository.getSupplierIdByStoreId(orderDetail.getOrder().getStoreId());
        Integer delivered = notificationRepository.createSupplierNotification(supplierId, 9, orderDetail.getOrder().getId(), "Buyer confirmed receipt of order #" + orderDetail.getOrder().getId() + ".", "Order complete.", "Order #" + orderDetail.getOrder().getId() + " is complete. The buyer has confirmed receipt.", false);
        if (delivered == null) {
            throw new InternalServerErrorException("Fail to create notification.");
        }
    }

    @Override
    @Transactional
    public String cancelOrder(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        // Ownership-scoped, restricted to the states a buyer may cancel (DRAFT, PENDING).
        if (!buyerOrderRepository.checkOrderExistForBuyerCancellable(id, buyerId)) {
            throw new NotFoundException("Order not found or can no longer be cancelled.");
        }
        OrderStatus current = orderStatusService.getCurrentStatus(id);
        orderStatusService.transitionOrder(id, current, OrderStatus.CANCELLED, buyerId, Role.BUYER, "Buyer cancelled order");
        return "Order cancelled.";
    }

    @Override
    public List<OrderStatusHistory> getOrderHistory(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer buyerId = appUser.getId();
        if (!buyerOrderRepository.checkOrderExistForBuyer(id, buyerId)
                && !buyerOrderRepository.checkOrderExistForBuyerCancellable(id, buyerId)) {
            throw new NotFoundException("Order not found.");
        }
        return orderStatusService.getHistory(id);
    }

    private boolean checkOrderIsComplete(Integer id) {
        return buyerOrderRepository.checkOrderIsComplete(id);
    }

    public Integer getTotalOrderPage(Integer pageSize, Integer totalOrder) {
        int totalPage;
        if (totalOrder % pageSize == 0) {
            totalPage = totalOrder / pageSize;
        } else {
            totalPage = (totalOrder / pageSize) + 1;
        }
        return totalPage;
    }


    private boolean isCartExist(Integer orderId) {
        return buyerOrderRepository.isCartExist(orderId);
    }


}
