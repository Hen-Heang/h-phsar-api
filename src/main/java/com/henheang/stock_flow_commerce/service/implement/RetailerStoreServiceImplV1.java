package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.*;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.rating.StoreRating;
import com.henheang.stock_flow_commerce.model.rating.StoreRatingRequest;
import com.henheang.stock_flow_commerce.model.store.StoreRetailer;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.RetailerStoreService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RetailerStoreServiceImplV1 implements RetailerStoreService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final StoreRepository storeRepository;

    public RetailerStoreServiceImplV1(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

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
    public List<StoreRetailer> getAllStore() throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        List<StoreRetailer> allStores = storeRepository.getAllStore();
        if (allStores.isEmpty()) {
            throw new NotFoundException("Stores not found");
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        // get bookmark field
        for (StoreRetailer store : allStores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return allStores;
    }

    @Override
    public StoreRetailer getStoreById(Integer id) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(id)) {
            throw new NotFoundException("This store id does not exist.");
        }

        // get store
        StoreRetailer store = storeRepository.getStoreById(id);

        // if get fail return internal error
        if (store == null) {
            throw new InternalServerErrorException("Fail to fetch store.");
        }
        // if store is disable do not show
        if (!store.getIsPublish()) {
            throw new NotFoundException("Store is disabled.");
        }
        // get bookmark field
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (Integer integer : bookmarkStoreId) {
            store.setIsBookmarked(Objects.equals(integer, store.getId()));
        }
        store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
        store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
        for (Category category : store.getCategories()) {
            category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
            category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
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
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();

        // check if already bookmarked throw
        if (checkAlreadyBookmarked(storeId, currentUser)) {
            throw new ConflictException("Already Bookmarked");
        }

        // get store and bookmark
        String confirm = storeRepository.bookmarkStoreById(storeId, currentUser);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Something went wrong while doing bookmark operation");
        }
        return "Successfully bookmark.";
    }

    @Override
    public String removeBookmarkStoreById(Integer storeId) {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }
        // get current user id
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();

        // check if not yet bookmarked throw
        if (!(checkAlreadyBookmarked(storeId, currentUser))) {
            throw new ConflictException("No bookmark found.");
        }

        // get store and bookmark
        String confirm = storeRepository.removeBookmarkStoreById(storeId, currentUser);
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Something went wrong while doing bookmark operation");
        }
        return "Successfully remove bookmark.";
    }

    @Override
    public StoreRating getRatingByStoreId(Integer storeId) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }

        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();

        StoreRating storeRating = storeRepository.getRatingByStoreId(storeId, currentUser);
        if (storeRating == null) {
            throw new NotFoundException("No rating found.");
        }
        storeRating.setCreatedDate(formatter.format(formatter.parse(storeRating.getCreatedDate())));
        storeRating.setUpdatedDate(formatter.format(formatter.parse(storeRating.getUpdatedDate())));
        return storeRating;
    }

    @Override
    public StoreRating editRatingByStoreId(Integer storeId, StoreRatingRequest ratingRequest) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }
        if (!(ratingRequest.getRatedStar() > 0 && ratingRequest.getRatedStar() < 6)) {
            throw new BadRequestException("Out of range. Rating range is from 1 to 5.");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        // check if already rated
        if (checkAlreadyRated(storeId, currentUser).equals(false)) {
            throw new ConflictException("Rating not found. Can't edit rating without rating.");
        }

        // edit and return value
        StoreRating storeRating = storeRepository.editRatingByStoreId(storeId, currentUser, ratingRequest);
        if (storeRating == null) {
            throw new InternalServerErrorException("Error while editing rating.");
        }
        storeRating.setCreatedDate(formatter.format(formatter.parse(storeRating.getCreatedDate())));
        storeRating.setUpdatedDate(formatter.format(formatter.parse(storeRating.getUpdatedDate())));
        return storeRating;
    }

    @Override
    public String deleteRatingByStoreId(Integer storeId) {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
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
            throw new NotFoundException("This store id does not exist.");
        }

        if (!(storeRatingRequest.getRatedStar() > 0 && storeRatingRequest.getRatedStar() < 6)) {
            throw new BadRequestException("Out of range. Rating range is from 1 to 5.");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();

        // check if already rated
        if (checkAlreadyRated(storeId, currentUser)) {
            throw new ConflictException("Already rated.");
        }
        // rate and get rate object
        StoreRating storeRating = storeRepository.ratingStoreById(storeId, currentUser, storeRatingRequest);
        if (storeRating == null) {
            throw new InternalServerErrorException("Error while in rating operation.");
        }
        storeRating.setCreatedDate(formatter.format(formatter.parse(storeRating.getCreatedDate())));
        storeRating.setUpdatedDate(formatter.format(formatter.parse(storeRating.getUpdatedDate())));
        return storeRating;
    }

    @Override
    public List<Product> getProductListingByStoreId(Integer storeId, String sort, String by) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }
        String[] validStrings = {"name", "qty", "price", "created_date"};
        if (Arrays.stream(validStrings).noneMatch(by::contains)) {
            throw new BadRequestException("Available sort are: 'name', 'qty', 'price', and 'created_date'.");
        }
        List<Product> products = null;
        if (sort.equalsIgnoreCase("asc")) {
            products = storeRepository.getProductListingByStoreIdASC(storeId, by);
        } else if (sort.equalsIgnoreCase("desc")) {
            products = storeRepository.getProductListingByStoreIdDESC(storeId, by);
        }
        assert products != null;
        if (products.isEmpty()) {
            throw new OKException("Products not found.");
        }
        return getProducts(products, formatter);
    }

    static List<Product> getProducts(List<Product> products, SimpleDateFormat formatter) throws ParseException {
        for (Product product : products) {
            product.setCreatedDate(formatter.format(formatter.parse(product.getCreatedDate())));
            product.setUpdatedDate(formatter.format(formatter.parse(product.getUpdatedDate())));
            product.getCategory().setCreatedDate(formatter.format(formatter.parse(product.getCategory().getCreatedDate())));
            product.getCategory().setUpdatedDate(formatter.format(formatter.parse(product.getCategory().getUpdatedDate())));
        }
        return products;
    }

    @Override
    public Integer getTotalStore() {
        return storeRepository.getTotalStores();
    }

    @Override
    public Integer getTotalRatedStores() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        return storeRepository.getTotalRatedStores(retailerId);
    }

    @Override
    public Integer getTotalBookmarkedStores() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer retailerId = appUser.getId();
        return storeRepository.getTotalBookmarkedStores(retailerId);
    }

    @Override
    public Integer findTotalPage(Integer totalStore, Integer pageSize) {
        int totalPage;
        if (totalStore % pageSize == 0) {
            totalPage = totalStore / pageSize;
        } else {
            totalPage = (Integer) (totalStore / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public List<StoreRetailer> getAllUserStoreSortByDate(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        // check sort spelling
        if (!(sort.toLowerCase().equals("asc") || sort.toLowerCase().equals("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new NotFoundException("Page number and size should be higher than 0.");
        }
        // fetch stores
        List<StoreRetailer> stores;
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
            throw new NotFoundException("Stores not found");
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreRetailer> getAllUserStoreSortByCurrentUserFavorite(Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        List<StoreRetailer> stores = storeRepository.getAllUserStoreSortByCurrentUserFavoriteDESC(pageNumber, pageSize, currentUser);
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException("Stores not found");
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreRetailer> getAllBookmarkedStore(Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        if (getTotalBookmarkedStores() == 0) {
            throw new NotFoundException("No bookmarked store found.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        List<StoreRetailer> stores = storeRepository.getAllBookmarkedStore(pageNumber, pageSize, currentUser);
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException("Stores not found");
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreRetailer> searchStoreByName(Integer pageNumber, Integer pageSize, String name) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        List<StoreRetailer> stores = storeRepository.searchStoreByName(pageNumber, pageSize, name);
        // get all store
        Integer totalStore = getTotalStore();
        // find total page
        Integer totalPage = findTotalPage(totalStore, pageSize);
        // check out of range
        if (totalStore < pageSize * pageNumber && stores.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException("Stores not found");
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<Category> getCategoryListingByStoreId(Integer storeId) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }
        List<Category> categories = storeRepository.getCategoryListingByStoreId(storeId);
        if (categories.isEmpty()) {
            throw new OKException("Products not found.");
        }
        for (Category category : categories) {
            category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
            category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
        }
        return categories;
    }

    @Override
    public List<Product> getProductListingByStoreIdAndCategoryId(Integer storeId, Integer categoryId) throws ParseException {
        // check if store exist
        if (!checkIfStoreExist(storeId)) {
            throw new NotFoundException("This store id does not exist.");
        }
        if (!storeRepository.checkIfCategoryExistInStore(storeId, categoryId)) {
            throw new NotFoundException("Category does not exist in this store.");
        }
        List<Product> products = storeRepository.getProductListingByStoreIdAndCategoryId(storeId, categoryId);
        if (products.isEmpty()) {
            throw new OKException("Product not Found. This category have no products.");
        }
        for (Product product : products) {
            product.setCreatedDate(formatter.format(formatter.parse(product.getCreatedDate())));
            product.setUpdatedDate(formatter.format(formatter.parse(product.getUpdatedDate())));
            product.getCategory().setCreatedDate(formatter.format(formatter.parse(product.getCategory().getCreatedDate())));
            product.getCategory().setUpdatedDate(formatter.format(formatter.parse(product.getCategory().getUpdatedDate())));
        }
        return products;
    }

    @Override
    public List<StoreRetailer> getStoresByCategorySearch(String name, String sort, String by) throws ParseException {
        // check sort spelling
        if (!(sort.toLowerCase().equals("asc") || sort.toLowerCase().equals("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        by = by.toLowerCase().trim();
        if (!(by.equals("created_date") || by.equals("is_publish") || by.equals("name"))) {
            throw new BadRequestException("Invalid input. Available sorting are 'created_date' - 'is_publish' - 'name'. Case sensitive not needed.");
        }
        List<StoreRetailer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getStoresByCategorySearchASC(name, by);
        } else {
            stores = storeRepository.getStoresByCategorySearchDESC(name, by);
        }
        if (stores.isEmpty()) {
            throw new NotFoundException("No store was found.");
        }
        for (StoreRetailer store : stores) {
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreRetailer> getStoresBySearch(String name, String sort, String by) throws ParseException {
        AppUser appUser =(AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        // check sort spelling
        if (!(sort.toLowerCase().equals("asc") || sort.toLowerCase().equals("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        by = by.toLowerCase().trim();
        if (!(by.equals("created_date") || by.equals("is_publish") || by.equals("name"))) {
            throw new BadRequestException("Invalid input. Available sorting are 'created_date' - 'is_publish' - 'name'. Case sensitive not needed.");
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

        List<StoreRetailer> stores;
        if (sort.toLowerCase().equals("asc")) {
            stores = storeRepository.getStoresByStoreIdsASC(combinedList.toString().replaceAll("\\[|\\]", ""));
        } else {
            stores = storeRepository.getStoresByStoreIdsDESC(combinedList.toString().replaceAll("\\[|\\]", ""));
        }
        if (stores.isEmpty()) {
            throw new NotFoundException("No store was found.");
        }
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for_loop:
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreRetailer> getAllUserStoreSortByRatedStar(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        // check sort spelling
        if (!(sort.toLowerCase().equals("asc") || sort.toLowerCase().equals("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch stores
        List<StoreRetailer> stores;
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
            throw new NotFoundException("Stores not found");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
            }
        }
        return stores;
    }

    @Override
    public List<StoreRetailer> getAllUserStoreSortByName(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        // check sort spelling
        if (!(sort.toLowerCase().equals("asc") || sort.toLowerCase().equals("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException("Page number and size should be higher than 0.");
        }
        // fetch stores
        List<StoreRetailer> stores;
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
            throw new NotFoundException("Stores not found");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUser = appUser.getId();
        List<Integer> bookmarkStoreId = storeRepository.getBookmarkStoreId(currentUser);
        for (StoreRetailer store : stores) {
            store.setIsBookmarked(bookmarkStoreId.contains(store.getId()));
            store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
            store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
            for (Category category : store.getCategories()) {
                category.setCreatedDate(formatter.format(formatter.parse(category.getCreatedDate())));
                category.setUpdatedDate(formatter.format(formatter.parse(category.getUpdatedDate())));
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


