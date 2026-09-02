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

    /**
     * 카테고리 이름으로 기존 행을 찾거나 없으면 새로 만들어 id 를 돌려준다.
     * 동시 요청과 소프트 삭제된 동명 카테고리를 DB 의 UNIQUE(name) 제약으로 한 번에 처리한다.
     *
     * @param name 정규화된 카테고리 이름
     * @return 확정된 tb_category.id
     */
    Integer upsertCategoryByName(@Param("name") String name);

    /**
     * 스토어에 카테고리를 연결한다. 이미 연결돼 있으면 아무 행도 넣지 않는다.
     *
     * @param storeId    대상 스토어 id
     * @param categoryId 연결할 카테고리 id
     * @return 삽입된 행 수 (1 = 신규 연결, 0 = 이미 연결됨)
     */
    int linkStoreCategoryIfAbsent(@Param("storeId") Integer storeId, @Param("categoryId") Integer categoryId);

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



