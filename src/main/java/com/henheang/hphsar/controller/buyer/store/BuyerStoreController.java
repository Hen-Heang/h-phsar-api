package com.henheang.hphsar.controller.buyer.store;
import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.common.api.PagedResponse;
import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.rating.StoreRating;
import com.henheang.hphsar.model.rating.StoreRatingRequest;
import com.henheang.hphsar.model.store.StoreBuyer;
import com.henheang.hphsar.service.BuyerStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Buyer Store Controller")
@RequestMapping("${base.buyer.v1}/stores")
@SecurityRequirement(name = "bearerAuth")
public class BuyerStoreController extends BaseController {
    private final BuyerStoreService buyerStoreService;

    @Operation(summary = "Get all store")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreBuyer>>> getAllStore() throws ParseException {
        return ok(Code.FETCHED, buyerStoreService.getAllStore());
    }

    @Operation(summary = "Get all store sort by date")
    @GetMapping("/sort/date")
    public ResponseEntity<PagedResponse<StoreBuyer>> getAllUserStoreSortByDate(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<StoreBuyer> stores = buyerStoreService.getAllUserStoreSortByDate(sort, pageNumber, pageSize);
        return okPage(Code.FETCHED, stores, pageNumber, pageSize, buyerStoreService.getTotalStore());
    }

    @Operation(summary = "Get all store sort by favorite")
    @GetMapping("/sort/favorite")
    public ResponseEntity<PagedResponse<StoreBuyer>> getAllUserStoreSortByCurrentUserFavorite(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<StoreBuyer> stores = buyerStoreService.getAllUserStoreSortByCurrentUserFavorite(pageNumber, pageSize);
        return okPage(Code.FETCHED, stores, pageNumber, pageSize, buyerStoreService.getTotalStore());
    }

    // get only favorite
    @Operation(summary = "Get only bookmarked store")
    @GetMapping("/bookmark")
    public ResponseEntity<PagedResponse<StoreBuyer>> getAllBookmarkedStore(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<StoreBuyer> stores = buyerStoreService.getAllBookmarkedStore(pageNumber, pageSize);
        return okPage(Code.FETCHED, stores, pageNumber, pageSize, buyerStoreService.getTotalBookmarkedStores());
    }

