package com.henheang.hphsar.controller.supplier.history;
import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
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
@Tag(name = "Supplier History Controller")
@RequestMapping("${base.supplier.v1}/history")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SupplierHistoryController extends BaseController {
    private final HistoryService historyService;

    @GetMapping("/import")
    public ResponseEntity<?> getProductImportHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
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
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);

        return okPage(
                "Fetched order history  successfully.",
                historyService.getOrderHistory(sort, pageNumber, pageSize),
                pageNumber,
                pageSize,
                historyService.findTotalOrderPage(pageSize)
        );
    }
}
