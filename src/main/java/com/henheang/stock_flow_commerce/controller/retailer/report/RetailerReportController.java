package com.henheang.stock_flow_commerce.controller.retailer.report;

import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.retailer.report.RetailerReport;
import com.henheang.stock_flow_commerce.service.RetailerReportService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@Tag(name = "Retailer Report Controller")
@RequestMapping("${base.retailer.v1}reports")
@SecurityRequirement(name = "bearer")
public class RetailerReportController {
    private final RetailerReportService retailerReportService;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;

    public RetailerReportController(RetailerReportService retailerReportService) {
        this.retailerReportService = retailerReportService;
    }

    @Operation(summary = "Get retailer report")
    @GetMapping("")
    public ResponseEntity<?> getRetailerMonthlyReport(
            @RequestParam(defaultValue = "yyyy-mm") String startDate,
            @RequestParam(defaultValue = "yyyy-mm") String endDate
    ) throws ParseException {
        ApiResponse<RetailerReport> response = ApiResponse.<RetailerReport>builder()
                .status(HttpStatus.OK.value())
                .message("Get successfully report")
                .data(retailerReportService.getRetailerMonthlyReport(startDate,endDate))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}