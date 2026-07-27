package com.henheang.hphsar.controller.buyer.profile;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.model.buyer.Buyer;
import com.henheang.hphsar.model.buyer.BuyerRequest;
import com.henheang.hphsar.service.BuyerProfileService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;


@RestController
@RequiredArgsConstructor
@Tag(name = "Buyer Profile Controller")
@RequestMapping("${base.buyer.v1}/profiles")
@SecurityRequirement(name = "bearerAuth")
public class BuyerProfileController extends BaseController {

    private final BuyerProfileService buyerProfileService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Buyer>> createBuyerProfile(@RequestBody BuyerRequest buyerRequest) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return created("created successfully", buyerProfileService.createBuyerProfile(currentUserId,buyerRequest));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<Buyer>> getBuyerProfile() throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(Code.PROFILE_FETCHED, buyerProfileService.getBuyerProfile(currentUserId));
    }

    @PutMapping("")
    public ResponseEntity<ApiResponse<Buyer>> updateBuyerProfile(@RequestBody BuyerRequest buyerRequest)  {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok("updated successfully", buyerProfileService.updateBuyerProfile(currentUserId, buyerRequest));
    }
}