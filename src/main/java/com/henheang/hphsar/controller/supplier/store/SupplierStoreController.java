package com.henheang.hphsar.controller.supplier.store;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.store.StoreRequest;
import com.henheang.hphsar.service.SupplierStoreService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@Tag(name = "Supplier Store Controller")
@RequestMapping("${base.supplier.v1}/stores")
@SecurityRequirement(name = "bearerAuth")
public class SupplierStoreController extends BaseController {

    private final SupplierStoreService supplierStoreService;
    private final CurrentUserProvider currentUserProvider;
    @Operation(summary = "Setup new store")
    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
       return created(
                "Store created.",
                supplierStoreService.createNewStore(storeRequest,currentUserId)
        );
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserStore() throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(
                "Fetched store detail.",
                supplierStoreService.getUserStore(currentUserId)
        );
    }

    @PutMapping
    public ResponseEntity<?> editAllFieldUserStore(@RequestBody StoreRequest storeRequest) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(
                "Store updated.",
                supplierStoreService.editAllFieldUserStore(currentUserId,storeRequest)
        );
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserStore() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(
                "Store deleted.",
                supplierStoreService.deleteUserStore(currentUserId)
        );
    }

    @PutMapping("/disable")
    public ResponseEntity<?> disableStore() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(
                "Store is now inactive.",
                supplierStoreService.disableStore(currentUserId)
        );
    }

    @PutMapping("/enable")
    public ResponseEntity<?> enableStore() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(
                "Store is now active.",
                supplierStoreService.enableStore(currentUserId)
        );
    }
}
