package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.product.ProductEditRequest;
import com.henheang.stock_flow_commerce.model.product.ProductRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface ProductDistributorRepository {


    @Select("""
            select id from tb_store where distributor_account_id= #{currentUserId};
            """)
    Integer getStoreIdByCurrentUserId(Integer currentUserId);

//    @Select("""
//            insert into tb_store_product_detail
//            values(default, #{storeId},#{productId} ,0, 0,default)
//            """)
//    void insertNewProductDetail(Integer productId, Integer storeId, ProductRequest productRequest);


    @Select("""
            insert into tb_product_import
            values(default, default, #{storeId}) returning id
            """)
    Integer insertNewProductImport(Integer storeId);

    @Select("""
            insert into tb_product_import_detail
            values(default,#{productId},#{productImportId},#{proRequest.qty},#{proRequest.price})
            """)
    void insertNewProductImportDetail(Integer productId, Integer productImportId, @Param("proRequest") ProductRequest productRequest);

    @Select("""
            select id from tb_category where name =#{categoryName}
            """)
    Integer getCategoryIdByName(String categoryName);

    @Select("""
            insert into tb_product_category
            values(default, #{categoryIdFromName}, #{productId} );
            """)
    void insertNewProductCategory(Integer productId, Integer categoryIdFromName);

    @Update("""
            UPDATE tb_store_product_detail
            SET qty = qty + #{qty}, price = #{price}, is_publish = #{isPublish}
            WHERE store_id = #{storeId} AND id = #{productId};
            """)
    void updateStoreProductDetail(Integer storeId, Integer productId, Integer qty, Double price, Boolean isPublish);


    @Select("""
            SELECT a.id, b.name, a.qty, a.price, a.image, category_id, is_publish AS isPublish, b.created_date AS createdDate, b.updated_date AS updatedDate, a.description
            FROM tb_store_product_detail a
                     JOIN tb_product b ON a.product_id = b.id
            WHERE a.id = #{productId}
            AND a.store_id = #{storeId};
            """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.ProductDistributorRepository.getCategoryByCategoryId"))
    Product getProductById(Integer storeId,Integer productId);

    @Select("""
             SELECT name FROM tb_category
                              WHERE id = #{categoryId};
            """)
    String getCategories(Integer categoryId);

    @Select("""
              delete from tb_product where id=#{productId};
            """)
    void deleteProductById(Integer productId, Integer storeId);

    @Select("""
            DELETE FROM tb_store_product_detail
            WHERE id=#{productId}
            AND store_id= #{storeId}
            RETURNING 1;
            """)
    String deleteProductDetailById(Integer productId, Integer storeId);

//    @Select("""
//              select p.id,name, qty, price ,image,p.category_id,is_active AS isActive,is_publish AS isPublish, created_date AS createdDate, updated_date AS updatedDate
//            from tb_product p inner join tb_store_product_detail s on p.id = s.product_id order by qty asc
//            """)
//    @Result(property = "categoryName", column = "category_id",
//            one = @One(select = "getCategories"))
//    @Result(property = "id", column = "id")
//    @Result(property = "categoryId", column = "category_id")
//    List<Product> getAllProductByQty(Integer storeId);

    @Select("""
            SELECT x.id,
                   name,
                   qty,
                   price,
                   image,
                   x.category_id,
                   is_active    AS isActive,
                   is_publish   AS isPublish,
                   created_date AS createdDate,
                   updated_date AS updatedDate
            FROM tb_store_product_detail x
                     JOIN tb_product y ON x.product_id = y.id
            WHERE store_id = #{storeId}
            AND name ILIKE '%${name}%';
            """)
    @Result(property = "categoryName", column = "category_id",
            one = @One(select = "getCategories"))
    @Result(property = "id", column = "id")
    @Result(property = "categoryId", column = "category_id")
    List<Product> getAllProductByName(String name, Integer storeId);

    @Select("""
            select p.id,name, qty, price ,image,is_active AS isActive,is_publish AS isPublish, created_date AS createdDate, updated_date AS updatedDate
            from tb_product p inner join tb_store_product_detail s on p.id = s.product_id where store_id=#{storeId} order by price asc ;
            """)
    List<Product> getAllProductByUnitPrice(Integer storeId);


    @Select("""
            SELECT COALESCE((SELECT id from tb_product where name ILIKE '${name}'), 0);
            """)
    Integer getProductIdByName(String name);

    @Select("""
            SELECT exists(SELECT * FROM tb_store_product_detail WHERE store_id = #{storeId} AND id = #{productId});
            """)
    boolean checkStoreHasProduct(Integer storeId, Integer productId);

    @Select("""
            INSERT INTO tb_product
            VALUES (DEFAULT, #{name}, DEFAULT, DEFAULT, true)
            RETURNING id;
            """)
    Integer createNewProduct(String name);

    @Select("""
            INSERT INTO tb_store_product_detail(id, store_id, product_id, qty, price, is_publish, image, category_id, description)
            VALUES (DEFAULT, #{storeId}, #{productId}, 0, 0, #{prod.isPublish}, #{prod.image}, #{prod.categoryId}, #{prod.description})
            RETURNING id;
            """)
    String insertNewProduct(Integer storeId, Integer productId, @Param("prod") ProductRequest productRequest);

    @Select("""
            INSERT INTO tb_product_import
            VALUES (DEFAULT, DEFAULT, #{storeId})
            RETURNING id;
            """)
    Integer createNewImportRecord(Integer storeId);

    @Insert("""
            INSERT INTO tb_product_import_detail(id, product_id, product_import_id, qty, price, category_id)
            VALUES (DEFAULT, #{productId}, #{importId}, #{qty}, #{price}, #{categoryId});
            """)
    void insertImportDetail(Integer importId, Integer productId, Integer qty, Double price, Integer categoryId);

    @Select("""
            SELECT a.id, b.name, a.qty, a.price, a.image, category_id, is_publish AS isPublish, b.created_date AS createdDate, b.updated_date AS updatedDate, a.description
            FROM tb_store_product_detail a
                     JOIN tb_product b ON a.product_id = b.id
            WHERE a.id = #{storeProductDetailId};
            """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.ProductDistributorRepository.getCategoryByCategoryId"))
    Product getStoreProductByStoreProductId(Integer storeProductDetailId);

    @Select("""
            SELECT id, name, created_date AS createdDate, updated_date AS updatedDate FROM tb_category
            WHERE id = #{id};
            """)
    Category getCategoryByCategoryId(Integer id);

    @Select("""
            UPDATE tb_store_product_detail
            SET product_id = #{productId}, price = #{prod.price}, image = #{prod.image}, description = #{prod.description}, category_id = #{prod.categoryId}, is_publish = true
            WHERE store_id = #{storeId}
            AND id = #{id}
            RETURNING id;
            """)
    String changeStoreProductDetail(Integer storeId, @Param("prod") ProductEditRequest productRequest, Integer productId, Integer id, Boolean isPublish);

    @Select("""
            UPDATE tb_store_product_detail
            SET is_publish = false
            WHERE store_id = #{storeId}
            AND id = #{id}
            RETURNING id;
            """)
    Integer unPublishProduct(Integer storeId, Integer id);
    @Select("""
            UPDATE tb_store_product_detail
            SET is_publish = true
            WHERE store_id = #{storeId}
            AND id = #{id}
            RETURNING id;
            """)
    Integer publishProduct(Integer storeId, Integer id);

    @Select("""
            SELECT a.id, b.name, a.qty, a.price, a.image, category_id, is_publish AS isPublish, b.created_date AS createdDate, b.updated_date AS updatedDate, a.description
            FROM tb_store_product_detail a
                     JOIN tb_product b ON a.product_id = b.id
            WHERE a.store_id = #{storeId}
            ORDER BY ${by} ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.ProductDistributorRepository.getCategoryByCategoryId"))
    List<Product> getAllProductByQtyASC(String by, Integer storeId, Integer pageSize, Integer pageNumber);

    @Select("""
            SELECT a.id, b.name, a.qty, a.price, a.image, category_id, is_publish AS isPublish, b.created_date AS createdDate, b.updated_date AS updatedDate, a.description
            FROM tb_store_product_detail a
                     JOIN tb_product b ON a.product_id = b.id
            WHERE a.store_id = #{storeId}
            ORDER BY ${by} DESC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.ProductDistributorRepository.getCategoryByCategoryId"))
    List<Product> getAllProductByQtyDESC(String by, Integer storeId, Integer pageSize, Integer pageNumber);

    @Select("""
            SELECT count(*) FROM tb_store_product_detail
            WHERE store_id = #{store};
            """)
    Integer getAllProduct(Integer storeId);

    @Select("""
            SELECT EXISTS(SELECT *
                          FROM tb_store_product_detail
                          WHERE id = #{id}
                            AND store_id = #{storeId}
                            AND is_publish = true);
            """)
    boolean checkProductPublish(Integer storeId, Integer id);

    @Select("""
            SELECT product_id FROM tb_store_product_detail
            WHERE store_id = #{storeId}
            AND id = #{id};
            """)
    Integer getProductIdInStoreProduct(Integer storeId, Integer id);

    @Select("""
            SELECT EXISTS(
                           SELECT *
                           FROM tb_store_product_detail
                           WHERE store_id = #{storeId});
            """)
    boolean checkStoreHasAnyProduct(Integer storeId);

    @Select("""
            SELECT EXISTS(SELECT *
                          FROM tb_order x
                                   JOIN tb_order_detail y ON x.id = y.order_id
                          WHERE store_product_id = #{productId}
                            AND store_id = (SELECT id FROM tb_store WHERE distributor_account_id = #{currentUserId}));
            """)
    boolean checkForProductInOrder(Integer currentUserId, Integer productId);


}