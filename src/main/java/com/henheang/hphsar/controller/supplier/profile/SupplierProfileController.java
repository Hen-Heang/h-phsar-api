package com.henheang.hphsar.controller.supplier.profile;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.supplier.Supplier;
import com.henheang.hphsar.model.supplier.SupplierRequest;
import com.henheang.hphsar.service.SupplierProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Tag(name = "Supplier Profile Controller")
@RequestMapping("${base.supplier.v1}/profiles")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SupplierProfileController  extends BaseController {

    private final SupplierProfileService userProfileService;

    @Operation(summary = "Get user profile")
    @GetMapping
    public ResponseEntity<ApiResponse<Supplier>> getUserProfileById() throws ParseException {
        AppUser appUser=(AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        Supplier data = userProfileService.getUserProfile(currentUserId);
        return ok(Code.PROFILE_FETCHED, data);
    }

    @Operation(summary = "Create profile")
    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> addUserProfile(@RequestBody SupplierRequest supplierRequest) throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        Supplier data = userProfileService.addUserProfile(currentUserId, supplierRequest);
        return created(Code.PROFILE_CREATED, data);
    }

    @Operation(summary = "Update profile")
    @PutMapping
    public ResponseEntity<ApiResponse<Supplier>> updateUserProfile(@RequestBody SupplierRequest supplierRequest) throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        Supplier data = userProfileService.updateUserProfile(currentUserId, supplierRequest);
        return ok(Code.PROFILE_UPDATED, data);
    }
}
