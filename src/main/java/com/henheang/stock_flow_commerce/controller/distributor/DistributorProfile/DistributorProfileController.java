package com.henheang.stock_flow_commerce.controller.distributor.DistributorProfile;

import com.henheang.stock_flow_commerce.controller.BaseController;
import com.henheang.stock_flow_commerce.common.api.ApiResponse;
import com.henheang.stock_flow_commerce.common.api.Code;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.distributor.Distributor;
import com.henheang.stock_flow_commerce.model.distributor.DistributorRequest;
import com.henheang.stock_flow_commerce.service.DistributorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Tag(name = "Distributor Profile Controller")
@RequestMapping("${base.distributor.v1}profiles")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class DistributorProfileController  extends BaseController {

    private final DistributorProfileService userProfileService;

    @Operation(summary = "Get user profile")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Distributor>> getUserProfileById() throws ParseException {
        AppUser appUser=(AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        Distributor data = userProfileService.getUserProfile(currentUserId);
        return ok(Code.PROFILE_FETCHED, data);
    }

    @Operation(summary = "Create profile")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<Distributor>> addUserProfile(@RequestBody DistributorRequest distributorRequest) throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        Distributor data = userProfileService.addUserProfile(currentUserId, distributorRequest);
        return created(Code.PROFILE_CREATED, data);
    }

    @Operation(summary = "Update profile")
    @PutMapping("/")
    public ResponseEntity<ApiResponse<Distributor>> updateUserProfile(@RequestBody DistributorRequest distributorRequest) throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        Distributor data = userProfileService.updateUserProfile(currentUserId, distributorRequest);
        return ok(Code.PROFILE_UPDATED, data);
    }
}
