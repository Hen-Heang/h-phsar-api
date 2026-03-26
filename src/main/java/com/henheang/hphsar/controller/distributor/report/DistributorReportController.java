package com.henheang.hphsar.controller.distributor.report;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.service.DistributorReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@Tag(name = "Distributor Report Controller")
@RequestMapping("${base.distributor.v1}/reports")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DistributorReportController extends BaseController {

    private final DistributorReportService distributorReportService;
    @Operation(summary = "Get report")
    @GetMapping
    public ResponseEntity<?> getDistributorReport(@RequestParam(defaultValue = "yyyy-mm") String startDate, @RequestParam(defaultValue = "yyyy-mm") String endDate) throws ParseException {
       return ok("Fetched report.", distributorReportService.getDistributorReport(startDate, endDate)
       );
    }
}
