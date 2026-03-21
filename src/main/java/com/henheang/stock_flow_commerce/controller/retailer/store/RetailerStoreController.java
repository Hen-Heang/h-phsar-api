package com.henheang.stock_flow_commerce.controller.retailer.store;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.rating.StoreRating;
import com.henheang.stock_flow_commerce.model.rating.StoreRatingRequest;
import com.henheang.stock_flow_commerce.model.store.StoreRetailer;
import com.henheang.stock_flow_commerce.service.RetailerStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "Retailer Store Controller")
@RequestMapping("${base.retailer.v1}stores")
@SecurityRequirement(name = "bearer")
public class RetailerStoreController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;
    private final RetailerStoreService retailerStoreService;

    public RetailerStoreController(RetailerStoreService retailerStoreService) {
        this.retailerStoreService = retailerStoreService;
    }

    @Operation(summary = "Get all store")
    @GetMapping("/")
    public ResponseEntity<?> getAllStore() throws ParseException {
        ApiResponse<List<StoreRetailer>> response = ApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getAllStore())
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all store sort by date")
    @GetMapping("/sort/date")
    public ResponseEntity<?> getAllUserStoreSortByDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<StoreRetailer>> response = PaginationApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getAllUserStoreSortByDate(sort, pageNumber, pageSize))
                .totalPage(retailerStoreService.findTotalPage(retailerStoreService.getTotalStore(), pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all store sort by favorite")
    @GetMapping("/sort/favorite")
    public ResponseEntity<?> getAllUserStoreSortByCurrentUserFavorite(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<StoreRetailer>> response = PaginationApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getAllUserStoreSortByCurrentUserFavorite(pageNumber, pageSize))
                .totalPage(retailerStoreService.findTotalPage(retailerStoreService.getTotalStore(), pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    // get only favorite
    @Operation(summary = "Get only bookmarked store")
    @GetMapping("/bookmark")
    public ResponseEntity<?> getAllBookmarkedStore(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<StoreRetailer>> response = PaginationApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getAllBookmarkedStore(pageNumber, pageSize))
                .totalPage(retailerStoreService.findTotalPage(retailerStoreService.getTotalBookmarkedStores(), pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    // search by store name
    @Operation(summary = "Search by store name")
    @GetMapping("/name/search")
    public ResponseEntity<?> searchStoreByName(@RequestParam Integer pageNumber, @RequestParam Integer pageSize, @RequestParam String name) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<StoreRetailer>> response = PaginationApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.searchStoreByName(pageNumber, pageSize, name))
                .totalPage(retailerStoreService.findTotalPage(retailerStoreService.getTotalStore(), pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all store order by rate")
    @GetMapping("/sort/rated")
    public ResponseEntity<?> getAllUserStoreSortByRatedStar(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<StoreRetailer>> response = PaginationApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getAllUserStoreSortByRatedStar(sort, pageNumber, pageSize))
                .totalPage(retailerStoreService.findTotalPage(retailerStoreService.getTotalRatedStores(), pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all store order by name")
    @GetMapping("/sort/name")
    public ResponseEntity<?> getAllUserStoreSortByName(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<StoreRetailer>> response = PaginationApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getAllUserStoreSortByName(sort, pageNumber, pageSize))
                .totalPage(retailerStoreService.findTotalPage(retailerStoreService.getTotalStore(), pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get one store by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStoreById(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<StoreRetailer> response = ApiResponse.<StoreRetailer>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getStoreById(id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bookmark a store")
    @PostMapping("/{storeId}/bookmark/")
    public ResponseEntity<?> bookmarkStoreById(@PathVariable Integer storeId) {
        if (storeId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.CREATED.value())
                .message("Bookmark successfully.")
                .data(retailerStoreService.bookmarkStoreById(storeId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remove bookmark")
    @DeleteMapping("/{storeId}/bookmark/remove")
    public ResponseEntity<?> removeBookmarkStoreById(@PathVariable Integer storeId) {
        if (storeId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Bookmark removed successfully.")
                .data(retailerStoreService.removeBookmarkStoreById(storeId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "rating a store")
    @PostMapping("/{storeId}/rating")
    public ResponseEntity<?> ratingStoreById(@PathVariable Integer storeId, @RequestBody StoreRatingRequest ratingRequest) throws ParseException {
        if (storeId > 2147483646 || ratingRequest.getRatedStar() > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<StoreRating> response = ApiResponse.<StoreRating>builder()
                .status(HttpStatus.CREATED.value())
                .message("Rated store successfully.")
                .data(retailerStoreService.ratingStoreById(storeId, ratingRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "get store rating details by store id")
    @GetMapping("/{storeId}/rating")
    public ResponseEntity<?> getRatingByStoreId(@PathVariable Integer storeId) throws ParseException {
        if (storeId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<StoreRating> response = ApiResponse.<StoreRating>builder()
                .status(HttpStatus.OK.value())
                .message("Rating fetched.")
                .data(retailerStoreService.getRatingByStoreId(storeId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "edit store rating")
    @PutMapping("/{storeId}/rating")
    public ResponseEntity<?> editRatingByStoreId(@PathVariable Integer storeId, @RequestBody StoreRatingRequest ratingRequest) throws ParseException {
        if (storeId > 2147483646 || ratingRequest.getRatedStar() > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<StoreRating> response = ApiResponse.<StoreRating>builder()
                .status(HttpStatus.OK.value())
                .message("Bookmark updated.")
                .data(retailerStoreService.editRatingByStoreId(storeId, ratingRequest))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "delete store rating")
    @DeleteMapping("/{storeId}/rating")
    public ResponseEntity<?> deleteRatingByStoreId(@PathVariable Integer storeId) {
        if (storeId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Rating removed successfully.")
                .data(retailerStoreService.deleteRatingByStoreId(storeId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "get store products")
    @GetMapping("/{storeId}/products")
    public ResponseEntity<?> getProductListingByStoreId(@PathVariable Integer storeId, @RequestParam String sort, @RequestParam String by) throws ParseException {
        if (storeId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched products.")
                .data(retailerStoreService.getProductListingByStoreId(storeId, sort, by))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "get store category")
    @GetMapping("/{storeId}/category")
    public ResponseEntity<?> getCategoryListingByStoreId(@PathVariable Integer storeId) throws ParseException {
        if (storeId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<List<Category>> response = ApiResponse.<List<Category>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched Category.")
                .data(retailerStoreService.getCategoryListingByStoreId(storeId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "get product by category")
    @GetMapping("/{storeId}/products/category")
    public ResponseEntity<?> getStoreProductByCategory(@PathVariable Integer storeId, @RequestParam Integer categoryId) throws ParseException {
        if (storeId > 2147483646 || categoryId > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<List<Product>> response = ApiResponse.<List<Product>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched Products.")
                .data(retailerStoreService.getProductListingByStoreIdAndCategoryId(storeId, categoryId))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search category for store")
    @GetMapping("/category/search")
    public ResponseEntity<?> getStoresByCategorySearch(@RequestParam String name, @RequestParam String sort, @RequestParam String by) throws ParseException {
        ApiResponse<List<StoreRetailer>> response = ApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getStoresByCategorySearch(name, sort, by))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search product, category, store for store. Priority product > category > store name")
    @GetMapping("/hybrid/search")
    public ResponseEntity<?> getStoresBySearch(@RequestParam String name, @RequestParam String sort, @RequestParam String by) throws ParseException {
        ApiResponse<List<StoreRetailer>> response = ApiResponse.<List<StoreRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetched successfully.")
                .data(retailerStoreService.getStoresBySearch(name, sort, by))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}