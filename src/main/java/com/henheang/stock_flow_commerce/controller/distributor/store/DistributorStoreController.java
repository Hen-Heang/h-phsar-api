package com.henheang.stock_flow_commerce.controller.distributor.store;

import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.store.Store;
import com.henheang.stock_flow_commerce.model.store.StoreRequest;
import com.henheang.stock_flow_commerce.service.DistributorStoreService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Distributor Store Controller")
@RequestMapping("${base.distributor.v1}stores")
@SecurityRequirement(name = "bearer")
public class DistributorStoreController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;
    private final DistributorStoreService distributorStoreService;

    public DistributorStoreController(DistributorStoreService distributorStoreService) {
        this.distributorStoreService = distributorStoreService;
    }
    @Operation(summary = "Setup new store")
    @PostMapping("")
    public ResponseEntity<?> createStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<Store> storeApiResponse = ApiResponse.<Store>builder()
                .status(HttpStatus.CREATED.value())
                .message("Created new Store.")
                .data(distributorStoreService.createNewStore(storeRequest, currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(storeApiResponse);
    }

    @GetMapping("/user/")
    public ResponseEntity<?> getUserStore() throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<Store> response = ApiResponse.<Store>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(distributorStoreService.getUserStore(currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("")
    public ResponseEntity<?> editAllFieldUserStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<Store> response = ApiResponse.<Store>builder()
                .status(HttpStatus.OK.value())
                .message("Store updated.")
                .data(distributorStoreService.editAllFieldUserStore(currentUserId, storeRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUserStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Store deleted.")
                .data(distributorStoreService.deleteUserStore(currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/disable")
    public ResponseEntity<?> disableStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Store is deactivated.")
                .data(distributorStoreService.disableStore(currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/enable")
    public ResponseEntity<?> enableStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Store is now active.")
                .data(distributorStoreService.enableStore(currentUserId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
