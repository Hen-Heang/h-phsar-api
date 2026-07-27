package com.henheang.hphsar.controller.supplier.product;
import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.common.api.PagedResponse;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductEditRequest;
import com.henheang.hphsar.model.product.ProductImport;
import com.henheang.hphsar.model.product.ProductRequest;
import com.henheang.hphsar.service.SupplierProductService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Supplier Product Controller")
@RequestMapping("${base.supplier.v1}/products")
@SecurityRequirement(name = "bearerAuth")
public class SupplierProductController  extends BaseController {

    private final SupplierProductService supplierProductService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @Operation(summary = "Add new product")
    public ResponseEntity<ApiResponse<List<Product>>> insertNewProduct(@RequestBody ArrayList<ProductRequest> productRequests) throws ParseException {
        for (ProductRequest productRequest : productRequests){
            if (productRequest.getQty() > 2147483646 || productRequest.getCategoryId() > 2147483646 || productRequest.getPrice() > 2147483646){
                throw new BadRequestException("Number value can not exceed 2147483646");
            }
        }
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return created("Product added successfully.", supplierProductService.insertNewProduct(currentUserId, productRequests)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(Code.FETCHED, supplierProductService.getProductById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "edit product by id")
    public ResponseEntity<ApiResponse<Product>> editProduct(@PathVariable Integer id, @RequestBody ProductEditRequest productRequest) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("edited successfully", supplierProductService.editProduct(id, productRequest));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "delete product by id")
    public ResponseEntity<ApiResponse<String>> deleteProductById(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Product deleted successfully.", supplierProductService.deleteProductById(id));
    }

    @Operation(summary = "search product by name")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProductByName(@RequestParam String name) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return ok(Code.FETCHED, supplierProductService.getAllProductByName(currentUserId,name));
    }


    @Operation(summary = "unpublish product")
    @PutMapping("/{id}/unlist")
    public ResponseEntity<ApiResponse<String>> unPublishProduct(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Successfully unlisted.", supplierProductService.unPublishProduct(id));
    }

    @Operation(summary = "publish product")
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<String>> publishProduct(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok("Successfully published.", supplierProductService.publishProduct(id));
    }

    @Operation(summary = "Get all product with sorting")
    @GetMapping("/sort")
    public ResponseEntity<PagedResponse<Product>> getAllProductWithSorting(@RequestParam(defaultValue = "asc") String sort,@RequestParam(defaultValue = "qty") String by, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        Integer totalElements = supplierProductService.getTotalElements();
        List<Product> products = supplierProductService.getAllProductBySorting(sort, by, pageNumber, pageSize);
        return okPage(Code.FETCHED, products, pageNumber, pageSize, totalElements);
    }

    @Operation(summary = "Import or re-stock")
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<List<Product>>> importProduct(@RequestBody List<ProductImport> productsImport) throws ParseException {
        return created("Import product.", supplierProductService.importProduct(productsImport));
    }
}
