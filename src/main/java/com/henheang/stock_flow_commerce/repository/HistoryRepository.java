package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.history.ImportHistory;
import com.henheang.stock_flow_commerce.model.history.OrderDetailHistory;
import com.henheang.stock_flow_commerce.model.history.OrderHistory;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface HistoryRepository {

//    @Select("""
//            SELECT a.id,
//                   imported_date AS date,
//                   category_id,
//                   tpid.product_id,
//                   a.store_id
//            FROM tb_product_import a
//                     JOIN tb_store_product_detail b ON a.store_id = b.store_id
//                     JOIN tb_product_import_detail tpid ON a.id = tpid.product_import_id
//            WHERE a.store_id = #{storeId}
//              AND tpid.product_import_id IN (a.id)
//              ORDER BY date DESC
//              LIMIT #{pageSize}
//              OFFSET #{pageSize} * (#{pageNumber} -1);;
//            """)
//    @Result(property = "category", column = "{id=category_id,test=product_id}", one = @One(select = "getCategoryNameById"))
//    @Result(property = "name", column = "product_id", one = @One(select = "getProductNameByid"))
//    @Result(property = "qty", column = "{productId=product_id , date=date, storeId=store_id}", one = @One(select = "com.henheang.stock_flow_commerce.repository.HistoryRepository.getImportDetailQty"))
//    @Result(property = "price", column = "{productId=product_id , date=date, storeId=store_id}", one = @One(select = "com.henheang.stock_flow_commerce.repository.HistoryRepository.getImportDetailPrice"))
//    @Result(property = "total", column = "{productId=product_id , date=date, storeId=store_id}", one = @One(select = "com.henheang.stock_flow_commerce.repository.HistoryRepository.getImportDetailTotal"))
//    List<ImportHistory> getProductImportHistoryASC(Integer pageNumber, Integer pageSize, Integer storeId);

//    @Select("""
//            SELECT b.id,
//                   a.id AS importId,
//                   imported_date AS date,
//                   (SELECT name FROM tb_category WHERE id = (SELECT category_id FROM tb_store_product_detail WHERE product_id = b.product_id)) AS category,
//                   (SELECT name FROM tb_product WHERE id = b.product_id ) AS name,
//                   (SELECT qty FROM tb_product_import_detail WHERE product_import_id = a.id AND product_id = b.product_id) AS qty,
//                   (SELECT price FROM tb_product_import_detail WHERE product_import_id = a.id AND product_id = b.product_id) AS price,
//                   (SELECT qty * price FROM tb_product_import_detail WHERE product_import_id = a.id AND product_id = b.product_id) AS total
//            FROM tb_product_import a
//            JOIN tb_product_import_detail b ON a.id = b.product_import_id
//            WHERE store_id = #{storeId}
//              ORDER BY date DESC
//              LIMIT #{pageSize}
//              OFFSET #{pageSize} * (#{pageNumber} -1);
//            """)
//    List<ImportHistory> getProductImportHistoryASC(Integer pageNumber, Integer pageSize, Integer storeId);
    @Select("""
            SELECT tb_product_import_detail.product_id AS id,
                               (SELECT id FROM tb_product_import WHERE id = tb_product_import_detail.product_import_id) AS importId,
                               (SELECT imported_date FROM tb_product_import WHERE id = tb_product_import_detail.product_import_id) AS date,
                               (SELECT name FROM tb_category WHERE id = tb_product_import_detail.category_id) AS category,
                               (SELECT name FROM tb_product WHERE id =  tb_product_import_detail.product_id) AS name,
                               qty,
                               price,
                               (qty * price) AS total
                        FROM tb_product_import_detail
                        JOIN tb_product_import tpi ON tpi.id = tb_product_import_detail.product_import_id
            WHERE store_id = #{storeId}
            ORDER BY date ASC
            LIMIT #{pageSize}
              OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    List<ImportHistory> getProductImportHistoryASC(Integer pageNumber, Integer pageSize, Integer storeId);

//    @Select("""
//            SELECT a.id,
//                   imported_date AS date,
//                   category_id,
//                   tpid.product_id,
//                   a.store_id
//            FROM tb_product_import a
//                     JOIN tb_store_product_detail b ON a.store_id = b.store_id
//                     JOIN tb_product_import_detail tpid ON a.id = tpid.product_import_id
//            WHERE a.store_id = #{storeId}
//              AND tpid.product_import_id IN (a.id)
//              ORDER BY date DESC
//              LIMIT #{pageSize}
//              OFFSET #{pageSize} * (#{pageNumber} -1);;
//            """)
//    @Result(property = "category", column = "{id=category_id,test=product_id}", one = @One(select = "getCategoryNameById"))
//    @Result(property = "name", column = "product_id", one = @One(select = "getProductNameByid"))
//    @Result(property = "qty", column = "{productId=product_id , date=date, storeId=store_id}", one = @One(select = "com.henheang.stock_flow_commerce.repository.HistoryRepository.getImportDetailQty"))
//    @Result(property = "price", column = "{productId=product_id , date=date, storeId=store_id}", one = @One(select = "com.henheang.stock_flow_commerce.repository.HistoryRepository.getImportDetailPrice"))
//    @Result(property = "total", column = "{productId=product_id , date=date, storeId=store_id}", one = @One(select = "com.henheang.stock_flow_commerce.repository.HistoryRepository.getImportDetailTotal"))
//    List<ImportHistory> getProductImportHistoryDESC(Integer pageNumber, Integer pageSize, Integer storeId);
    @Select("""
            SELECT tb_product_import_detail.product_id AS id,
                   (SELECT id FROM tb_product_import WHERE id = tb_product_import_detail.product_import_id) AS importId,
                   (SELECT imported_date FROM tb_product_import WHERE id = tb_product_import_detail.product_import_id) AS date,
                   (SELECT name FROM tb_category WHERE id = (SELECT category_id FROM tb_store_product_detail WHERE id = tb_product_import_detail.product_id)) AS category,
                   (SELECT name FROM tb_product WHERE id = (SELECT product_id FROM tb_store_product_detail WHERE id = tb_product_import_detail.product_id)) AS name,
                   qty,
                   price,
                   (qty * price) AS total
            FROM tb_product_import_detail
            JOIN tb_product_import tpi ON tpi.id = tb_product_import_detail.product_import_id
            WHERE store_id = #{storeId}
            ORDER BY date DESC
            LIMIT #{pageSize}
              OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    List<ImportHistory> getProductImportHistoryDESC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT coalesce(qty, 0)
            FROM tb_product_import_detail
                     JOIN tb_product_import tpi ON tb_product_import_detail.product_import_id = tpi.id
            WHERE product_id = #{productId}
              AND imported_date = #{date}
              AND store_id = #{storeId}
            """)
    Integer getImportDetailQty(Integer productId, Date date, Integer storeId);
    @Select("""
            SELECT coalesce(price, 0)
            FROM tb_product_import_detail
                     JOIN tb_product_import tpi ON tb_product_import_detail.product_import_id = tpi.id
            WHERE product_id = #{productId}
              AND imported_date = #{date}
              AND store_id = #{storeId}
            """)
    Double getImportDetailPrice(Integer productId, Date date, Integer storeId);
    @Select("""
            SELECT (qty * price)
            FROM tb_product_import_detail
                     JOIN tb_product_import tpi ON tb_product_import_detail.product_import_id = tpi.id
            WHERE product_id = #{productId}
              AND imported_date = #{date}
              AND store_id = #{storeId}
            """)
    Double getImportDetailTotal(Integer productId, Date date, Integer storeId);

    @Select("""
            SELECT name
            FROM tb_category
            WHERE id = #{id};
            """)
    String getCategoryNameById(Integer id, Integer test);

    @Select("""
            SELECT name
            FROM tb_product
            WHERE id = #{id};
            """)
    String getProductNameByid(Integer id);

    @Select("""
            SELECT count(*) FROM tb_product_import_detail a
            JOIN tb_product_import b ON a.product_import_id = b.id
            WHERE store_id = #{storeId};
            """)
    Integer findTotalImportDetail(Integer storeId);

    @Select("""
            SELECT count(*) FROM tb_order
            JOIN tb_order_detail tod ON tb_order.id = tod.order_id
            WHERE store_id = #{storeId}
            AND status_id IN (5, 6);
            """)
    Integer findTotalOrderHistory(Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id IN (5, 6)
            ORDER BY date ASC
            LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    List<OrderDetailHistory> getOrderHistoryASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT tspd.id                                                  AS productId,
                   (SELECT name FROM tb_product WHERE id = tspd.product_id) AS productName,
                   image,
                   tspd.qty                                                 AS inStock,
                   a.qty                                                    AS qty,
                   a.unit_price                                               AS unitPrice,
                   (a.qty * a.unit_price)                                     AS subTotal
            FROM tb_order_detail AS a
                     JOIN tb_store_product_detail tspd ON a.store_product_id = tspd.id
            WHERE order_id = #{orderId};
            """)
    ProductOrder getProductOrderForOrderDetail(Integer orderId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE id = #{id};
            """)
    @Result(property = "retailerId", column = "retailerId")
    @Result(property = "id", column = "id")
    @Result(property = "name", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerNameById"))
    @Result(property = "image", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerImageById"))
    @Result(property = "address", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerAddressById"))
    @Result(property = "total", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getTotalOrderAmount"))
    @Result(property = "status", column = "status_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getStatusByStatusId"))
    OrderHistory getOrderByOrderId(Integer orderId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id IN (5, 6)
            ORDER BY date DESC
            LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    List<OrderDetailHistory> getOrderHistoryDESC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT COUNT(*)
            FROM tb_order
                     JOIN tb_order_detail tod ON tb_order.id = tod.order_id
            WHERE retailer_account_id = #{currentUserId}
              AND status_id IN (5, 6);
            """)
    Integer findTotalRetailerOrder(Integer currentUserId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{currentUserId}
            AND status_id IN (5, 6)
            ORDER BY date ASC
            LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getRetailerOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    List<OrderDetailHistory> getRetailerOrderHistoryASC(Integer pageNumber, Integer pageSize, Integer currentUserId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{currentUserId}
            AND status_id IN (5, 6)
            ORDER BY date DESC
            LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getRetailerOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    List<OrderDetailHistory> getRetailerOrderHistoryDESC(Integer pageNumber, Integer pageSize, Integer currentUserId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE id = #{id};
            """)
    @Result(property = "retailerId", column = "retailerId")
    @Result(property = "storeId", column = "storeId")
    @Result(property = "id", column = "id")
    @Result(property = "name", column = "storeId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreNameById"))
    @Result(property = "image", column = "storeId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreImageByStoreId"))
    @Result(property = "address", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerAddressById"))
    @Result(property = "total", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getTotalOrderAmount"))
    @Result(property = "status", column = "status_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getStatusByStatusId"))
    OrderHistory getRetailerOrderByOrderId(Integer orderId);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE retailer_account_id = #{currentUserId}
            AND status_id = 8;
            """)
    Integer findTotalRetailerDraft(Integer currentUserId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{currentUserId}
            AND status_id IN (8)
            ORDER BY date ASC
            LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getRetailerOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    List<OrderDetailHistory> getRetailerDraftASC(Integer pageNumber, Integer pageSize, Integer currentUserId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{currentUserId}
            AND status_id IN (8)
            ORDER BY date DESC
            LIMIT #{pageSize} OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getRetailerOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    List<OrderDetailHistory> getRetailerDraftDESC(Integer pageNumber, Integer pageSize, Integer currentUserId);

    @Select("""
            SELECT EXISTS(SELECT *
                        FROM tb_order
                        WHERE id = #{id}
                        AND status_id = 8);
            """)
    boolean checkDraftById(Integer id);

    @Select("""
            DELETE FROM tb_order
            WHERE id = #{id}
            RETURNING id;
            """)
    Integer deleteDraftById(Integer id);

    @Select("""
            UPDATE tb_order
            SET status_id = 1
            WHERE id = #{id}
            RETURNING id;
            """)
    Integer updateDraftById(Integer id);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{currentUserId}
            AND id = #{id}
            AND status_id IN (1)
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "getRetailerOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "getProductOrderForOrderDetail"))
    OrderDetailHistory getDraftHistory(Integer id, Integer currentUserId);
}