    // search by store name
    @Operation(summary = "Search by store name")
    @GetMapping("/name/search")
    public ResponseEntity<PagedResponse<StoreBuyer>> searchStoreByName(@RequestParam Integer pageNumber, @RequestParam Integer pageSize, @RequestParam String name) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<StoreBuyer> stores = buyerStoreService.searchStoreByName(pageNumber, pageSize, name);
        return okPage(Code.FETCHED, stores, pageNumber, pageSize, buyerStoreService.getTotalStore());
    }

    @Operation(summary = "Get all store order by rate")
    @GetMapping("/sort/rated")
    public ResponseEntity<PagedResponse<StoreBuyer>> getAllUserStoreSortByRatedStar(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<StoreBuyer> stores = buyerStoreService.getAllUserStoreSortByRatedStar(sort, pageNumber, pageSize);
        return okPage(Code.FETCHED, stores, pageNumber, pageSize, buyerStoreService.getTotalRatedStores());
    }

    @Operation(summary = "Get all store order by name")
    @GetMapping("/sort/name")
    public ResponseEntity<PagedResponse<StoreBuyer>> getAllUserStoreSortByName(@RequestParam String sort, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<StoreBuyer> stores = buyerStoreService.getAllUserStoreSortByName(sort, pageNumber, pageSize);
        return okPage(Code.FETCHED, stores, pageNumber, pageSize, buyerStoreService.getTotalStore());
    }

    @Operation(summary = "Get one store by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreBuyer>> getStoreById(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(Code.FETCHED, buyerStoreService.getStoreById(id));
    }

    @Operation(summary = "Bookmark a store")
    @PostMapping("/{storeId}/bookmark")
    public ResponseEntity<ApiResponse<String>> bookmarkStoreById(@PathVariable Integer storeId) {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return created("Bookmark successfully.", buyerStoreService.bookmarkStoreById(storeId));
    }

    @Operation(summary = "Remove bookmark")
    @DeleteMapping("/{storeId}/bookmark/remove")
    public ResponseEntity<ApiResponse<String>> removeBookmarkStoreById(@PathVariable Integer storeId) {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return ok("Bookmark removed successfully.", buyerStoreService.removeBookmarkStoreById(storeId));
    }

    @Operation(summary = "rating a store")
    @PostMapping("/{storeId}/rating")
    public ResponseEntity<ApiResponse<StoreRating>> ratingStoreById(@PathVariable Integer storeId, @RequestBody StoreRatingRequest ratingRequest) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(storeId, ratingRequest.getRatedStar());
        return created("Rated store successfully.", buyerStoreService.ratingStoreById(storeId, ratingRequest));
    }

    @Operation(summary = "get store rating details by store id")
    @GetMapping("/{storeId}/rating")
    public ResponseEntity<ApiResponse<StoreRating>> getRatingByStoreId(@PathVariable Integer storeId) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return ok("Rating fetched.", buyerStoreService.getRatingByStoreId(storeId));
    }

    @Operation(summary = "edit store rating")
    @PutMapping("/{storeId}/rating")
    public ResponseEntity<ApiResponse<StoreRating>> editRatingByStoreId(@PathVariable Integer storeId, @RequestBody StoreRatingRequest ratingRequest) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(storeId, ratingRequest.getRatedStar());
        return ok("Bookmark updated.", buyerStoreService.editRatingByStoreId(storeId, ratingRequest));
    }

    @Operation(summary = "delete store rating")
    @DeleteMapping("/{storeId}/rating")
    public ResponseEntity<ApiResponse<String>> deleteRatingByStoreId(@PathVariable Integer storeId) {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return ok("Rating removed successfully.", buyerStoreService.deleteRatingByStoreId(storeId));
    }

    @Operation(summary = "get store products")
    @GetMapping("/{storeId}/products")
    public ResponseEntity<ApiResponse<List<Product>>> getProductListingByStoreId(@PathVariable Integer storeId, @RequestParam String sort, @RequestParam String by) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return ok("Fetched products.", buyerStoreService.getProductListingByStoreId(storeId, sort, by));
    }

    @Operation(summary = "get store category")
    @GetMapping("/{storeId}/category")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoryListingByStoreId(@PathVariable Integer storeId) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(storeId);
        return ok("Fetched Category.", buyerStoreService.getCategoryListingByStoreId(storeId));
    }

    @Operation(summary = "get product by category")
    @GetMapping("/{storeId}/products/category")
    public ResponseEntity<ApiResponse<List<Product>>> getStoreProductByCategory(@PathVariable Integer storeId, @RequestParam Integer categoryId) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(storeId, categoryId);
        return ok("Fetched Products.", buyerStoreService.getProductListingByStoreIdAndCategoryId(storeId, categoryId));
    }

    @Operation(summary = "Search category for store")
    @GetMapping("/category/search")
    public ResponseEntity<ApiResponse<List<StoreBuyer>>> getStoresByCategorySearch(@RequestParam String name, @RequestParam String sort, @RequestParam String by) throws ParseException {
        return ok(Code.FETCHED, buyerStoreService.getStoresByCategorySearch(name, sort, by));
    }

    @Operation(summary = "Search product, category, store for store. Priority product > category > store name")
    @GetMapping("/hybrid/search")
    public ResponseEntity<ApiResponse<List<StoreBuyer>>> getStoresBySearch(@RequestParam String name, @RequestParam String sort, @RequestParam String by) throws ParseException {
        return ok(Code.FETCHED, buyerStoreService.getStoresBySearch(name, sort, by));
    }
}