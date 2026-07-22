package com.henheang.hphsar.controller.supplier.product;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.model.ApiResponse;
import com.henheang.hphsar.model.PaginationApiResponse;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductEditRequest;
import com.henheang.hphsar.model.product.ProductImport;
import com.henheang.hphsar.model.product.ProductRequest;
import com.henheang.hphsar.service.SupplierProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "Supplier Product Controller")
@RequestMapping("${base.supplier.v1}/products")
@SecurityRequirement(name = "bearerAuth")
public class SupplierProductController  extends BaseController {

    private final SupplierProductService supplierProductService;

    public SupplierProductController(SupplierProductService supplierProductService) {
        this.supplierProductService = supplierProductService;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;

    @PostMapping
    @Operation(summary = "Add new product")
    public ResponseEntity<?> insertNewProduct(@RequestBody ArrayList<ProductRequest> productRequests) throws ParseException {
        for (ProductRequest productRequest : productRequests){
            if (productRequest.getQty() > 2147483646 || productRequest.getCategoryId() > 2147483646 || productRequest.getPrice() > 2147483646){
                throw new BadRequestException("Number value can not exceed 2147483646");
            }
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return created("Product added successfully.", supplierProductService.insertNewProduct(currentUserId, productRequests)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<Product> response = ApiResponse.<Product>builder()
                .status(HttpStatus.OK.value())
                .message("fetched successfully")
                .data(supplierProductService.getProductById(id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "edit product by id")
    public ResponseEntity<?> editProduct(@PathVariable Integer id, @RequestBody ProductEditRequest productRequest) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<Product> response = ApiResponse.<Product>builder()
                .status(HttpStatus.OK.value())
                .message("edited successfully")
                .data(supplierProductService.editProduct(id, productRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "delete product by id")
    public ResponseEntity<?> deleteProductById(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Product deleted successfully.")
                .data(supplierProductService.deleteProductById(id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "search product by name")
    @GetMapping("/search")
    public ResponseEntity<?> getAllProductByName(@RequestParam String name) throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("fetched successfully")
                .data(supplierProductService.getAllProductByName(currentUserId,name))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "unpublish product")
    @PutMapping("/{id}/unlist")
    public ResponseEntity<?> unPublishProduct(@PathVariable Integer id) {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully unlisted.")
                .data(supplierProductService.unPublishProduct(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "publish product")
    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publishProduct(@PathVariable Integer id) {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully published.")
                .data(supplierProductService.publishProduct(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all product with sorting")
    @GetMapping("/sort")
    public ResponseEntity<?> getAllProductWithSorting(@RequestParam(defaultValue = "asc") String sort,@RequestParam(defaultValue = "qty") String by, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        Integer totalElements = supplierProductService.getTotalElements();
        PaginationApiResponse<List<Product>> response = PaginationApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(supplierProductService.getAllProductBySorting(sort, by, pageNumber, pageSize))
                .totalPage(supplierProductService.getTotalPage(pageSize))
                .totalElements(totalElements)
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Import or re-stock")
    @PostMapping("/import")
    public ResponseEntity<?> importProduct(@RequestBody List<ProductImport> productsImport) throws ParseException {
        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
                .status(HttpStatus.CREATED.value())
                .message("Import product.")
                .data(supplierProductService.importProduct(productsImport))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
