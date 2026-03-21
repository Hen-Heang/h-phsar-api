package com.henheang.stock_flow_commerce.controller.distributor.report;

import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.report.DistributorReport;
import com.henheang.stock_flow_commerce.service.DistributorReportService;
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
@Tag(name = "Distributor Report Controller")
@RequestMapping("${base.distributor.v1}reports")
@SecurityRequirement(name = "bearer")
public class DistributorReportController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private final DistributorReportService distributorReportService;

    public DistributorReportController(DistributorReportService distributorReportService) {
        this.distributorReportService = distributorReportService;
    }
    @Operation(summary = "Get report")
    @GetMapping("")
    public ResponseEntity<?> getDistributorReport(@RequestParam(defaultValue = "yyyy-mm") String startDate, @RequestParam(defaultValue = "yyyy-mm") String endDate) throws ParseException {
        ApiResponse<DistributorReport> response = ApiResponse.<DistributorReport>builder()
                .status(HttpStatus.OK.value())
                .message("Report fetched.")
                .data(distributorReportService.getDistributorReport(startDate, endDate))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
