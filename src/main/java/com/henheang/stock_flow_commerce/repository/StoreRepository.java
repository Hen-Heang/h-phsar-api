package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.rating.StoreRating;
import com.henheang.stock_flow_commerce.model.rating.StoreRatingRequest;
import com.henheang.stock_flow_commerce.model.store.Store;
import com.henheang.stock_flow_commerce.model.store.StoreRequest;
import com.henheang.stock_flow_commerce.model.store.StoreRetailer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StoreRepository {

    @Select("""
            INSERT INTO tb_store (id, distributor_account_id, name, description, created_date, updated_date, is_publish, is_active, banner_image, address, phone)
            VALUES (DEFAULT, #{currentUserId}, #{store.name}, #{store.description}, DEFAULT, DEFAULT, #{store.isPublish}, DEFAULT, #{store.bannerImage}, #{store.address}, #{store.primaryPhone})
            RETURNING id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    Store createNewStore(@Param("store") StoreRequest storeRequest, Integer currentUserId);


    // tb_store does not have rating. this method is for calculating rating if it does not have rating return 0
    @Select("""
            SELECT COALESCE(AVG(rated_star), 0) FROM tb_rating_detail
            WHERE store_id = #{id};
            """)
    Double getRating(Integer id);

    @Select("""
            SELECT COALESCE(COUNT(rated_star), 0) FROM tb_rating_detail
            WHERE store_id = #{id};
            """)
    Integer getRatingCount(Integer storeId);

    @Select("""
            SELECT store_id FROM tb_bookmark
            WHERE retailer_account_id = #{currentUserId};
            """)
    List<Integer> getBookmarkStoreId(Integer currentUserId);


    @Select("""
            SELECT COALESCE((SELECT 1 FROM tb_store WHERE distributor_account_id = #{currentUserId}), 0) AS isExist;
            """)
    Integer checkStoreIfCreated(Integer currentUserId);

    @Select("""
            SELECT id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, phone AS primaryPhone, created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive 
            FROM tb_store
            WHERE distributor_account_id = #{currentUserId};
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    Store getUserStore(Integer currentUserId);

    @Select("""
            SELECT id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive, phone AS primaryPhone FROM tb_store
            WHERE is_publish = true;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllStore();

    @Select("""
            SELECT phone FROM tb_store_phone
            WHERE store_id = #{storeId};
            """)
    List<String> getAdditionalPhone(Integer storeId);

    @Select("""
            UPDATE tb_store
            SET name         = #{store.name},
                description  = #{store.description},
                updated_date = NOW(),
                is_publish   = #{store.isPublish},
                banner_image = #{store.bannerImage},
                address = #{store.address},
                phone = #{store.primaryPhone}
            WHERE id = #{storeId}
            RETURNING id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, phone AS primaryPhone,created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    Store editAllFieldUserStore(Integer storeId, @Param("store") StoreRequest storeRequest);

    @Select("""
            SELECT id FROM tb_store
            WHERE distributor_account_id = #{currentUserId};
            """)
    Integer getStoreIdByUserId(Integer currentUserId);

    @Select("""
            SELECT exists(SELECT name FROM tb_role WHERE name ILIKE #{name});
            """)
    Boolean checkDuplicateStoreName(String name);

    @Select("""
            DELETE FROM tb_store
            WHERE distributor_account_id = #{currentUserId}
            RETURNING 1;
            """)
    String deleteUserStore(Integer currentUserId);

    @Select("""
            UPDATE tb_store
            SET is_publish = false, updated_date = now()
            WHERE distributor_account_id = #{currentUserId}
            RETURNING 1;
            """)
    String disableStore(Integer currentUserId);

    @Select("""
            UPDATE tb_store
            SET is_publish = true, updated_date = now()
            WHERE distributor_account_id = #{currentUserId}
            RETURNING 1;
            """)
    String enableStore(Integer currentUserId);

    @Select("""
            SELECT exists(SELECT * FROM tb_store
            WHERE id = #{storeId}
            AND is_publish = true);
            """)
    Boolean checkIfStoreExist(Integer storeId);


    @Select("""
            SELECT id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, phone AS primaryPhone, created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive 
            FROM tb_store
            WHERE id = #{id};
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    StoreRetailer getStoreById(Integer id);

    @Select("""
            INSERT INTO tb_bookmark
            VALUES (DEFAULT, #{storeId}, #{currentUser})
            RETURNING 1;
            """)
    String bookmarkStoreById(Integer storeId, Integer currentUser);

    @Select("""
            DELETE
            FROM tb_bookmark
            WHERE store_id = #{storeId}
            AND retailer_account_id = #{currentUser}
            RETURNING 1;
            """)
    String removeBookmarkStoreById(Integer storeId, Integer currentUser);

    @Select("""
            SELECT exists(SELECT * FROM tb_bookmark WHERE store_id = #{storeId} AND retailer_account_id = #{currentUserId});
            """)
    Boolean checkAlreadyBookmarked(Integer storeId, Integer currentUserId);

    @Select("""
            SELECT exists(SELECT * FROM tb_rating_detail WHERE store_id = #{storeId} AND retailer_account_id = #{currentUserId});
            """)
    Boolean checkAlreadyRated(Integer storeId, Integer currentUserId);

    @Select("""
            INSERT INTO tb_rating_detail
            VALUES (DEFAULT, #{storeId}, #{currentUser}, #{request.ratedStar}, DEFAULT, DEFAULT)
            RETURNING id, store_id AS storeId, retailer_account_id AS retailerId,rated_star AS ratedStar, created_date AS createdDate,
                updated_date AS updatedDate;
            """)
    StoreRating ratingStoreById(Integer storeId, Integer currentUser, @Param("request") StoreRatingRequest storeRatingRequest);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, rated_star AS ratedStar, created_date AS createdDate, updated_date AS updatedDate
            FROM tb_rating_detail
            WHERE store_id = #{storeId} AND retailer_account_id = #{currentUser};
            """)
    StoreRating getRatingByStoreId(Integer storeId, Integer currentUser);


    @Select("""
            UPDATE tb_rating_detail
            SET rated_star = #{request.ratedStar}, updated_date = now()
            WHERE store_id = #{storeId} AND retailer_account_id = #{currentUser}
            RETURNING id, store_id AS storeId, retailer_account_id AS retailerId,rated_star AS ratedStar, created_date AS createdDate,
                updated_date AS updatedDate;
            """)
    StoreRating editRatingByStoreId(Integer storeId, Integer currentUser, @Param("request") StoreRatingRequest ratingRequest);

    @Select("""
            DELETE
            FROM tb_rating_detail
            WHERE store_id = #{storeId}
              AND retailer_account_id = #{currentUser}
            RETURNING 1;
            """)
    String deleteRatingByStoreId(Integer storeId, Integer currentUser);

    @Select("""
            SELECT store_id     AS storeId,
                   td.id   AS id,
                   name,
                   qty,
                   price,
                   image,
                   description,
                   category_id,
                   is_publish   AS isPublish,
                   created_date AS createdDate,
                   updated_date AS updatedDate
            FROM tb_store_product_detail td
                     INNER JOIN tb_product tp ON td.product_id = tp.id
            WHERE td.store_id = #{storeId}
            ORDER BY ${by} ASC;
             """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryByCategoryId"))
    List<Product> getProductListingByStoreIdASC(Integer storeId, String by);

    @Select("""
            SELECT store_id     AS storeId,
                   td.id   AS id,
                   name,
                   qty,
                   price,
                   image,
                   description,
                   category_id,
                   is_publish   AS isPublish,
                   created_date AS createdDate,
                   updated_date AS updatedDate
            FROM tb_store_product_detail td
                     INNER JOIN tb_product tp ON td.product_id = tp.id
            WHERE td.store_id = #{storeId}
            ORDER BY ${by} DESC;
             """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryByCategoryId"))
    List<Product> getProductListingByStoreIdDESC(Integer storeId, String by);

    @Select("""
            SELECT store_id     AS storeId,
                   td.id   AS id,
                   name,
                   qty,
                   price,
                   image,
                   description,
                   category_id,
                   is_publish   AS isPublish,
                   created_date AS createdDate,
                   updated_date AS updatedDate
            FROM tb_store_product_detail td
                     INNER JOIN tb_product tp ON td.product_id = tp.id
            WHERE td.store_id = #{storeId}
            AND category_id = #{categoryId};
             """)
    @Result(property = "category", column = "category_id", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryByCategoryId"))
    List<Product> getProductListingByStoreIdAndCategoryId(Integer storeId, Integer categoryId);

    @Select("""
            SELECT id, name, created_date as createdDate, updated_date AS updatedDate FROM tb_category
            WHERE id = #{id};
            """)
    Category getCategoryByCategoryId(Integer id);

    @Select("""
            SELECT id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, phone AS primaryPhone, created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive 
            FROM tb_store
            WHERE is_publish = true
            ORDER BY created_date ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByDateASC(Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT id, name, address, banner_image AS bannerImage, distributor_account_id AS distributorAccountId, description, created_date AS createdDate, updated_date AS updatedDate, is_publish AS isPublish, is_active AS isActive, phone AS primaryPhone 
            FROM tb_store
            WHERE is_publish = true
            ORDER BY created_date DESC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByDateDESC(Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT count(*)
            FROM tb_store
            WHERE is_publish = true;
            """)
    Integer getTotalStores();

    @Select("""
            SELECT count(*)
            FROM tb_store
            JOIN tb_rating_detail trd ON tb_store.id = trd.store_id
            WHERE is_publish = true
            AND retailer_account_id = #{retailerId};
            """)
    Integer getTotalRatedStores(Integer retailerId);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
                     FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
            WHERE is_publish = TRUE
            ORDER BY (SELECT exists(SELECT * FROM tb_bookmark WHERE tb.retailer_account_id = #{currentUser})) DESC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByCurrentUserFavoriteDESC(Integer pageNumber, Integer pageSize, Integer currentUser);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND tb_store.id IN (SELECT DISTINCT store_id FROM tb_bookmark WHERE retailer_account_id = #{currentUser})
            ORDER BY created_date ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllBookmarkedStore(Integer pageNumber, Integer pageSize, Integer currentUser);
    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
                     FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
            WHERE is_publish = TRUE
            AND name ILIKE '%${name}%'
            ORDER BY (SELECT count(rated_star) FROM tb_rating_detail) DESC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> searchStoreByName(Integer pageNumber, Integer pageSize, String name);

    @Select("""
            SELECT tb_store.id                                      AS id,
                   name,
                   address,
                   banner_image                                     AS bannerImage,
                   tb_store.distributor_account_id                  AS distributorAccountId,
                   description,
                   phone                                            AS primaryPhone,
                   created_date                                     AS createdDate,
                   updated_date                                     AS updatedDate,
                   is_publish                                       AS isPublish,
                   is_active                                        AS isActive,
                   (SELECT coalesce(avg(rated_star), 0)  FROM tb_rating_detail WHERE store_id = tb_store.id) AS rating
            FROM tb_store
                     FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
            WHERE is_publish = TRUE
            GROUP BY tb_store.id
            ORDER BY rating ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByRatedStarASC(Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT tb_store.id                                      AS id,
                   name,
                   address,
                   banner_image                                     AS bannerImage,
                   tb_store.distributor_account_id                  AS distributorAccountId,
                   description,
                   phone                                            AS primaryPhone,
                   created_date                                     AS createdDate,
                   updated_date                                     AS updatedDate,
                   is_publish                                       AS isPublish,
                   is_active                                        AS isActive,
                   (SELECT coalesce(avg(rated_star), 0)  FROM tb_rating_detail WHERE store_id = tb_store.id) AS rating
            FROM tb_store
                     FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
            WHERE is_publish = TRUE
            GROUP BY tb_store.id
            ORDER BY rating DESC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByRatedStarDESC(Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT tb_store.id                      AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
                     FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
            WHERE is_publish = TRUE
            ORDER BY name ASC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByNameASC(Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT tb_store.id                      AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
                     FULL OUTER JOIN tb_bookmark tb ON tb_store.id = tb.store_id
            WHERE is_publish = TRUE
            ORDER BY name DESC
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getAllUserStoreSortByNameDESC(Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT store_id FROM tb_store_product_detail
            WHERE product_id = productId;
            """)
    Integer getStoreIdByProductId(Integer productId);

    @Select("""
            SELECT is_publish FROM tb_store WHERE distributor_account_id = #{currentUserId};
            """)
    boolean checkIfStoreIsDisable(Integer currentUserId);

    @Insert("""
            INSERT INTO tb_store_phone
            VALUES (DEFAULT, #{id}, #{phone});
            """)
    void addAdditionalPhone(String phone, Integer id);

    @Delete("""
            DELETE FROM tb_store_phone
            WHERE store_id = #{storeId};
            """)
    void deleteAdditionalPhone(Integer storeId);

    @Select("""
            SELECT count( DISTINCT store_id)
            FROM tb_bookmark
            WHERE retailer_account_id = #{retailerId};
            """)
    Integer getTotalBookmarkedStores(Integer retailerId);

    @Select("""
            SELECT banner_image FROM tb_store
            WHERE id = #{id};
            """)
    String getStoreImageByStoreId(Integer id);

    @Select("""
            SELECT name
            FROM tb_store
            WHERE id = #{id};
            """)
    String getStoreNameById(Integer id);

    @Select("""
            SELECT EXISTS(SELECT *
                          FROM tb_store
                                   INNER JOIN tb_store_phone tsp ON tb_store.id = tsp.store_id
                          WHERE phone = #{primaryPhone}
                             OR tsp.phone = #{primaryPhone});
            """)
    boolean checkDuplicatePhone(String primaryPhone);

    @Select("""
            SELECT DISTINCT A.id, A.name, A.updated_date AS updatedDate, A.created_date AS createdDate
            FROM tb_category A
                      INNER JOIN tb_store_product_detail B ON A.id = B.category_id
            WHERE store_id = #{storeId};
            """)
    List<Category> getCategoryListingByStoreId(Integer storeId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_store_category WHERE store_id = #{storeId} AND category_id = #{categoryId});
            """)
    boolean checkIfCategoryExistInStore(Integer storeId, Integer categoryId);

    @Select("""
            SELECT banner_image FROM tb_store
            WHERE id = #{id};
            """)
    String getStoreImageById(Integer id);

//    @Select("""
//            SELECT id
//            FROM tb_store_product_detail
//            WHERE qty = 0
//            AND store_id IN (SELECT store_id FROM tb_order WHERE id = #{orderId})
//            AND id IN (SELECT product_id FROM tb_order WHERE id = #{orderId});
//            """)
//    List<Integer> checkStock(Integer orderId);

    @Select("""
            SELECT id
            FROM tb_store_product_detail
            WHERE store_id IN (SELECT store_id FROM tb_order WHERE id = #{orderId})
            AND id IN (SELECT store_product_id FROM tb_order_detail WHERE order_id = #{orderId})
            AND qty = 0;
            """)
    List<Integer> checkStock(Integer orderId);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_category
                         WHERE category_id IN (SELECT id FROM tb_category WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_category
                         WHERE category_id IN (SELECT DISTINCT category_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
              AND is_active = true
            ORDER BY ${by} ASC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getStoresByCategorySearchASC(String name, String by);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_category
                         WHERE category_id IN (SELECT id FROM tb_category WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_category
                         WHERE category_id IN (SELECT DISTINCT category_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} DESC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getStoresByCategorySearchDESC(String name, String by);

    @Select("""
            SELECT tb_store.id
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT id FROM tb_product WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT DISTINCT product_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} ASC;
            """)
    List<Integer> getStoreIdsByCategorySearchASC(String name, String by);

    @Select("""
            SELECT tb_store.id
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT id FROM tb_product WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT DISTINCT product_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} DESC;
            """)
    List<Integer> getStoreIdsByCategorySearchDESC(String name, String by);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT id FROM tb_product WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT DISTINCT product_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} ASC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getStoresByProductSearchASC(String name, String by);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT id FROM tb_product WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT DISTINCT product_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} DESC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getStoresByProductSearchDESC(String name, String by);

    @Select("""
            SELECT tb_store.id
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT id FROM tb_product WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT DISTINCT product_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} ASC;
            """)
    List<Integer> getStoreIdByProductSearchASC(String name, String by);

    @Select("""
            SELECT tb_store.id
            FROM tb_store
            WHERE is_publish = TRUE
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT id FROM tb_product WHERE name ILIKE '%${name}%'))
              AND id IN (SELECT store_id
                         FROM tb_store_product_detail
                         WHERE product_id IN (SELECT DISTINCT product_id FROM tb_store_product_detail WHERE store_id = tb_store.id))
            ORDER BY ${by} DESC;
            """)
    List<Integer> getStoreIdByProductSearchDESC(String name, String by);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND name ILIKE '%${name}%'
            ORDER BY ${by} ASC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    List<StoreRetailer> getStoresByNameSearchASC(String name, String by);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE is_publish = TRUE
              AND name ILIKE '%${name}%'
            ORDER BY ${by} DESC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    List<StoreRetailer> getStoresByNameSearchDESC(String name, String by);

    @Select("""
            SELECT tb_store.id
            FROM tb_store
            WHERE is_publish = TRUE
              AND name ILIKE '%${name}%'
            ORDER BY ${by} ASC;
            """)
    List<Integer> getStoresIdByNameSearchASC(String name, String by);

    @Select("""
            SELECT tb_store.id
            FROM tb_store
            WHERE is_publish = TRUE
              AND name ILIKE '%${name}%'
            ORDER BY ${by} DESC;
            """)
    List<Integer> getStoresIdByNameSearchDESC(String name, String by);


    @Select("""
            SELECT address
            FROM tb_store
            WHERE id = #{id};
            """)
    String getStoreAddressById(Integer id);

    @Select("""
            SELECT phone
            FROM tb_store
            WHERE id = #{id};
            """)
    String getStorePrimaryPhoneById(Integer id);

    @Select("""
            SELECT email FROM tb_distributor_account
            WHERE id = (SELECT distributor_account_id FROM tb_store WHERE id = #{id});
            """)
    String getStoreEmailByStoreId(Integer id);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE id IN (${combinedList})
            ORDER BY array_position('{${combinedList}}', id) ASC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getStoresByStoreIdsASC(String combinedList);

    @Select("""
            SELECT tb_store.id                     AS id,
                   name,
                   address,
                   banner_image                    AS bannerImage,
                   tb_store.distributor_account_id AS distributorAccountId,
                   description,
                   phone                           AS primaryPhone,
                   created_date                    AS createdDate,
                   updated_date                    AS updatedDate,
                   is_publish                      AS isPublish,
                   is_active                       AS isActive
            FROM tb_store
            WHERE id IN (${combinedList})
            ORDER BY array_position('{${combinedList}}', id) DESC;
            """)
    @Result(property = "id", column = "id")
    @Result(property = "rating", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRating"))
    @Result(property = "ratingCount", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getRatingCount"))
    @Result(property = "additionalPhone", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "categories", column = "id", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getCategoryListingByStoreId"))
    List<StoreRetailer> getStoresByStoreIdsDESC(String combinedList);

    @Select("""
            SELECT store_id
            FROM tb_order
            WHERE status_id = 8
            AND id = #{id};
            """)
    Integer getStoreIdByDraftId(Integer id);
}
