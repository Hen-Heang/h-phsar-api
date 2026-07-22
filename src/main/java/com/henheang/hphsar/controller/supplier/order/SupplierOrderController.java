package com.henheang.hphsar.controller.supplier.order;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.SupplierOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@RestController
@Tag(name = "Supplier Order Controller")
@RequestMapping("${base.supplier.v1}/orders")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SupplierOrderController extends BaseController {

    private final SupplierOrderService supplierOrderService;
    private final StoreRepository storeRepository;


    @Operation(summary = "Get order detail")
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getOrderDetailsByOrderId(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return ok("Fetched order detail.", supplierOrderService.getOrderDetailsByOrderId(id, storeId));
    }

    @Operation(summary = "Get Invoice")
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<?> getInvoiceByOrderId(@PathVariable Integer orderId) throws ParseException {
        if (orderId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return ok("Fetched invoice.", supplierOrderService.getInvoiceByOrderId(orderId, storeId));
    }

    @Operation(summary = "Get all order")
    @GetMapping
    public ResponseEntity<?> getAllOrderCurrentUserSortByCreatedDate(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return okPage(
                "Fetched All orders.",
                supplierOrderService.getAllOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId),
                pageNumber,
                pageSize,
                supplierOrderService.findTotalPage(supplierOrderService.getTotalOrder(storeId), pageSize)
        );
    }

    @Operation(summary = "Get all pending order")
    @GetMapping("/pending")
    public ResponseEntity<?> getNewOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return okPage(
                "Fetched pending orders.",
                supplierOrderService.getNewOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId),
                pageNumber,
                pageSize,
                supplierOrderService.findTotalPage(supplierOrderService.getTotalNewOrder(storeId), pageSize)
        );
    }

    @Operation(summary = "Accept pending order")
    @PutMapping("/pending/accept/{orderId}")
    public ResponseEntity<?> acceptPendingOrder(@PathVariable Integer orderId) {
        if (orderId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return ok(
                "Accepted order.",
                supplierOrderService.acceptPendingOrder(orderId, storeId)
        );
    }

    @Operation(summary = "Decline pending order")
    @PutMapping("/pending/decline/{orderId}")
    public ResponseEntity<?> declinePendingOrder(@PathVariable Integer orderId) {
        if (orderId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return ok(
                "Declined order.",
                supplierOrderService.declinePendingOrder(orderId, storeId)
        );
    }

    @Operation(summary = "Get all preparing order")
    @GetMapping("/preparing")
    public ResponseEntity<?> getPreparingOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return okPage(
                "Fetched preparing orders.",
                supplierOrderService.getPreparingOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId),
                pageNumber,
                pageSize,
                supplierOrderService.findTotalPage(supplierOrderService.getTotalPreparingOrder(storeId), pageSize)
        );
    }

    @Operation(summary = "Update preparing order to dispatching")
    @PutMapping("/preparing/{orderId}")
    public ResponseEntity<?> finishPreparing(@PathVariable Integer orderId) {
        if (orderId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return ok(
                "Order finished preparing.",
                supplierOrderService.finishPreparing(orderId, storeId)
        );
    }

    @Operation(summary = "Get all dispatching order")
    @GetMapping("/dispatching")
    public ResponseEntity<?> getDispatchingOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return okPage(
                "Fetched dispatching orders",
                supplierOrderService.getDispatchingOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId),
                pageNumber,
                pageSize,
                supplierOrderService.findTotalPage(supplierOrderService.getTotalDispatchingOrder(storeId), pageSize)
        );
    }

    @Operation(summary = "Update dispatching order to confirming")
    @PutMapping("/dispatching/{orderId}")
    public ResponseEntity<?> orderDelivered(@PathVariable Integer orderId) {
        if (orderId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return ok(
                "Order finished dispatching.",
                supplierOrderService.orderDelivered(orderId, storeId)
        );
    }

    @Operation(summary = "Get all confirming order")
    @GetMapping("/confirming")
    public ResponseEntity<?> getConfirmingOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        return okPage(
                "Fetched confirming orders.",
                supplierOrderService.getConfirmingOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId),
                pageNumber,
                pageSize,
                supplierOrderService.findTotalPage(supplierOrderService.getTotalConfirmingOrder(storeId), pageSize)
        );
    }

    @Operation(summary = "Get all complete order")
    @GetMapping("/complete")
    public ResponseEntity<?> getCompleteOrderCurrentUserSortByCreatedDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);

        return okPage(
                "Fetched Complete orders.",
                supplierOrderService.getCompleteOrderCurrentUserSortByCreatedDate(sort, pageNumber, pageSize, storeId),
                pageNumber,
                pageSize,
                supplierOrderService.findTotalPage(supplierOrderService.getTotalCompleteOrder(storeId), pageSize)
        );
    }
}
