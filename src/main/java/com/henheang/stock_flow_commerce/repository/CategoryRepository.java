package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.category.CategoryRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryRepository {

//    @Select("""
//            SELECT exists(select *from warehouse_master.public.tb_store_category where category_id=#{id});
//             """)
//    Category checkExistCategoryStore(Integer id);

    @Select("""
            SELECT * FROM tb_category
            WHERE name ILIKE #{name};
            """)
    @ResultMap("categoryMap")
    Category getDuplicateCategory(@Param("name") String name);

    @Select("""
            INSERT INTO tb_category(name)
            VALUES(#{category.name})
            RETURNING *
            """)
    @Results(id = "categoryMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "createdDate", column = "created_date"),
            @Result(property = "updatedDate", column = "updated_date")
    })
    Category insertCategory(@Param("category") CategoryRequest categoryRequest);

//    @Select("""
//            WITH ROWS AS (
//            INSERT INTO tb_store_category
//            VALUES (DEFAULT, #{storeId}, #{categoryId})
//            RETURNING category_id)
//            SELECT id AS categoryId, name AS categoryName, created_date AS createdDate, updated_date AS updatedDate
//            FROM tb_category
//            WHERE id = (SELECT category_id FROM ROWS);
//            """)
//    Category insertCategory(Integer storeId, Integer categoryId);

    @Select("""
            SELECT * FROM tb_store_category
            WHERE store_id = #{storeId}
            ORDER BY category_id ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "category_id")
    @Result(property = "name", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryNameById"))
    @Result(property = "createdDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryCreatedDateById"))
    @Result(property = "updatedDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryUpdatedById"))
    List<Category> getAllCategory(Integer storeId, Integer pageNumber, Integer pageSize);


//    @Select("""
//            SELECT *
//            FROM tb_store_category
//            WHERE category_id = #{id}
//            AND store_id = #{storeId};
//            """)
//    @Result(property = "id", column = "category_id")
//    @Result(property = "name", column = "category_id",
//            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryNameById"))
//    @Result(property = "createdDate", column = "category_id",
//            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryCreatedDateById"))
//    @Result(property = "updatedDate", column = "category_id",
//            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryUpdatedById"))
    Category getCategoryById(@Param("id") Integer id, @Param("storeId") Integer storeId);


    @Select("""
            UPDATE tb_store_category
            SET category_id = #{categoryId}
            WHERE store_id = #{storeId}
            AND category_id = #{id}
            RETURNING category_id;
            """)
    @Result(property = "id", column = "category_id")
    @Result(property = "name", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryNameById"))
    @Result(property = "createdDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryCreatedDateById"))
    @Result(property = "updatedDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryUpdatedById"))
    Category editCategory(Integer categoryId, Integer id, Integer storeId);

    @Select("""
            DELETE FROM tb_store_category
            WHERE category_id = #{id}
            AND store_id = #{storeId}
            RETURNING category_id;
            """)
    String deleteCategory(Integer id, Integer storeId);


    @Select("""
            SELECT EXISTS(SELECT name FROM tb_category WHERE name ILIKE #{name});
            """)
    Boolean checkIfExist(String name);

    @Select("""
            select id from tb_store
            where distributor_account_id = #{userId};
            """)
    Integer getStoreIdByCurrentUserId(Integer userId);

    @Insert("""
            INSERT INTO tb_store_category(store_id,category_id)
            VALUES(#{storeId},#{categoryId});
            """)
    void addCategoryToStore(Integer categoryId, Integer storeId);

//    @Select("""
//            select id from tb_category
//            where name ILIKE #{name};
//            """)
//    Integer getCategoryIdByName(String name);

//    @Select("""
//            select exists(select * from tb_store_category
//            where store_id = #{storeId} and category_id = #{categoryId});
//            """)
//    boolean checkStoreCategoryDuplicate(Integer storeId, Integer categoryId);

    @Select("""
            SELECT id FROM tb_store_category
            WHERE store_id = #{storeId} and category_id=#{categoryId};
            """)
