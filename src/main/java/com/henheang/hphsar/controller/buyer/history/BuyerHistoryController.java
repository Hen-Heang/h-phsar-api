package com.henheang.hphsar.controller.buyer.history;
import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.PagedResponse;
import com.henheang.hphsar.model.history.OrderDetailHistory;
import com.henheang.hphsar.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Buyer History Controller")
@RequestMapping("${base.buyer.v1}/history")
@SecurityRequirement(name = "bearerAuth")
public class BuyerHistoryController extends BaseController {
    private final HistoryService historyService;

    @Operation(summary = "get order history")
    @GetMapping("/order")
    public ResponseEntity<PagedResponse<OrderDetailHistory>> getOrderHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize ) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<OrderDetailHistory> history = historyService.getBuyerOrderHistory(sort, pageNumber, pageSize);
        return okPage("Fetched order history successfully.", history, pageNumber, pageSize, historyService.findBuyerTotalOrderElements());
    }

    @Operation(summary = "Get draft")
    @GetMapping("/draft")
    public ResponseEntity<PagedResponse<OrderDetailHistory>> getDraftHistory(@RequestParam(defaultValue = "asc") String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize ) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<OrderDetailHistory> draft = historyService.getDraftHistory(sort, pageNumber, pageSize);
        return okPage("Fetched order history successfully.", draft, pageNumber, pageSize, historyService.findBuyerTotalDraftElements());
    }

    @Operation(summary = "Delete draft")
    @DeleteMapping("/draft/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDraftById(@PathVariable Integer id){
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Deleted draft.", historyService.deleteDraftById(id));
    }

    @Operation(summary = "set draft to request")
    @PutMapping("/draft/{id}")
    public ResponseEntity<ApiResponse<OrderDetailHistory>> updateDraftById(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Updated draft.", historyService.updateDraftById(id));
    }
}
