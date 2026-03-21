package com.henheang.stock_flow_commerce.controller.distributor.history;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.history.ImportHistory;
import com.henheang.stock_flow_commerce.model.history.OrderDetailHistory;
import com.henheang.stock_flow_commerce.service.HistoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "Distributor history Controller")
@RequestMapping("${base.distributor.v1}history")
@SecurityRequirement(name = "bearer")
public class DistributorHistoryController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final HistoryService historyService;

    public DistributorHistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/import")
    public ResponseEntity<?> getProductImportHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize ) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<ImportHistory>> response = PaginationApiResponse.<List<ImportHistory>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched import history successfully.")
                .data(historyService.getProductImportHistory(sort, pageNumber, pageSize))
                .totalPage(historyService.findTotalImportPage(pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order")
    public ResponseEntity<?> getOrderHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize ) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<OrderDetailHistory>> response = PaginationApiResponse.<List<OrderDetailHistory>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched order history  successfully.")
                .data(historyService.getOrderHistory(sort, pageNumber, pageSize))
                .totalPage(historyService.findTotalOrderPage(pageSize))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
