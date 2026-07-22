package com.henheang.hphsar.controller.buyer.report;

import com.henheang.hphsar.model.ApiResponse;
import com.henheang.hphsar.model.buyer.report.BuyerReport;
import com.henheang.hphsar.service.BuyerReportService;
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
@Tag(name = "Buyer Report Controller")
@RequestMapping("${base.buyer.v1}/reports")
@SecurityRequirement(name = "bearerAuth")
public class BuyerReportController {
    private final BuyerReportService buyerReportService;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;

    public BuyerReportController(BuyerReportService buyerReportService) {
        this.buyerReportService = buyerReportService;
    }

    @Operation(summary = "Get buyer report")
    @GetMapping
    public ResponseEntity<?> getBuyerMonthlyReport(
            @RequestParam(defaultValue = "yyyy-mm") String startDate,
            @RequestParam(defaultValue = "yyyy-mm") String endDate
    ) throws ParseException {
        ApiResponse<BuyerReport> response = ApiResponse.<BuyerReport>builder()
                .status(HttpStatus.OK.value())
                .message("Get successfully report")
                .data(buyerReportService.getBuyerMonthlyReport(startDate,endDate))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}