package com.henheang.hphsar.service;

import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.rating.StoreRating;
import com.henheang.hphsar.model.rating.StoreRatingRequest;
import com.henheang.hphsar.model.store.StoreBuyer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

public interface BuyerStoreService {
    List<StoreBuyer> getAllStore() throws ParseException;

    StoreBuyer getStoreById(Integer id) throws ParseException;

    String bookmarkStoreById(Integer storeId);

    StoreRating ratingStoreById(Integer storeId, StoreRatingRequest storeRatingRequest) throws ParseException;

    String removeBookmarkStoreById(Integer storeId);

    StoreRating getRatingByStoreId(Integer storeId) throws ParseException;

    StoreRating editRatingByStoreId(Integer storeId, StoreRatingRequest ratingRequest) throws ParseException;

    String deleteRatingByStoreId(Integer storeId);

    List<Product> getProductListingByStoreId(Integer storeId, String sort, String by) throws ParseException;

    List<StoreBuyer> getAllUserStoreSortByDate(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    Integer findTotalPage(Integer totalStore, Integer pageSize);

    Integer getTotalStore();

    Integer getTotalRatedStores();

    Integer getTotalBookmarkedStores();

    List<StoreBuyer> getAllUserStoreSortByCurrentUserFavorite(Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreBuyer> getAllUserStoreSortByRatedStar(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreBuyer> getAllUserStoreSortByName(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreBuyer> getAllBookmarkedStore(Integer pageNumber, Integer pageSize) throws ParseException;

    List<StoreBuyer> searchStoreByName(Integer pageNumber, Integer pageSize, String name) throws ParseException;

    List<Category> getCategoryListingByStoreId(Integer storeId) throws ParseException;

    List<Product> getProductListingByStoreIdAndCategoryId(Integer storeId, Integer categoryId) throws ParseException;

    List<StoreBuyer> getStoresByCategorySearch(String name, String sort, String by) throws ParseException;

    List<StoreBuyer> getStoresBySearch(String name, String sort, String by) throws ParseException;
}
