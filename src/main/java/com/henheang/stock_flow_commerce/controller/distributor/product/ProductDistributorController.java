package com.henheang.stock_flow_commerce.controller.distributor.product;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.product.ProductEditRequest;
import com.henheang.stock_flow_commerce.model.product.ProductImport;
import com.henheang.stock_flow_commerce.model.product.ProductRequest;
import com.henheang.stock_flow_commerce.service.ProductDistributorService;
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
@Tag(name = "Distributor Product Controller")
@RequestMapping("${base.distributor.v1}products")
@SecurityRequirement(name = "bearer")
public class ProductDistributorController {

    private final ProductDistributorService productDistributorService;

    public ProductDistributorController(ProductDistributorService productDistributorService) {
        this.productDistributorService = productDistributorService;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;

    @PostMapping("")
    @Operation(summary = "Add new product")
    public ResponseEntity<?> insertNewProduct(@RequestBody ArrayList<ProductRequest> productRequests) throws ParseException {
        for (ProductRequest productRequest : productRequests){
            if (productRequest.getQty() > 2147483646 || productRequest.getCategoryId() > 2147483646 || productRequest.getPrice() > 2147483646){
                throw new BadRequestException("Number value can not exceed 2147483646");
            }
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("Insert product successfully.")
                .data(productDistributorService.insertNewProduct(currentUserId, productRequests))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
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
                .data(productDistributorService.getProductById(id))
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
                .data(productDistributorService.editProduct(id, productRequest))
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
                .data(productDistributorService.deleteProductById(id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/")
//    @Operation(summary = "Get all product from smaller to bigger qty ")
//    public ResponseEntity<?> getAllProductByQty() throws ParseException {
//        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Integer currentUserId= appUser.getId();
//        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
//                .status(HttpStatus.OK.value())
//                .message("fetched successfully")
//                .data(productDistributorService.getAllProductByQty(currentUserId))
//                .date(formatter.format(date = new Date()))
//                .build();
//        return ResponseEntity.ok(response);
//    }



    @Operation(summary = "search product by name")
    @GetMapping("/search")
    public ResponseEntity<?> getAllProductByName(@RequestParam String name) throws ParseException {
        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId= appUser.getId();
        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("fetched successfully")
                .data(productDistributorService.getAllProductByName(currentUserId,name))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

//    @GetMapping("products/unit price")
//    @Operation(summary = "Get all product by unit price DESC")
//    public ResponseEntity<?> getAllProductByUnitPrice() throws ParseException {
//        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Integer currentUserId= appUser.getId();
//        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
//                .status(HttpStatus.OK.value())
//                .message("fetched successfully")
//                .data(productDistributorService.getAllProductByUnitPrice(currentUserId))
//                .date(formatter.format(date = new Date()))
//                .build();
//        return ResponseEntity.ok(response);
//
//    }

//    @PutMapping("products/import")
//    @Operation(summary = "Import products")
//    public ResponseEntity<?> importProduct() throws ParseException {
//        AppUser appUser= (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Integer currentUserId= appUser.getId();
//        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
//                .status(HttpStatus.OK.value())
//                .message("imported successfully")
//                .data(productDistributorService.importProduct(currentUserId))
//                .date(formatter.format(date = new Date()))
//                .build();
//        return ResponseEntity.ok(response);
//    }

    @Operation(summary = "unpublish product")
    @PutMapping("/{id}/unlist")
    public ResponseEntity<?> unPublishProduct(@PathVariable Integer id) {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully unlisted.")
                .data(productDistributorService.unPublishProduct(id))
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
                .data(productDistributorService.publishProduct(id))
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
        PaginationApiResponse<List<Product>> response = PaginationApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(productDistributorService.getAllProductBySorting(sort, by, pageNumber, pageSize))
                .totalPage(productDistributorService.getTotalPage(pageSize))
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
                .data(productDistributorService.importProduct(productsImport))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
