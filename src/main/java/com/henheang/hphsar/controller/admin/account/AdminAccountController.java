package com.henheang.hphsar.controller.admin.account;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.appUser.AdminUserSummaryDto;
import com.henheang.hphsar.model.appUser.UpdateActiveStatusRequest;
import com.henheang.hphsar.service.AdminAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AdminAccountController — Phase 2: ADMIN-only supplier & buyer account management.
 * <p>
 * Endpoints:
 * GET   /suppliers            → paginated, searchable list of supplier accounts
 * GET   /suppliers/{id}       → single supplier detail
 * PATCH /suppliers/{id}/status → activate/deactivate a supplier account
 * GET   /buyers, /buyers/{id}, PATCH /buyers/{id}/status → same for buyers
 */
@RestController
@Tag(name = "Admin Account Controller")
@RequestMapping("${base.admin.v1}")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminAccountController extends BaseController {

    private final AdminAccountService adminAccountService;

    @GetMapping("/suppliers")
    public ResponseEntity<?> listSuppliers(@RequestParam(required = false) String search,
                                            @RequestParam Integer pageNumber,
                                            @RequestParam Integer pageSize) {
        return okPage(
                "Fetched suppliers successfully.",
                adminAccountService.listSuppliers(search, pageNumber, pageSize),
                pageNumber,
                pageSize,
                adminAccountService.countSuppliers(search)
        );
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<?> getSupplier(@PathVariable Integer id) {
        return ok("Fetched supplier successfully.", adminAccountService.getSupplierById(id));
    }

    @PatchMapping("/suppliers/{id}/status")
    public ResponseEntity<?> updateSupplierStatus(@PathVariable Integer id,
                                                   @RequestBody UpdateActiveStatusRequest request) {
        AdminUserSummaryDto updated = adminAccountService.updateSupplierActiveStatus(id, request.getIsActive());
        return ok("Supplier status updated successfully.", updated);
    }

    @GetMapping("/buyers")
    public ResponseEntity<?> listBuyers(@RequestParam(required = false) String search,
                                         @RequestParam Integer pageNumber,
                                         @RequestParam Integer pageSize) {
        return okPage(
                "Fetched buyers successfully.",
                adminAccountService.listBuyers(search, pageNumber, pageSize),
                pageNumber,
                pageSize,
                adminAccountService.countBuyers(search)
        );
    }

    @GetMapping("/buyers/{id}")
    public ResponseEntity<?> getBuyer(@PathVariable Integer id) {
        return ok("Fetched buyer successfully.", adminAccountService.getBuyerById(id));
    }

    @PatchMapping("/buyers/{id}/status")
    public ResponseEntity<?> updateBuyerStatus(@PathVariable Integer id,
                                                @RequestBody UpdateActiveStatusRequest request) {
        AdminUserSummaryDto updated = adminAccountService.updateBuyerActiveStatus(id, request.getIsActive());
        return ok("Buyer status updated successfully.", updated);
    }
}
