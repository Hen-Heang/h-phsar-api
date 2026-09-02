package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.common.ExceptionMessages;
import com.henheang.hphsar.exception.*;
import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.category.CategoryRequest;
import com.henheang.hphsar.repository.CategoryRepository;
import com.henheang.hphsar.service.CategoryService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class CategoryServiceImplV1 implements CategoryService {

    private static final DateTimeFormatter INPUT_TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();
    private static final DateTimeFormatter OUTPUT_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** Matches tb_category.name VARCHAR(150) in schema.sql. */
    private static final int MAX_CATEGORY_NAME_LENGTH = 150;

    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

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
//            throw new BadRequestException(ExceptionMessages.CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_VALUE);
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
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
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
        // move all products that have this category to 113 UNKNOWN
        if (categoryRepository.checkIfCategoryHaveProduct(storeId, id)) {
            categoryRepository.createNewStoreCategory(storeId, 113);
            String unknownCategoryId = categoryRepository.moveProductCategory(storeId, id);
            if (!Objects.equals(unknownCategoryId, "113")) {
                throw new InternalServerErrorException("Fail to change category.");
            }
        }
        String categoryName = categoryRepository.getCategoryNameById(id);
        Integer rowsAffected = categoryRepository.deleteCategory(id, storeId);
        if (rowsAffected == null || rowsAffected == 0) {
            throw new InternalServerErrorException("Fail to delete category.");
        }
        return "Successfully deleted category : " + categoryName;
    }

    //Validation of updating in category
    @Override
    public Category editCategory(String name, Integer id) throws ParseException {
        if (name == null || name.isBlank()) {
            throw new DefaultValueException(ExceptionMessages.CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_LEGAL);
        }
        if (name.equalsIgnoreCase("UNKNOWN")) {
            throw new BadRequestException("Can not change category to unknown");
        }
        if (name.equals("string")) {
            throw new DefaultValueException(ExceptionMessages.CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_LEGAL);
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
    @Transactional
    public Category createCategoryStore(String name) throws ParseException {

        if (name == null || name.isBlank()) {
            throw new BadRequestException("Invalid name insertion. Please input name.");
        }
        Integer storeId = getStoreIdForCurrentUser();
        // trim white space
        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        // One upsert replaces the old "checkDuplicateCategory then createNewCategory" pair.
        // That pair lost the race between two suppliers adding the same new name, and it
        // failed outright on a soft-deleted name — is_active = false still holds the
        // UNIQUE(name) row, but checkDuplicateCategory reported the name as free.
        Integer categoryId = categoryRepository.upsertCategoryByName(normalizedName);
        // Keeps this endpoint's existing 409: adding a category the store already has is
        // an error here, unlike the product-creation path, which is deliberately idempotent.
        if (categoryRepository.checkIfStoreCategoryDuplicate(storeId, categoryId)) {
            throw new ConflictException("Fail to create category because store already created this category.");
        }
        Category category = categoryRepository.createNewStoreCategory(storeId, categoryId);
        // if insert fail
        if (category == null) {
            throw new InternalServerErrorException("Fail to create category. Something went during the process.");
        }
        normalizeCategoryDate(category);
        return category;
    }

    @Override
    @Transactional
    public Integer resolveCategoryIdForStore(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Invalid category name. Please input name.");
        }
        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        // tb_category.name is VARCHAR(150); rejecting here turns a driver-level failure
        // into a 400 that names the offending category.
        if (normalizedName.length() > MAX_CATEGORY_NAME_LENGTH) {
            throw new BadRequestException("Category name is too long: " + name);
        }
        Integer storeId = getStoreIdForCurrentUser();
        Integer categoryId = categoryRepository.upsertCategoryByName(normalizedName);
        if (categoryId == null) {
            throw new InternalServerErrorException("Fail to create category. Something went during the process.");
        }
        // 0 affected rows is the "store already had this category" case, which is a
        // success for this method — unlike createCategoryStore, resolving is idempotent.
        categoryRepository.linkStoreCategoryIfAbsent(storeId, categoryId);
        return categoryId;
    }


    @Override
    public List<Category> getCategoryByCurrentUserId() {
        Integer storeId = getStoreIdForCurrentUser();
        List<Category> categories = categoryRepository.getCategoryByCurrentUserId(storeId);
        normalizeCategoryDates(categories);
        return categories;
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
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
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
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
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






