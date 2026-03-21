package com.henheang.stock_flow_commerce.controller.retailer.order;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.Cart.Cart;
import com.henheang.stock_flow_commerce.model.Cart.CartOrder;
import com.henheang.stock_flow_commerce.model.Cart.CartSummery;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.invoice.Invoice;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.order.OrderDetail;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import com.henheang.stock_flow_commerce.service.OrderRetailerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Tag(name = "Retailer order Controller")
@RequestMapping("${base.retailer.v1}orders")
@SecurityRequirement(name = "bearer")
@RestController
public class OrderRetailerController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final OrderRetailerService orderRetailerService;

    public OrderRetailerController(OrderRetailerService orderRetailerService) {
        this.orderRetailerService = orderRetailerService;
    }

    @Operation(summary = "Add to cart")
    @PostMapping("/cart")
    public ResponseEntity<?> addProductToCart(@RequestParam Integer storeId, @RequestBody List<CartOrder> orders) {
        if (storeId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<List<ProductOrder>> response = ApiResponse.<List<ProductOrder>>builder()
                .status(HttpStatus.CREATED.value())
                .message("Added product to cart.")
                .data(orderRetailerService.addProductToCart(storeId, orders))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(summary = "Remove product from cart")
    @DeleteMapping("/cart/product")
    public ResponseEntity<?> removeProductInCart(@RequestParam Integer productId) {
        if (productId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Removed product from cart.")
                .data(orderRetailerService.removeProductFromCart(productId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update product in cart")
    @PutMapping("/cart")
    public ResponseEntity<?> updatePrductInCart(@RequestParam Integer productId, @RequestParam Integer qty) {
        if (productId > 2147483646 || qty > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<ProductOrder> response = ApiResponse.<ProductOrder>builder()
                .status(HttpStatus.OK.value())
                .message("Updated product from cart.")
                .data(orderRetailerService.updateProductInCart(productId, qty))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View all cart")
    @GetMapping("/carts")
    public ResponseEntity<?> viewAllCarts(){
        ApiResponse<List<CartSummery>> response = ApiResponse.<List<CartSummery>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch carts")
                .data(orderRetailerService.viewAllCarts())
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View cart details")
    @GetMapping("/cart/details")
    public ResponseEntity<?> viewCartDetail(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<Cart> response = PaginationApiResponse.<Cart>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch cart details.")
                .data(orderRetailerService.viewCartDetail(pageNumber, pageSize))
                .totalPage(orderRetailerService.getTotalPage(pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel order / delete cart")
    @DeleteMapping("/cart/cancel")
    public ResponseEntity<?> cancelCart() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Cancel cart successfully.")
                .data(orderRetailerService.cancelCart())
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Save to draft")
    @PutMapping("/cart/draft")
    public ResponseEntity<?> saveToDraft() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Saved card to draft successfully.")
                .data(orderRetailerService.saveToDraft())
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm order")
    @PutMapping("/confirm")
    public ResponseEntity<?> confirmOrder() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Confirm order successfully.")
                .data(orderRetailerService.confirmOrder())
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order list and it's progress")
    @GetMapping("")
    public ResponseEntity<?> getOrderActivities(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch orders successfully.")
                .data(orderRetailerService.getOrderActivities(sort, pageNumber, pageSize))
                .totalPage(orderRetailerService.getTotalOrderPage(pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order detail by order id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetailByOrderId(@PathVariable Integer id) throws ParseException {
        ApiResponse<OrderDetail> response = ApiResponse.<OrderDetail>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched order details successfully.")
                .data(orderRetailerService.getOrderDetailByOrderId(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order arrived")
    @PutMapping("/{id}/arrived")
    public ResponseEntity<?> markOrderAsArrived(@PathVariable Integer id) {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Accepted delivery.")
                .data(orderRetailerService.markOrderAsArrived(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "confirm transaction")
    @PutMapping("/{id}/recieve")
    public ResponseEntity<?> confirmTransaction(@PathVariable Integer id) {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Accepted delivery.")
                .data(orderRetailerService.confirmTransaction(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View Invoice")
    @GetMapping("/invoice/{id}")
    public ResponseEntity<?> viewInvoiceByOrderId(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<Invoice> response = ApiResponse.<Invoice>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch invoice successfully.")
                .data(orderRetailerService.viewInvoiceByOrderId(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
