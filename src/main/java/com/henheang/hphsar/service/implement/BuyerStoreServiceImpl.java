package com.henheang.hphsar.service.implement;
import com.henheang.hphsar.common.ExceptionMessages;

import com.henheang.hphsar.exception.*;
import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.rating.StoreRating;
import com.henheang.hphsar.model.rating.StoreRatingRequest;
import com.henheang.hphsar.model.store.StoreBuyer;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.BuyerStoreService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import com.henheang.hphsar.utils.DateTimeUtil;
import com.henheang.hphsar.utils.PaginationUtils;
import com.henheang.hphsar.utils.SortDirectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BuyerStoreServiceImpl implements BuyerStoreService {
    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    Boolean checkIfStoreExist(Integer storeId) {
        return storeRepository.checkIfStoreExist(storeId);
    }

    Boolean checkAlreadyBookmarked(Integer storeId, Integer currentUserId) {
        return storeRepository.checkAlreadyBookmarked(storeId, currentUserId);
    }

    Boolean checkAlreadyRated(Integer storeId, Integer currentUserId) {
        return storeRepository.checkAlreadyRated(storeId, currentUserId);
    }

    @Override
    public List<StoreBuyer> getAllStore() throws ParseException {
        Integer currentUser = currentUserProvider.getCurrentUserId();
        List<StoreBuyer> allStores = storeRepository.getAllStore();
        if (allStores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        // get bookmark field
        for (StoreBuyer store : allStores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return allStores;
    }

    @Override
    public StoreBuyer getStoreById(Integer id) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(id)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }

        // get store
        StoreBuyer store = storeRepository.getStoreById(id);

        // if get fail return internal error
        if (store == null) {
            throw new InternalServerErrorException("Fail to fetch store.");
        }
        // if store is disable do not show
        if (!store.getIsPublish()) {
            throw new NotFoundException("Store is disabled.");
        }
        // get bookmark field
        Integer currentUser = currentUserProvider.getCurrentUserId();
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (Integer integer : bookmarkStoreId) {
            store.setIsBookmarked(Objects.equals(integer, store.getId()));
        }
        store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
        store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
        for (Category category : store.getCategories()) {
            category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
            category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
        }
        return store;
    }

    @Override
    public String bookmarkStoreById(Integer storeId) {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store storeId does not exist.");
        }
        // get current user storeId
        Integer currentUser = currentUserProvider.getCurrentUserId();

        // check if already bookmarked throw
        if (checkAlreadyBookmarked(storeId, currentUser)) {
            throw new ConflictException("Already Bookmarked");
        }

        // get store and bookmark
        String confirm = storeRepository.bookmarkStoreById(storeId, currentUser);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException(ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DOING_BOOKMARK_OPERATION);
        }
        return "Successfully bookmark.";
    }

    @Override
    public String removeBookmarkStoreById(Integer storeId) {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }
        // get current user id
        Integer currentUser = currentUserProvider.getCurrentUserId();

        // check if not yet bookmarked throw
        if (!(checkAlreadyBookmarked(storeId, currentUser))) {
            throw new ConflictException("No bookmark found.");
        }

        // get store and bookmark
        String confirm = storeRepository.removeBookmarkStoreById(storeId, currentUser);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException(ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DOING_BOOKMARK_OPERATION);
        }
        return "Successfully remove bookmark.";
    }

    @Override
    public StoreRating getRatingByStoreId(Integer storeId) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }

        Integer currentUser = currentUserProvider.getCurrentUserId();

        StoreRating storeRating = storeRepository.getRatingByStoreId(storeId, currentUser);
        if (storeRating == null) {
            throw new NotFoundException("No rating found.");
        }
        storeRating.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(storeRating.getCreatedDate())));
        storeRating.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(storeRating.getUpdatedDate())));
        return storeRating;
    }

    @Override
    public StoreRating editRatingByStoreId(Integer storeId, StoreRatingRequest ratingRequest) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }
        if (!(ratingRequest.getRatedStar() > 0 && ratingRequest.getRatedStar() < 6)) {
            throw new BadRequestException(ExceptionMessages.OUT_OF_RANGE_RATING_RANGE_IS_FROM_1);
        }
        Integer currentUser = currentUserProvider.getCurrentUserId();
        // check if already rated
        if (checkAlreadyRated(storeId, currentUser).equals(false)) {
            throw new ConflictException("Rating not found. Can't edit rating without rating.");
        }

        // edit and return value
        StoreRating storeRating = storeRepository.editRatingByStoreId(storeId, currentUser, ratingRequest);
        if (storeRating == null) {
            throw new InternalServerErrorException("Error while editing rating.");
        }
        if (storeRating.getCreatedDate() != null)
            storeRating.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(storeRating.getCreatedDate())));
        return storeRating;
    }

    @Override
    public String deleteRatingByStoreId(Integer storeId) {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }
        Integer currentUser = currentUserProvider.getCurrentUserId();
        // check if already rated
        if (checkAlreadyRated(storeId, currentUser).equals(false)) {
            throw new ConflictException("Rating not found.");
        }
        String confirm = storeRepository.deleteRatingByStoreId(storeId, currentUser);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Delete rating failed.");
        }
        return "Delete rating successfully. Rating deleted permanently.";
    }

    @Override
    public StoreRating ratingStoreById(Integer storeId, StoreRatingRequest storeRatingRequest) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }

        if (!(storeRatingRequest.getRatedStar() > 0 && storeRatingRequest.getRatedStar() < 6)) {
            throw new BadRequestException(ExceptionMessages.OUT_OF_RANGE_RATING_RANGE_IS_FROM_1);
        }
        Integer currentUser = currentUserProvider.getCurrentUserId();

        // check if already rated
        if (checkAlreadyRated(storeId, currentUser)) {
            throw new ConflictException("Already rated.");
        }
        // rate and get rate object
        StoreRating storeRating = storeRepository.ratingStoreById(storeId, currentUser, storeRatingRequest);
        if (storeRating == null) {
            throw new InternalServerErrorException("Error while in rating operation.");
        }
        if (storeRating.getCreatedDate() != null)
            storeRating.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(storeRating.getCreatedDate())));
        return storeRating;
    }

    @Override
    public List<Product> getProductListingByStoreId(Integer storeId, String sort, String by) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }
        Map<String, String> validColumns = Map.of(
                "name", "tp.name",
                "qty", "td.qty",
                "price", "td.price",
                "created_date", "td.created_date"
        );
        if (!validColumns.containsKey(by)) {
            throw new BadRequestException("Available sort are: 'name', 'qty', 'price', and 'created_date'.");
        }
        String qualifiedBy = validColumns.get(by);
        List<Product> products = null;
        if (sort.equalsIgnoreCase("asc")) {
            products = storeRepository.getProductListingByStoreIdASC(storeId, qualifiedBy);
        } else if (sort.equalsIgnoreCase("desc")) {
            products = storeRepository.getProductListingByStoreIdDESC(storeId, qualifiedBy);
        }
        assert products != null;
        if (products.isEmpty()) {
            throw new OKException(ExceptionMessages.PRODUCTS_NOT_FOUND);
        }
        return getProducts(products);
    }

    static List<Product> getProducts(List<Product> products) throws ParseException {
        for (Product product : products) {
            if (product.getCreatedDate() != null)
                product.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCreatedDate())));
            if (product.getUpdatedDate() != null)
                product.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getUpdatedDate())));
            if (product.getCategory() != null) {
                if (product.getCategory().getCreatedDate() != null)
                    product.getCategory().setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCategory().getCreatedDate())));
                if (product.getCategory().getUpdatedDate() != null)
                    product.getCategory().setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCategory().getUpdatedDate())));
            }
        }
        return products;
    }

    @Override
    public Integer getTotalStore() {
        return storeRepository.getTotalStores();
    }

    @Override
    public Integer getTotalRatedStores() {
        Integer buyerId = currentUserProvider.getCurrentUserId();
        return storeRepository.getTotalRatedStores(buyerId);
    }

    @Override
    public Integer getTotalBookmarkedStores() {
        Integer buyerId = currentUserProvider.getCurrentUserId();
        return storeRepository.getTotalBookmarkedStores(buyerId);
    }

    @Override
    public Integer findTotalPage(Integer totalStore, Integer pageSize) {
        return PaginationUtils.totalPages(totalStore, pageSize);
    }

    @Override
    public List<StoreBuyer> getAllUserStoreSortByDate(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        Integer currentUser = currentUserProvider.getCurrentUserId();
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch stores
        List<StoreBuyer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getAllUserStoreSortByDateASC(pageNumber, pageSize);
        } else {
            stores = storeRepository.getAllUserStoreSortByDateDESC(pageNumber, pageSize);
        }
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreBuyer> getAllUserStoreSortByCurrentUserFavorite(Integer pageNumber, Integer pageSize) throws ParseException {
        Integer currentUser = currentUserProvider.getCurrentUserId();
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        List<StoreBuyer> stores = storeRepository.getAllUserStoreSortByCurrentUserFavoriteDESC(pageNumber, pageSize, currentUser);
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreBuyer> getAllBookmarkedStore(Integer pageNumber, Integer pageSize) throws ParseException {
        Integer currentUser = currentUserProvider.getCurrentUserId();
        if (getTotalBookmarkedStores() == 0) {
            throw new NotFoundException("No bookmarked store found.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        List<StoreBuyer> stores = storeRepository.getAllBookmarkedStore(pageNumber, pageSize, currentUser);
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreBuyer> searchStoreByName(Integer pageNumber, Integer pageSize, String name) throws ParseException {
        Integer currentUser = currentUserProvider.getCurrentUserId();
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        List<StoreBuyer> stores = storeRepository.searchStoreByName(pageNumber, pageSize, name);
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<Category> getCategoryListingByStoreId(Integer storeId) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }
        List<Category> categories = storeRepository.getCategoryListingByStoreId(storeId);
        if (categories.isEmpty()) {
            throw new OKException(ExceptionMessages.PRODUCTS_NOT_FOUND);
        }
        for (Category category : categories) {
            if (category.getCreatedDate() != null)
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
            if (category.getUpdatedDate() != null)
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
        }
        return categories;
    }

    @Override
    public List<Product> getProductListingByStoreIdAndCategoryId(Integer storeId, Integer categoryId) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException(ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST);
        }
        if (!storeRepository.checkIfCategoryExistInStore(storeId, categoryId)) {
            throw new NotFoundException("Category does not exist in this store.");
        }
        List<Product> products = storeRepository.getProductListingByStoreIdAndCategoryId(storeId, categoryId);
        if (products.isEmpty()) {
            throw new OKException("Product not Found. This category have no products.");
        }
        return getProducts(products);
    }

    @Override
    public List<StoreBuyer> getStoresByCategorySearch(String name, String sort, String by) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        by = by.toLowerCase().trim();
        if (!(by.equals("created_date") || by.equals("is_publish") || by.equals("name"))) {
            throw new BadRequestException(ExceptionMessages.INVALID_INPUT_AVAILABLE_SORTING_ARE_CREATED_DATE_IS);
        }
        List<StoreBuyer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getStoresByCategorySearchASC(name, by);
        } else {
            stores = storeRepository.getStoresByCategorySearchDESC(name, by);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        for (StoreBuyer store : stores) {
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreBuyer> getStoresBySearch(String name, String sort, String by) throws ParseException {
        Integer currentUser = currentUserProvider.getCurrentUserId();
        // check sort spelling
        SortDirectionUtils.validate(sort);
        by = by.toLowerCase().trim();
        if (!(by.equals("created_date") || by.equals("is_publish") || by.equals("name"))) {
            throw new BadRequestException(ExceptionMessages.INVALID_INPUT_AVAILABLE_SORTING_ARE_CREATED_DATE_IS);
        }
        // get store ids from product
        List<Integer> storesFromProduct;
        if (sort.toLowerCase().equals("asc")) {
            storesFromProduct = storeRepository.getStoreIdByProductSearchASC(name, by);
        } else {
            storesFromProduct = storeRepository.getStoreIdByProductSearchDESC(name, by);
        }

        // get store ids from category
        List<Integer> storesFromCategory;
        if (sort.toLowerCase().equals("asc")) {
            storesFromCategory = storeRepository.getStoreIdsByCategorySearchASC(name, by);
        } else {
            storesFromCategory = storeRepository.getStoreIdsByCategorySearchDESC(name, by);
        }

        // get store ids from name
        List<Integer> storesFromName;
        if (sort.toLowerCase().equals("asc")) {
            storesFromName = storeRepository.getStoresIdByNameSearchASC(name, by);
        } else {
            storesFromName = storeRepository.getStoresIdByNameSearchDESC(name, by);
        }
        List<Integer> combinedList = combineLists(storesFromProduct, storesFromCategory, storesFromName);

        List<StoreBuyer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getStoresByStoreIdsASC(combinedList.toString().replaceAll("\\[|\\]", ""));
        } else {
            stores = storeRepository.getStoresByStoreIdsDESC(combinedList.toString().replaceAll("\\[|\\]", ""));
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for_loop:
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreBuyer> getAllUserStoreSortByRatedStar(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch stores
        List<StoreBuyer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getAllUserStoreSortByRatedStarASC(pageNumber, pageSize);
        } else {
            stores = storeRepository.getAllUserStoreSortByRatedStarDESC(pageNumber, pageSize);
        }
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        Integer currentUser = currentUserProvider.getCurrentUserId();
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreBuyer> getAllUserStoreSortByName(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        // fetch stores
        List<StoreBuyer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getAllUserStoreSortByNameASC(pageNumber, pageSize);
        } else {
            stores = storeRepository.getAllUserStoreSortByNameDESC(pageNumber, pageSize);
        }
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.STORES_NOT_FOUND);
        }
        Integer currentUser = currentUserProvider.getCurrentUserId();
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreBuyer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
            store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getCreatedDate())));
                category.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    public static List<Integer> combineLists(List<Integer> list1, List<Integer> list2, List<Integer> list3) {
        // Create a new list to store the combined lists
        List<Integer> combinedList = new ArrayList<>();

        // Combine the three lists without duplicates
        combinedList.addAll(list1);
        combinedList.addAll(list2);
        combinedList.addAll(list3);

        // Remove duplicates from the combined list
        Collections.sort(combinedList);
        combinedList = new ArrayList<>(new LinkedHashSet<>(combinedList));

        return combinedList;
    }

}


