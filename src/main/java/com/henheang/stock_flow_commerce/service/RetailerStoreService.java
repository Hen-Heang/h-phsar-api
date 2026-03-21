package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.rating.StoreRating;
import com.henheang.stock_flow_commerce.model.rating.StoreRatingRequest;
import com.henheang.stock_flow_commerce.model.store.StoreRetailer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

public interface RetailerStoreService {
    List<StoreRetailer> getAllStore() throws ParseException;

    StoreRetailer getStoreById(Integer id) throws ParseException;

    String bookmarkStoreById(Integer storeId);

    StoreRating ratingStoreById(Integer storeId, StoreRatingRequest storeRatingRequest) throws ParseException;

    String removeBookmarkStoreById(Integer storeId);

    StoreRating getRatingByStoreId(Integer storeId) throws ParseException;

    StoreRating editRatingByStoreId(Integer storeId, StoreRatingRequest ratingRequest) throws ParseException;

    String deleteRatingByStoreId(Integer storeId);

    List<Product> getProductListingByStoreId(Integer storeId, String sort, String by) throws ParseException;

    List<StoreRetailer> getAllUserStoreSortByDate(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    Integer findTotalPage(Integer totalStore, Integer pageSize);

    Integer getTotalStore();

    Integer getTotalRatedStores();

    Integer getTotalBookmarkedStores();

    List<StoreRetailer> getAllUserStoreSortByCurrentUserFavorite(Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreRetailer> getAllUserStoreSortByRatedStar(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreRetailer> getAllUserStoreSortByName(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreRetailer> getAllBookmarkedStore(Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreRetailer> searchStoreByName(Integer pageNumber, Integer pageSize, String name) throws ParseException;

    List<Category> getCategoryListingByStoreId(Integer storeId) throws ParseException;

    List<Product> getProductListingByStoreIdAndCategoryId(Integer storeId, Integer categoryId) throws ParseException;

    List<StoreRetailer> getStoresByCategorySearch(String name, String sort, String by) throws ParseException;

    List<StoreRetailer> getStoresBySearch(String name, String sort, String by) throws ParseException;
}
