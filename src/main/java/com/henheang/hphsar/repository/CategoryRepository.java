package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.category.CategoryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryRepository {

    Category getDuplicateCategory(@Param("name") String name);

    Category insertCategory(@Param("category") CategoryRequest categoryRequest);

    List<Category> getAllCategory(Integer storeId, Integer pageNumber, Integer pageSize);

    Category getCategoryById(@Param("id") Integer id, @Param("storeId") Integer storeId);

    Category editCategory(Integer categoryId, Integer id, Integer storeId);

    Integer deleteCategory(Integer id, Integer storeId);

    Boolean checkIfExist(String name);

    Integer getStoreIdByCurrentUserId(Integer userId);

    void addCategoryToStore(Integer categoryId, Integer storeId);

    Integer getCategoryInCurrentStoreId(Integer categoryId, Integer storeId);

    List<Category> getCategoryByCurrentUserId(Integer storeId);

    boolean checkDuplicateCategory(String name);

    Integer createNewCategory(String name);

    Category createNewStoreCategory(Integer storeId, Integer newCategoryId);

    String getCategoryNameById(Integer id);

    String getCategoryCreatedDateById(Integer id);

    String getCategoryUpdatedById(Integer id);

    Integer getCategoryIdByName(String name);

    boolean checkIfStoreCategoryDuplicate(Integer storeId, Integer categoryId);

    Integer findTotalCategory(Integer storeId);

    List<Category> searchCategoryByName(@Param("name") String name,
                                        @Param("storeId") Integer storeId,
                                        @Param("pageNumber") Integer pageNumber,
                                        @Param("pageSize") Integer pageSize);

    boolean storeIsExist(Integer currentUserId);

    Integer getCategories(Integer storeId);

    String moveProductCategory(Integer storeId, Integer id);

    boolean checkIfCategoryHaveProduct(Integer storeId, Integer id);

    void replaceProductCategory(Integer oldId, Integer newId, Integer storeId);

    Integer getCategoryIdByProductId(Integer id);


}



