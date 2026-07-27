package com.henheang.hphsar.controller.buyer.report;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.model.buyer.report.BuyerReport;
import com.henheang.hphsar.service.BuyerReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@Tag(name = "Buyer Report Controller")
@RequestMapping("${base.buyer.v1}/reports")
@SecurityRequirement(name = "bearerAuth")
public class BuyerReportController extends BaseController {
    private final BuyerReportService buyerReportService;

    public BuyerReportController(BuyerReportService buyerReportService) {
        this.buyerReportService = buyerReportService;
    }

    @Operation(summary = "Get buyer report")
    @GetMapping
    public ResponseEntity<ApiResponse<BuyerReport>> getBuyerMonthlyReport(
            @RequestParam(defaultValue = "yyyy-mm") String startDate,
            @RequestParam(defaultValue = "yyyy-mm") String endDate
    ) throws ParseException {
        return created("Get successfully report", buyerReportService.getBuyerMonthlyReport(startDate,endDate));
    }
}