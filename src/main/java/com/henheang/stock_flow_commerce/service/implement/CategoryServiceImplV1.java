package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.*;
import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.category.CategoryRequest;
import com.henheang.stock_flow_commerce.repository.CategoryRepository;
import com.henheang.stock_flow_commerce.service.CategoryService;
import com.henheang.stock_flow_commerce.service.support.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class CategoryServiceImplV1 implements CategoryService {

    private static final DateTimeFormatter INPUT_TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();
    private static final DateTimeFormatter OUTPUT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public CategoryServiceImplV1(CategoryRepository categoryRepository, CurrentUserProvider currentUserProvider) {
        this.categoryRepository = categoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

//    public Boolean checkCategoryExist(String name) {
//        System.out.println(categoryRepository.checkIfExist(name));
//        return categoryRepository.checkIfExist(name);
//    }

//    @Override
//    public Category insertCategory(CategoryRequest categoryRequest) {
//        return createNewCategory(categoryRequest);
//    }


    @Override
    public Category insertCategory(CategoryRequest categoryRequest) {
        return categoryRepository.insertCategory(categoryRequest);
    }

    //    @Override
//    public Category insertCategory(CategoryRequest categoryRequest) {
//        if (categoryRequest.getName().equals("string") || categoryRequest.getName().isBlank()) {
//            throw new BadRequestException("Can not use default value. Please input value! ");
//        }
//        if (!(checkCategoryExist(categoryRequest.getName().toLowerCase().trim()))) {
//            String newCategoryID = categoryRepository.createNewCategory(categoryRequest.getName().toLowerCase().trim());
//            System.out.println(newCategoryID);
//            if (!Objects.equals(newCategoryID, "1")) {
//                throw new InternalServerErrorException("Insert category failed.");
//            }
//        } else {
//            throw new ConflictException("Already exist.");
//        }
//
//        // get store id by current user id
//        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Integer currentUserId = appUser.getId();
//        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
//        // get category id by category name
//        Integer categoryId = categoryRepository.getCategoryIdByName(categoryRequest.getName().toLowerCase().trim());
//        //check duplicate in tb_store_category
//        if (categoryRepository.checkStoreCategoryDuplicate(storeId, categoryId)) {
//            throw new ConflictException("This user already have this category");
//        }
//
//        // insert to store_category
//
//        Category category = categoryRepository.insertCategory(storeId, categoryId);
//        if (category == null) {
//            throw new InternalServerErrorException("Insert store category failed");
//        }
//
//// return category;
//    }
    @Override
    public List<Category> getAllCategory(Integer pageNumber, Integer pageSize) throws ParseException {
        Integer storeId = getStoreIdForCurrentUser();
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new NotFoundException("Page number and size should be higher than 0.");
        }
        if (categoryRepository.getCategories(storeId) < 1) {
            throw new BadRequestException("This store have no category. Please create new category.");
        }
        Integer totalPage = findTotalPage(pageSize);
        Integer totalCategory = categoryRepository.findTotalCategory(storeId);
        List<Category> categories = categoryRepository.getAllCategory(storeId, pageNumber, pageSize);
        if (totalCategory < pageSize * pageNumber && categories.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        normalizeCategoryDates(categories);
        return categories;
    }

    @Override
    public Category getCategoryById(Integer id) throws ParseException {
        Integer storeId = getStoreIdForCurrentUser();
        Category category = categoryRepository.getCategoryById(id, storeId);
        if (category == null) {
            throw new NotFoundException("Category Not found");
        }
        normalizeCategoryDate(category);
        return category;
    }

    @Override
    public String deleteCategory(Integer id) {
        Integer storeId = getStoreIdForCurrentUser();
        if (!categoryRepository.checkIfStoreCategoryDuplicate(storeId, id)) {
            throw new NotFoundException("Category not found.");
        }
        // move all product that have this category to 113 UNKNOWN
        if (categoryRepository.checkIfCategoryHaveProduct(storeId, id)) {
            categoryRepository.createNewStoreCategory(storeId, 113);
            String unknownCategoryId = categoryRepository.moveProductCategory(storeId, id);
            if (!Objects.equals(unknownCategoryId, "113")) {
                throw new InternalServerErrorException("Fail to change category.");
            }
        }
        String deletedCategoryId = categoryRepository.deleteCategory(id, storeId);
        if (deletedCategoryId == null) {
            throw new InternalServerErrorException("Fail to delete category.");
        }
        if (!Objects.equals(deletedCategoryId, String.valueOf(id))) {
            throw new InternalServerErrorException("Catastrophic error!!! Delete wrong category during operation.");
        }
        return "Successfully deleted category : " + categoryRepository.getCategoryNameById(Integer.parseInt(deletedCategoryId));
    }

    //Validation of updating in category
    @Override
    public Category editCategory(String name, Integer id) throws ParseException {
        if (name == null || name.isBlank()) {
            throw new DefaultValueException("Can not use default value. Please input legal value.");
        }
        if (name.equalsIgnoreCase("UNKNOWN")) {
            throw new BadRequestException("Can not change category to unknown");
        }
        if (name.equals("string")) {
            throw new DefaultValueException("Can not use default value. Please input legal value.");
        }
        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        Integer storeId = getStoreIdForCurrentUser();
        Integer categoryIdByName = categoryRepository.getCategoryIdByName(normalizedName);
        // check if id exists
        if (!categoryRepository.checkIfStoreCategoryDuplicate(storeId, id)) {
            throw new NotFoundException("This ID does not exist.");
        }
        if (categoryIdByName != null && categoryRepository.checkIfStoreCategoryDuplicate(storeId, categoryIdByName)) {
            throw new ConflictException("Already have this category name in this store. Please check and edit using different name.");
        }
        Category category;
        // check if new category exists or not
        if (categoryRepository.checkDuplicateCategory(normalizedName)) {
            // if exist get new category id
            Integer categoryId = categoryRepository.getCategoryIdByName(normalizedName);
            category = categoryRepository.editCategory(categoryId, id, storeId);
        } else {
            Integer newCategoryId = categoryRepository.createNewCategory(normalizedName);
            category = categoryRepository.editCategory(newCategoryId, id, storeId);
        }
        // internal server error just in case database error
        if (category == null) {
            throw new InternalServerErrorException("Internal server error. Fail to update category.");
        }
        // move from the old category to the new category
        categoryRepository.replaceProductCategory(id, category.getId(), storeId);

        normalizeCategoryDate(category);
        return category;
    }

//    @Override
//    public List<Product> getProductByCategoryById(Integer id) {
//        Category category = categoryRepository.getCategoryById(id);
//        if (category == null) {
//            throw new NotFoundException("Product Not found");
//        }
//        return categoryRepository.getProductByCategoryId(id);
//    }

//    @Override
//    public Category createCategoryStore(CategoryRequest categoryRequest) {
//        return categoryRepository.insertCategory(storeId, getCategoryById());
//    }

//    @Override
//    public Category createNewCategory(CategoryRequest categoryRequest) {
//        return categoryRepository.insertCategory(categoryRequest);
//    }

//    @Override
//    public Category createNewCategory(CategoryRequest categoryRequest) {
//        return categoryRepository.createNewCategory();
//    }


    @Override
    public Category createCategoryStore(String name) throws ParseException {

        if (name == null || name.isBlank()) {
            throw new BadRequestException("Invalid name insertion. Please input name.");
        }
        Integer storeId = getStoreIdForCurrentUser();
        // trim white space
        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        Category category;
        // if category is already created or haven't been created
        if (!categoryRepository.checkDuplicateCategory(normalizedName)) { // if not yet create, create new
            Integer newCategoryId = categoryRepository.createNewCategory(normalizedName);
            category = categoryRepository.createNewStoreCategory(storeId, newCategoryId);
        } else { // if already created, just get the id and insert
            Integer categoryId = categoryRepository.getCategoryIdByName(normalizedName);
            if (categoryRepository.checkIfStoreCategoryDuplicate(storeId, categoryId)) {
                throw new ConflictException("Fail to create category because store already created this category.");
            }
            category = categoryRepository.createNewStoreCategory(storeId, categoryId);
        }
        // if insert fail
        if (category == null) {
            throw new InternalServerErrorException("Fail to create category. Something went during the process.");
        }
        normalizeCategoryDate(category);
        return category;
    }


    @Override
    public List<Category> getCategoryByCurrentUserId() {
        Integer storeId = getStoreIdForCurrentUser();
        return categoryRepository.getCategoryByCurrentUserId(storeId);
    }

    @Override
    public Integer findTotalPage(Integer pageSize) {
        if (pageSize <= 0) {
            throw new BadRequestException("Page size should be higher than 0.");
        }
        Integer storeId = getStoreIdForCurrentUser();
        Integer totalCategory = categoryRepository.findTotalCategory(storeId);
        return (totalCategory + pageSize - 1) / pageSize;
    }

    @Override
    public List<Category> searchCategoryByName(String name, Integer pageNumber, Integer pageSize) throws ParseException {
        Integer storeId = getStoreIdForCurrentUser();
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new NotFoundException("Page number and size should be higher than 0.");
        }
        Integer totalPage = findTotalPage(pageSize);
        Integer totalCategory = categoryRepository.findTotalCategory(storeId);
        List<Category> categories = categoryRepository.searchCategoryByName(name, storeId, pageNumber, pageSize);
        if (categories == null) {
            throw new NotFoundException("No Categories are found in database! Please create new category!");
        }
        if (totalCategory < pageSize * pageNumber && categories.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        normalizeCategoryDates(categories);
        return categories;
    }

    private Integer getStoreIdForCurrentUser() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (!categoryRepository.storeIsExist(currentUserId)) {
            throw new NotFoundException("User have not created store. Please create store to proceed with category.");
        }
        return categoryRepository.getStoreIdByCurrentUserId(currentUserId);
    }

    private void normalizeCategoryDates(List<Category> categories) {
        for (Category category : categories) {
            normalizeCategoryDate(category);
        }
    }

    private void normalizeCategoryDate(Category category) {
        category.setCreatedDate(normalizeDateValue(category.getCreatedDate()));
        category.setUpdatedDate(normalizeDateValue(category.getUpdatedDate()));
    }

    private String normalizeDateValue(String dateValue) {
        if (dateValue == null || dateValue.isBlank()) {
            return dateValue;
        }
        String sanitizedValue = dateValue.trim().replace('T', ' ');
        if (sanitizedValue.endsWith("Z")) {
            sanitizedValue = sanitizedValue.substring(0, sanitizedValue.length() - 1);
        }
        try {
            return LocalDateTime.parse(sanitizedValue, INPUT_TIMESTAMP_FORMATTER).format(OUTPUT_TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException ex) {
            return sanitizedValue;
        }
    }
}






