package com.henheang.stock_flow_commerce.controller.distributor.order;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.invoice.Invoice;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.order.OrderDetail;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.OrderDistributorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "Distributor order Controller")
@RequestMapping("${base.distributor.v1}orders")
@SecurityRequirement(name = "bearer")
public class OrderDistributorController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final OrderDistributorService orderDistributorService;
    private final StoreRepository storeRepository;

    public OrderDistributorController(OrderDistributorService orderDistributorService, StoreRepository storeRepository) {
        this.orderDistributorService = orderDistributorService;
        this.storeRepository = storeRepository;
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getOrderDetailsByOrderId(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        ApiResponse<OrderDetail> response = ApiResponse.<OrderDetail>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched order details.")
                .data(orderDistributorService.getOrderDetailsByOrderId(id, storeId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Invoice")
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<?> getInvoiceByOrderId(@PathVariable Integer orderId) throws ParseException {
        if (orderId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        ApiResponse<Invoice> response = ApiResponse.<Invoice>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched invoice.")
                .data(orderDistributorService.getInvoiceByOrderId(orderId, storeId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all order")
    @GetMapping("")
    public ResponseEntity<?> getAllOrderCurrentUserSortByCreatedDate(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched orders.")
                .data(orderDistributorService.getAllOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId))
                .totalPage(orderDistributorService.findTotalPage(orderDistributorService.getTotalOrder(storeId), pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all pending order")
    @GetMapping("/pending")
    public ResponseEntity<?> getNewOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched orders.")
                .data(orderDistributorService.getNewOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId))
                .totalPage(orderDistributorService.findTotalPage(orderDistributorService.getTotalNewOrder(storeId), pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Accept pending order")
    @PutMapping("/pending/accept/{orderId}")
    public ResponseEntity<?> acceptPendingOrder(@PathVariable Integer orderId){
        if (orderId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Accepted order.")
                .data(orderDistributorService.acceptPendingOrder(orderId, storeId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Decline pending order")
    @PutMapping("/pending/decline/{orderId}")
    public ResponseEntity<?> declinePendingOrder(@PathVariable Integer orderId){
        if (orderId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Declined order.")
                .data(orderDistributorService.declinePendingOrder(orderId, storeId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all preparing order")
    @GetMapping("/preparing")
    public ResponseEntity<?> getPreparingOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched orders.")
                .data(orderDistributorService.getPreparingOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId))
                .totalPage(orderDistributorService.findTotalPage(orderDistributorService.getTotalPreparingOrder(storeId), pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update preparing order to dispatching")
    @PutMapping("/preparing/{orderId}")
    public ResponseEntity<?> finishPreparing(@PathVariable Integer orderId){
        if (orderId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Order finished preparing.")
                .data(orderDistributorService.finishPreparing(orderId, storeId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all dispatching order")
    @GetMapping("/dispatching")
    public ResponseEntity<?> getDispatchingOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched orders.")
                .data(orderDistributorService.getDispatchingOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId))
                .totalPage(orderDistributorService.findTotalPage(orderDistributorService.getTotalDispatchingOrder(storeId), pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update dispatching order to confirming")
    @PutMapping("/dispatching/{orderId}")
    public ResponseEntity<?> orderDelivered(@PathVariable Integer orderId){
        if (orderId > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Order delivered.")
                .data(orderDistributorService.orderDelivered(orderId, storeId))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all confirming order")
    @GetMapping("/confirming")
    public ResponseEntity<?> getConfirmingOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched orders.")
                .data(orderDistributorService.getConfirmingOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId))
                .totalPage(orderDistributorService.findTotalPage(orderDistributorService.getTotalConfirmingOrder(storeId), pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all complete order")
    @GetMapping("/complete")
    public ResponseEntity<?> getCompleteOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        PaginationApiResponse<List<Order>> response = PaginationApiResponse.<List<Order>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched orders.")
                .data(orderDistributorService.getCompleteOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId))
                .totalPage(orderDistributorService.findTotalPage(orderDistributorService.getTotalCompleteOrder(storeId), pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
