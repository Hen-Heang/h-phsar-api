package com.henheang.hphsar.controller.buyer.profile;

import com.henheang.hphsar.model.ApiResponse;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.buyer.Buyer;
import com.henheang.hphsar.model.buyer.BuyerRequest;
import com.henheang.hphsar.service.BuyerProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@Tag(name = "Buyer Profile Controller")
@RequestMapping("${base.buyer.v1}/profiles")
@SecurityRequirement(name = "bearerAuth")
public class BuyerProfileController {

    private final BuyerProfileService buyerProfileService;

     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date date;

    public BuyerProfileController(BuyerProfileService buyerProfileService) {
        this.buyerProfileService = buyerProfileService;
    }

    @PostMapping("")
    public ResponseEntity<?> createBuyerProfile(@RequestBody BuyerRequest buyerRequest) {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<Buyer> response= ApiResponse.<Buyer>builder()
                .status(HttpStatus.CREATED.value())
                .message("created successfully")
                .data(buyerProfileService.createBuyerProfile(currentUserId,buyerRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("")
    public ResponseEntity<?> getBuyerProfile() throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<Buyer> response= ApiResponse.<Buyer>builder()
                .status(HttpStatus.OK.value())
                .message("fetched successfully")
                .data(buyerProfileService.getBuyerProfile(currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("")
    public ResponseEntity<?> updateBuyerProfile(@RequestBody BuyerRequest buyerRequest)  {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<Buyer> response= ApiResponse.<Buyer>builder()
                .status(HttpStatus.OK.value())
                .message("updated successfully")
                .data(buyerProfileService.updateBuyerProfile(currentUserId, buyerRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}