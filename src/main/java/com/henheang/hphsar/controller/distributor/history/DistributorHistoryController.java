package com.henheang.hphsar.controller.distributor.history;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.service.HistoryService;
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
@Tag(name = "Distributor history Controller")
@RequestMapping("${base.distributor.v1}/history")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DistributorHistoryController extends BaseController {
    private final HistoryService historyService;

    @GetMapping("/import")
    public ResponseEntity<?> getProductImportHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        return okPage(
                "Fetched import history successfully.",
                historyService.getProductImportHistory(sort, pageNumber, pageSize),
                pageNumber,
                pageSize,
                historyService.findTotalImportPage(pageSize)
        );
    }

    @GetMapping("/order")
    public ResponseEntity<?> getOrderHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }

        return okPage(
                "Fetched order history  successfully.",
                historyService.getOrderHistory(sort, pageNumber, pageSize),
                pageNumber,
                pageSize,
                historyService.findTotalOrderPage(pageSize)
        );
    }
}
