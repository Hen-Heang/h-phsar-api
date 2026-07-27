package com.henheang.hphsar.controller.buyer.order;
import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.common.api.PagedResponse;
import com.henheang.hphsar.model.Cart.Cart;
import com.henheang.hphsar.model.Cart.CartOrder;
import com.henheang.hphsar.model.Cart.CartSummery;
import com.henheang.hphsar.model.invoice.Invoice;
import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.order.OrderDetail;
import com.henheang.hphsar.model.order.OrderStatusHistory;
import com.henheang.hphsar.model.product.ProductOrder;
import com.henheang.hphsar.service.BuyerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;


@Tag(name = "Buyer Order Controller")
@RequestMapping("${base.buyer.v1}/orders")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class BuyerOrderController extends BaseController {

    private final BuyerOrderService buyerOrderService;

    @Operation(summary = "Add to cart")
    @PostMapping("/cart")
    public ResponseEntity<ApiResponse<List<ProductOrder>>> addProductToCart(@RequestParam Integer storeId, @RequestBody List<CartOrder> orders) {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return created("Added product to cart.", buyerOrderService.addProductToCart(storeId, orders));
    }


    @Operation(summary = "Remove product from cart")
    @DeleteMapping("/cart/product")
    public ResponseEntity<ApiResponse<String>> removeProductInCart(@RequestParam Integer productId) {
        ValidationUtils.rejectIfExceedsIntLimit(productId);
        return ok("Removed product from cart.", buyerOrderService.removeProductFromCart(productId));
    }

    @Operation(summary = "Update product in cart")
    @PutMapping("/cart")
    public ResponseEntity<ApiResponse<ProductOrder>> updatePrductInCart(@RequestParam Integer productId, @RequestParam Integer qty) {
        ValidationUtils.rejectIfExceedsIntLimit(productId, qty);
        return ok("Updated product from cart.", buyerOrderService.updateProductInCart(productId, qty));
    }

    @Operation(summary = "View all cart")
    @GetMapping("/carts")
    public ResponseEntity<ApiResponse<List<CartSummery>>> viewAllCarts(){
        return ok("Fetch carts", buyerOrderService.viewAllCarts());
    }

    @Operation(summary = "View cart details")
    @GetMapping("/cart/details")
    public ResponseEntity<ApiResponse<Cart>> viewCartDetail(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        return ok("Fetch cart details.", buyerOrderService.viewCartDetail(pageNumber, pageSize));
    }

    @Operation(summary = "Cancel order / delete cart")
    @DeleteMapping("/cart/cancel")
    public ResponseEntity<ApiResponse<String>> cancelCart() {
        return ok("Cancel cart successfully.", buyerOrderService.cancelCart());
    }

    @Operation(summary = "Save to draft")
    @PutMapping("/cart/draft")
    public ResponseEntity<ApiResponse<String>> saveToDraft() {
        return ok("Saved card to draft successfully.", buyerOrderService.saveToDraft());
    }

    @Operation(summary = "Confirm order")
    @PutMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmOrder() {
        return ok("Confirm order successfully.", buyerOrderService.confirmOrder());
    }

    @Operation(summary = "Get order list and it's progress")
    @GetMapping
    public ResponseEntity<PagedResponse<Order>> getOrderActivities(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<Order> orders = buyerOrderService.getOrderActivities(sort, pageNumber, pageSize);
        return okPage("Fetch orders successfully.", orders, pageNumber, pageSize, buyerOrderService.getTotalOrderElements());
    }

    @Operation(summary = "Get order detail by order id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetail>> getOrderDetailByOrderId(@PathVariable Integer id) throws ParseException {
        return ok("Fetched order details successfully.", buyerOrderService.getOrderDetailByOrderId(id));
    }

    @Operation(summary = "Update order arrived")
    @PutMapping("/{id}/arrived")
    public ResponseEntity<ApiResponse<String>> markOrderAsArrived(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(Code.DELIVERY_ACCEPTED, buyerOrderService.markOrderAsArrived(id));
    }


    @Operation(summary = "confirm transaction")
    @PutMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<String>> confirmTransaction(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(Code.DELIVERY_ACCEPTED, buyerOrderService.confirmTransaction(id));
    }

    @Operation(summary = "View Invoice")
    @GetMapping("/invoice/{id}")
    public ResponseEntity<ApiResponse<Invoice>> viewInvoiceByOrderId(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Fetch invoice successfully.", buyerOrderService.viewInvoiceByOrderId(id));
    }

    @Operation(summary = "Cancel a draft or not-yet-accepted pending order")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Order cancelled.", buyerOrderService.cancelOrder(id));
    }

    @Operation(summary = "Get order status history")
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<OrderStatusHistory>>> getOrderHistory(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Fetched order history successfully.", buyerOrderService.getOrderHistory(id));
    }
}
