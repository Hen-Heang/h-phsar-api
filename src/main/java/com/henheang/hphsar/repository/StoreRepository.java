package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.rating.StoreRating;
import com.henheang.hphsar.model.rating.StoreRatingRequest;
import com.henheang.hphsar.model.store.Store;
import com.henheang.hphsar.model.store.StoreRequest;
import com.henheang.hphsar.model.store.StoreBuyer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreRepository {

    Store createNewStore(@Param("store") StoreRequest storeRequest, Integer currentUserId);

    // tb_store does not have rating. this method is for calculating rating if it does not have rating return 0
    Double getRating(Integer id);

    Integer getRatingCount(Integer storeId);

    List<Integer> getBookmarkStoreId(Integer currentUserId);

    Integer checkStoreIfCreated(Integer currentUserId);

    Store getUserStore(Integer currentUserId);

    List<StoreBuyer> getAllStore();

    List<String> getAdditionalPhone(Integer storeId);

    Store editAllFieldUserStore(Integer storeId, @Param("store") StoreRequest storeRequest);

    Integer getStoreIdByUserId(Integer currentUserId);

    Boolean checkDuplicateStoreName(String name);

    String deleteUserStore(Integer currentUserId);

    String disableStore(Integer currentUserId);

    String enableStore(Integer currentUserId);

    Boolean checkIfStoreExist(Integer storeId);

    StoreBuyer getStoreById(Integer id);

    String bookmarkStoreById(Integer storeId, Integer currentUser);

    String removeBookmarkStoreById(Integer storeId, Integer currentUser);

    Boolean checkAlreadyBookmarked(Integer storeId, Integer currentUserId);

    Boolean checkAlreadyRated(Integer storeId, Integer currentUserId);

    StoreRating ratingStoreById(Integer storeId, Integer currentUser, @Param("request") StoreRatingRequest storeRatingRequest);

    StoreRating getRatingByStoreId(Integer storeId, Integer currentUser);

    StoreRating editRatingByStoreId(Integer storeId, Integer currentUser, @Param("request") StoreRatingRequest ratingRequest);

    String deleteRatingByStoreId(Integer storeId, Integer currentUser);

    List<Product> getProductListingByStoreIdASC(Integer storeId, String by);

    List<Product> getProductListingByStoreIdDESC(Integer storeId, String by);

    List<Product> getProductListingByStoreIdAndCategoryId(Integer storeId, Integer categoryId);

    Category getCategoryByCategoryId(Integer id);

    List<StoreBuyer> getAllUserStoreSortByDateASC(Integer pageNumber, Integer pageSize);

    List<StoreBuyer> getAllUserStoreSortByDateDESC(Integer pageNumber, Integer pageSize);

    Integer getTotalStores();

    Integer getTotalRatedStores(Integer buyerId);

    List<StoreBuyer> getAllUserStoreSortByCurrentUserFavoriteDESC(Integer pageNumber, Integer pageSize, Integer currentUser);

    List<StoreBuyer> getAllBookmarkedStore(Integer pageNumber, Integer pageSize, Integer currentUser);

    List<StoreBuyer> searchStoreByName(Integer pageNumber, Integer pageSize, String name);

    List<StoreBuyer> getAllUserStoreSortByRatedStarASC(Integer pageNumber, Integer pageSize);

    List<StoreBuyer> getAllUserStoreSortByRatedStarDESC(Integer pageNumber, Integer pageSize);

    List<StoreBuyer> getAllUserStoreSortByNameASC(Integer pageNumber, Integer pageSize);

    List<StoreBuyer> getAllUserStoreSortByNameDESC(Integer pageNumber, Integer pageSize);

    Integer getStoreIdByProductId(Integer productId);

    boolean checkIfStoreIsDisable(Integer currentUserId);

    void addAdditionalPhone(String phone, Integer id);

    void deleteAdditionalPhone(Integer storeId);

    Integer getTotalBookmarkedStores(Integer buyerId);

    String getStoreImageByStoreId(Integer id);

    String getStoreNameById(Integer id);

    boolean checkDuplicatePhone(String primaryPhone);

    List<Category> getCategoryListingByStoreId(Integer storeId);

    boolean checkIfCategoryExistInStore(Integer storeId, Integer categoryId);

    String getStoreImageById(Integer id);

    List<Integer> checkStock(Integer orderId);

    List<StoreBuyer> getStoresByCategorySearchASC(String name, String by);

    List<StoreBuyer> getStoresByCategorySearchDESC(String name, String by);

    List<Integer> getStoreIdsByCategorySearchASC(String name, String by);

    List<Integer> getStoreIdsByCategorySearchDESC(String name, String by);

    List<StoreBuyer> getStoresByProductSearchASC(String name, String by);

    List<StoreBuyer> getStoresByProductSearchDESC(String name, String by);

    List<Integer> getStoreIdByProductSearchASC(String name, String by);

    List<Integer> getStoreIdByProductSearchDESC(String name, String by);

    List<StoreBuyer> getStoresByNameSearchASC(String name, String by);

    List<StoreBuyer> getStoresByNameSearchDESC(String name, String by);

    List<Integer> getStoresIdByNameSearchASC(String name, String by);

    List<Integer> getStoresIdByNameSearchDESC(String name, String by);

    String getStoreAddressById(Integer id);

    String getStorePrimaryPhoneById(Integer id);

    String getStoreEmailByStoreId(Integer id);

    List<StoreBuyer> getStoresByStoreIdsASC(String combinedList);

    List<StoreBuyer> getStoresByStoreIdsDESC(String combinedList);

    Integer getStoreIdByDraftId(Integer id);
}