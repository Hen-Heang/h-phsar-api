package com.henheang.stock_flow_commerce.controller.retailer.retailerprofile;

import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.retailer.Retailer;
import com.henheang.stock_flow_commerce.model.retailer.RetailerRequest;
import com.henheang.stock_flow_commerce.service.RetailerProfileService;
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
@Tag(name = "Retailer Profile Controller")
@RequestMapping("${base.retailer.v1}profiles")
@SecurityRequirement(name = "bearer")
public class RetailerProfileController {

    private final RetailerProfileService retailerProfileService;

     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date date;

    public RetailerProfileController(RetailerProfileService retailerProfileService) {
        this.retailerProfileService = retailerProfileService;
    }

    @PostMapping("")
    public ResponseEntity<?> createRetailerProfile(@RequestBody RetailerRequest retailerRequest) {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<Retailer> response= ApiResponse.<Retailer>builder()
                .status(HttpStatus.CREATED.value())
                .message("created successfully")
                .data(retailerProfileService.createRetailerProfile(currentUserId,retailerRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("")
    public ResponseEntity<?> getRetailerProfile() throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<Retailer> response= ApiResponse.<Retailer>builder()
                .status(HttpStatus.OK.value())
                .message("fetched successfully")
                .data(retailerProfileService.getRetailerProfile(currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("")
    public ResponseEntity<?> updateRetailerProfile(@RequestBody RetailerRequest retailerRequest)  {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<Retailer> response= ApiResponse.<Retailer>builder()
                .status(HttpStatus.OK.value())
                .message("updated successfully")
                .data(retailerProfileService.updateRetailerProfile(currentUserId, retailerRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}