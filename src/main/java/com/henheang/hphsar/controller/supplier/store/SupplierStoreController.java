package com.henheang.hphsar.controller.supplier.store;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.store.StoreRequest;
import com.henheang.hphsar.service.SupplierStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Tag(name = "Supplier Store Controller")
@RequestMapping("${base.supplier.v1}/stores")
@SecurityRequirement(name = "bearerAuth")
public class SupplierStoreController extends BaseController {

    private final SupplierStoreService supplierStoreService;

    public SupplierStoreController(SupplierStoreService supplierStoreService) {
        this.supplierStoreService = supplierStoreService;
    }
    @Operation(summary = "Setup new store")
    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
       return created(
                "Store created.",
                supplierStoreService.createNewStore(storeRequest,currentUserId)
        );
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserStore() throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Fetched store detail.",
                supplierStoreService.getUserStore(currentUserId)
        );
    }

    @PutMapping
    public ResponseEntity<?> editAllFieldUserStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store updated.",
                supplierStoreService.editAllFieldUserStore(currentUserId,storeRequest)
        );
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store deleted.",
                supplierStoreService.deleteUserStore(currentUserId)
        );
    }

    @PutMapping("/disable")
    public ResponseEntity<?> disableStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store is now inactive.",
                supplierStoreService.disableStore(currentUserId)
        );
    }

    @PutMapping("/enable")
    public ResponseEntity<?> enableStore() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok(
                "Store is now active.",
                supplierStoreService.enableStore(currentUserId)
        );
    }
}
