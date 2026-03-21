package com.henheang.stock_flow_commerce.controller.retailer.history;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.history.OrderDetailHistory;
import com.henheang.stock_flow_commerce.service.HistoryService;
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

@RestController
@Tag(name = "Retailer history Controller")
@RequestMapping("${base.retailer.v1}history")
@SecurityRequirement(name = "bearer")
public class RetailerHistoryController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final HistoryService historyService;

    public RetailerHistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Operation(summary = "get order history")
    @GetMapping("/order")
    public ResponseEntity<?> getOrderHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize ) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<OrderDetailHistory>> response = PaginationApiResponse.<List<OrderDetailHistory>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched order history successfully.")
                .data(historyService.getRetailerOrderHistory(sort, pageNumber, pageSize))
                .totalPage(historyService.findRetailerTotalOrderPage(pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get draft")
    @GetMapping("/draft")
    public ResponseEntity<?> getDraftHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize ) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<OrderDetailHistory>> response = PaginationApiResponse.<List<OrderDetailHistory>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched order history successfully.")
                .data(historyService.getDraftHistory(sort, pageNumber, pageSize))
                .totalPage(historyService.findRetailerTotalDraftPage(pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete draft")
    @DeleteMapping("/draft/{id}")
    public ResponseEntity<?> deleteDraftById(@PathVariable Integer id){
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Deleted draft.")
                .data(historyService.deleteDraftById(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "set draft to request")
    @PutMapping("/draft/{id}")
    public ResponseEntity<?> updateDraftById(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<OrderDetailHistory> response = ApiResponse.<OrderDetailHistory>builder()
                .status(HttpStatus.OK.value())
                .message("Updated draft.")
                .data(historyService.updateDraftById(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
