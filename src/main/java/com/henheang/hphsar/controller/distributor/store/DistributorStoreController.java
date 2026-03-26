package com.henheang.hphsar.controller.distributor.store;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.store.StoreRequest;
import com.henheang.hphsar.service.DistributorStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Tag(name = "Distributor Store Controller")
@RequestMapping("${base.distributor.v1}/stores")
@SecurityRequirement(name = "bearerAuth")
public class DistributorStoreController extends BaseController {

    private final DistributorStoreService distributorStoreService;

    public DistributorStoreController(DistributorStoreService distributorStoreService) {
        this.distributorStoreService = distributorStoreService;
    }
    @Operation(summary = "Setup new store")
    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
       return created(
                "Store created.",
                distributorStoreService.createNewStore(storeRequest,currentUserId)
        );
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserStore() throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Fetched store detail.",
                distributorStoreService.getUserStore(currentUserId)
        );
    }

    @PutMapping
    public ResponseEntity<?> editAllFieldUserStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store updated.",
                distributorStoreService.editAllFieldUserStore(currentUserId,storeRequest)
        );
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store deleted.",
                distributorStoreService.deleteUserStore(currentUserId)
        );
    }

    @PutMapping("/disable")
    public ResponseEntity<?> disableStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store is now inactive.",
                distributorStoreService.disableStore(currentUserId)
        );
    }

    @PutMapping("/enable")
    public ResponseEntity<?> enableStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store is now active.",
                distributorStoreService.enableStore(currentUserId)
        );
    }
}