//    @ResultMap("categoryMap")
    Integer getCategoryInCurrentStoreId(Integer categoryId, Integer storeId);

    @Select("""
            SELECT TCG.id,name,created_date, updated_date FROM tb_category TCG INNER JOIN tb_store_category TSC ON TCG.id = TSC.category_id
            WHERE TSC.store_id = #{storeId};
            """)
    @ResultMap("categoryMap")
    List<Category> getCategoryByCurrentUserId(Integer storeId);

    @Select("""
            SELECT exists(SELECT * FROM tb_category WHERE name ILIKE #{name});
            """)
    boolean checkDuplicateCategory(String name);

    @Select("""
            INSERT INTO tb_category(name)
            VALUES (#{name})
            RETURNING id;
            """)
    Integer createNewCategory(String name);

    @Select("""
            INSERT into tb_store_category
            VALUES (DEFAULT, #{storeId}, #{newCategoryId})
            RETURNING category_id;
            """)
    @Result(property = "id", column = "category_id")
    @Result(property = "name", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryNameById"))
    @Result(property = "createdDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryCreatedDateById"))
    @Result(property = "updatedDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryUpdatedById"))
    Category createNewStoreCategory(Integer storeId, Integer newCategoryId);

    @Select("""
            SELECT name FROM tb_category
            WHERE id = #{id};
            """)
    String getCategoryNameById(Integer id);

    @Select("""
            SELECT created_date AS createdDate FROM tb_category
            WHERE id = #{id};
            """)
    String getCategoryCreatedDateById(Integer id);
    @Select("""
            SELECT updated_date AS updatedDate FROM tb_category
            WHERE id = #{id};
            """)
    String getCategoryUpdatedById(Integer id);
    @Select("""
            SELECT id FROM tb_category
            WHERE name ILIKE #{name};
            """)
    Integer getCategoryIdByName(String name);

    @Select("""
            SELECT exists(SELECT * FROM tb_store_category WHERE store_id = #{storeId} AND category_id = #{categoryId});
            """)
    boolean checkIfStoreCategoryDuplicate(Integer storeId, Integer categoryId);

    @Select("""
            SELECT count(*) FROM tb_store_category
            WHERE store_id = #{storeId};
            """)
    Integer findTotalCategory(Integer storeId);

    @Select("""
            SELECT * FROM tb_store_category
            WHERE store_id = #{storeId}
            AND category_id IN (SELECT id FROM tb_category WHERE name ILIKE CONCAT('%', #{name}, '%'))
            ORDER BY category_id ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "category_id")
    @Result(property = "name", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryNameById"))
    @Result(property = "createdDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryCreatedDateById"))
    @Result(property = "updatedDate", column = "category_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.CategoryRepository.getCategoryUpdatedById"))
    List<Category> searchCategoryByName(@Param("name") String name,
                                        @Param("storeId") Integer storeId,
                                        @Param("pageNumber") Integer pageNumber,
                                        @Param("pageSize") Integer pageSize);

    @Select("""
            SELECT exists(SELECT * FROM tb_store WHERE distributor_account_id = #{currentUserId});
            """)
    boolean storeIsExist(Integer currentUserId);

    @Select("""
            SELECT count(*) FROM tb_store_category
            WHERE store_id = #{storeId};
            """)
    Integer getCategories(Integer storeId);

    @Select("""
            UPDATE tb_store_product_detail
            SET category_id = 113
            WHERE category_id = #{id}
            AND store_id = #{storeId}
            RETURNING 113;
            """)
    String moveProductCategory(Integer storeId, Integer id);

    @Select("""
            SELECT exists(
            SELECT * FROM tb_store_product_detail
            WHERE store_id = #{storeId}
            AND category_id = #{id}
            );
            """)
    boolean checkIfCategoryHaveProduct(Integer storeId, Integer id);

    @Update("""
            UPDATE tb_store_product_detail
            SET category_id = #{newId}
            WHERE category_id = #{oldId}
            AND store_id = #{storeId}
            """)
    void replaceProductCategory(Integer oldId, Integer newId, Integer storeId);

    @Select("""
            SELECT category_id
            FROM tb_store_product_detail
            WHERE id = #{id};
            """)
    Integer getCategoryIdByProductId(Integer id);


//    @Select("""
//            INSERT INTO tb_category
//            VALUES (default, #{name}, default, default)
//            RETURNING 1;
//            """)
//    @ResultMap("categoryMap")
//    String createNewCategory(String name);
//


}




