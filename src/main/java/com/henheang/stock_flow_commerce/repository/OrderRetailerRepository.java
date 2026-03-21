package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.Cart.CartSummery;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderRetailerRepository {

    @Select("""
            SELECT EXISTS(
                SELECT *
                FROM tb_order
                WHERE store_id = #{storeId}
                AND retailer_account_id = #{retailerId}
                AND status_id = 9);
            """)
    Boolean checkForCart(Integer storeId, Integer retailerId);

    @Select("""
            SELECT EXISTS(
                SELECT *
                FROM tb_order
                WHERE store_id = #{storeId}
                AND retailer_account_id = #{retailerId}
                AND status_id IN (8, 9));
            """)
    Boolean checkForCartOrPending(Integer storeId, Integer retailerId);

    @Select("""
            INSERT INTO tb_order
            VALUES (DEFAULT, #{retailerId}, #{storeId}, 9, DEFAULT, DEFAULT)
            RETURNING id;
            """)
    Integer createCart(Integer storeId, Integer retailerId);

    @Select("""
            SELECT id FROM tb_order
            WHERE retailer_account_id = #{retailerId}
            AND store_id = #{storeId}
            AND status_id = 9;
            """)
    Integer getOrderIdByStoreIdAndRetailerId(Integer storeId, Integer retailerId);

    @Select("""
            SELECT exists(SELECT * FROM tb_store_product_detail WHERE id = #{productId} AND qty >= #{qty})
            """)
    Boolean checkStock(Integer productId, Integer qty);

    @Select("""
            SELECT price FROM tb_store_product_detail WHERE id = #{productId};
            """)
    Double getProductPrice(Integer productId);

    @Select("""
            INSERT INTO tb_order_detail(id, order_id, qty, unit_price, store_product_id)
            VALUES (DEFAULT, #{orderId}, #{qty}, #{price}, #{storeProductId})
            RETURNING 1;
            """)
    String addProductToCart(Integer orderId, Integer storeProductId, Integer qty, Double price);

    @Select("""
            SELECT s.id                                                  AS productId,
                   (SELECT name FROM tb_product WHERE id = s.product_id) AS productName,
                   s.image,
                   s.qty                                                 AS inStock,
                   o.qty                                                 AS qty,
                   s.price                                               AS unitPrice,
                   (o.qty * o.unit_price)                                AS subTotal
            FROM tb_order_detail o
                     JOIN tb_store_product_detail s ON s.id = o.store_product_id
            WHERE o.order_id = #{orderId}
            AND o.store_product_id = #{storeProductId};
            """)
    ProductOrder getProductFromCart(Integer orderId, Integer storeProductId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order_detail WHERE store_product_id = #{storeProductId} AND order_id = #{orderId});
            """)
    boolean productIsInCart(Integer storeProductId, Integer orderId);

    @Select("""
            UPDATE tb_order_detail
            SET qty = #{qty}, unit_price = #{price}
            WHERE order_id = #{orderId}
            AND store_product_id = #{storeProductId}
            Returning 1;
            """)
    String updateProductQtyFromCart(Integer storeProductId, Integer orderId, Integer qty, Double price);

    @Select("""
            DELETE FROM tb_order_detail
            WHERE store_product_id = #{storeProductId}
            AND order_id = #{orderId}
            RETURNING 1;
            """)
    String removeProductFromCart(Integer storeProductId, Integer orderId);

//    @Select("""
//            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
//                        FROM tb_order
//                        WHERE id = #{orderId};
//            """)
//    @Result(property = "order", column = "id",
//            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderRetailerRepository.getOrderByOrderId"))
////    @Result(property = "products", column = "id",
////            many = @Many(select = "com.henheang.stock_flow_commerce.repository.OrderRetailerRepository.getProductOrderByOrderIdASC",
////                    fetchType = FetchType.LAZY))
//    Order viewCartDetailASC(Integer pageNumber, Integer pageSize, Integer orderId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE id = #{id};
            """)
    @Result(property = "retailerId", column = "retailerId")
    @Result(property = "storeId", column = "storeId")
    @Result(property = "id", column = "id")
    @Result(property = "name", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerNameById"))
    @Result(property = "address", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerAddressById"))
    @Result(property = "total", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getTotalOrderAmount"))
    @Result(property = "status", column = "status_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getStatusByStatusId"))
    @Result(property = "retailerImage", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerImageById"))
    @Result(property = "storeImage", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreImageById"))
    @Result(property = "storeName", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreNameById"))
    @Result(property = "storeAddress", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreAddressById"))
    @Result(property = "storePrimaryPhone", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStorePrimaryPhoneById"))
    @Result(property = "storeAdditionalPhone", column = "storeId", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "retailerPhone", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerPhoneById"))
    @Result(property = "retailerEmail", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerEmailById"))
    @Result(property = "storeEmail", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreEmailByStoreId"))
    Order getOrderByOrderId(Integer id);

    @Select("""
            SELECT s.id                                                  AS productId,
                   (SELECT name FROM tb_product WHERE id = s.product_id) AS productName,
                   s.image,
                   s.qty                                                 AS inStock,
                   o.qty                                                 AS qty,
                   s.price                                               AS unitPrice,
                   (o.qty * o.unit_price)                                AS subTotal
            FROM tb_order_detail o
                     JOIN tb_store_product_detail s ON s.id = o.store_product_id
            WHERE o.order_id = #{orderId}
            LIMIT #{pageSize}
            OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    List<ProductOrder> getProductOrderByOrderId(Integer orderId, @Param("pageNumber") Integer pageNumber, @Param("pageSize") Integer pageSize);
    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND status_id = 9);
            """)
    boolean isCartExist(Integer orderId);

    @Select("""
            SELECT count(*) FROM tb_order_detail
            WHERE order_id = #{orderId};
            """)
    Integer getTotalProduct(Integer orderId);


    @Select("""
            DELETE FROM tb_order
            WHERE id = #{orderId}
            AND status_id = 9
            RETURNING 1;
            """)
    String cancelCart(Integer orderId);

    @Select("""
            UPDATE tb_order
            SET status_id = 8
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String saveToDraft(Integer orderId);

    @Select("""
            UPDATE tb_order
            SET status_id = 1
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String confirmOrder(Integer orderId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{retailerId}
            AND status_id IN (1,2,3,4,5,6)
            ORDER BY created_date ASC
                        LIMIT #{pageSize}
                        OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "retailerId", column = "retailerId")
    @Result(property = "storeId", column = "storeId")
    @Result(property = "id", column = "id")
    @Result(property = "name", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerNameById"))
    @Result(property = "address", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerAddressById"))
    @Result(property = "total", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getTotalOrderAmount"))
    @Result(property = "status", column = "status_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getStatusByStatusId"))
    @Result(property = "retailerImage", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerImageById"))
    @Result(property = "storeImage", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreImageById"))
    @Result(property = "storeName", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreNameById"))
    @Result(property = "storeAddress", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreAddressById"))
    @Result(property = "storePrimaryPhone", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStorePrimaryPhoneById"))
    @Result(property = "storeAdditionalPhone", column = "storeId", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "retailerPhone", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerPhoneById"))
    @Result(property = "retailerEmail", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerEmailById"))
    @Result(property = "storeEmail", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreEmailByStoreId"))
    List<Order> getUserOrderActivitiesOrderByDateASC(Integer retailerId, Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE retailer_account_id = #{retailerId}
            AND status_id IN (1,2,3,4,5,6)
            ORDER BY created_date DESC
                        LIMIT #{pageSize}
                        OFFSET #{pageSize} * (#{pageNumber} -1);
            """)
    @Result(property = "retailerId", column = "retailerId")
    @Result(property = "storeId", column = "storeId")
    @Result(property = "id", column = "id")
    @Result(property = "name", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerNameById"))
    @Result(property = "address", column = "retailerId",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerAddressById"))
    @Result(property = "total", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getTotalOrderAmount"))
    @Result(property = "status", column = "status_id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getStatusByStatusId"))
    @Result(property = "retailerImage", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerImageById"))
    @Result(property = "storeImage", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreImageById"))
    @Result(property = "storeName", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreNameById"))
    @Result(property = "storeAddress", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreAddressById"))
    @Result(property = "storePrimaryPhone", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStorePrimaryPhoneById"))
    @Result(property = "storeAdditionalPhone", column = "storeId", many = @Many(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getAdditionalPhone"))
    @Result(property = "retailerPhone", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerPhoneById"))
    @Result(property = "retailerEmail", column = "retailerId", one = @One(select = "com.henheang.stock_flow_commerce.repository.RetailerProfileRepository.getRetailerEmailById"))
    @Result(property = "storeEmail", column = "storeId", one = @One(select = "com.henheang.stock_flow_commerce.repository.StoreRepository.getStoreEmailByStoreId"))
    List<Order> getUserOrderActivitiesOrderByDateDESC(Integer retailerId, Integer pageNumber, Integer pageSize);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE retailer_account_id = #{retailerId}
            AND status_id IN (1,2,3,4,5,6);
            """)
    Integer getTotalOrder(Integer retailerId);

    @Select("""
            SELECT exists(SELECT * FROM tb_order WHERE id = #{id} AND status_id = 4);
            """)
    boolean orderIsConfirming(Integer id);

    @Select("""
            SELECT exists(SELECT * FROM tb_order WHERE id = #{id} AND status_id IN (1, 2, 3, 4, 5, 6));
            """)
    boolean checkOrderExist(Integer id);

    @Select("""
            UPDATE tb_order
            SET status_id = 5
            WHERE id = #{id}
            RETURNING 1;
            """)
    String confirmTransaction(Integer id);

    @Select("""
            SELECT exists(SELECT * FROM tb_order WHERE id = #{id} AND status_id = 5);
            """)
    boolean checkOrderIsComplete(Integer id);

    @Select("""
            SELECT id FROM tb_store_product_detail
            WHERE store_id = #{storeId}
            AND id = #{productId};
            """)
    Integer getStoreProductId(Integer storeId, Integer productId);

    @Select("""
            DELETE FROM tb_order_detail
            WHERE order_id = #{orderId}
            RETURNING 1;
            """)
    String deleteOrderDetail(Integer orderId);

    @Select("""
            DELETE FROM tb_order
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String deleteOrder(Integer orderId);

    @Select("""
            SELECT exists(SELECT * FROM tb_retailer_info WHERE retailer_account_id = #{retailerId});
            """)
    boolean checkUserInfo(Integer retailerId);

    @Select("""
            SELECT EXISTS(SELECT *
                          FROM tb_order
                          WHERE status_id = 9
                            AND retailer_account_id = #{currentUserId});
            """)
    boolean checkForAnyCart(Integer currentUserId);

    @Select("""
            SELECT id,
                   store_id                                                                   AS storeId,
                   (SELECT name FROM tb_store WHERE id = tb_order.store_id)                   AS storeName,
                   (SELECT banner_image FROM tb_store WHERE id = tb_order.store_id)           AS storeImage,
                   (SELECT SUM(unit_price) FROM tb_order_detail WHERE order_id = tb_order.id) AS total
            FROM tb_order
            WHERE status_id = 9
              AND retailer_account_id = #{currentUserId};
            """)
    List<CartSummery> getAllCarts(Integer currentUserId);

    @Select("""
            SELECT id FROM tb_order
            WHERE retailer_account_id = #{retailerId}
            AND status_id = 9;
            """)
    Integer getUserCartId(Integer retailerId);

    @Select("""
            SELECT store_id FROM tb_order
            WHERE id = #{cartId};
            """)
    Integer getStoreIdByOrderId(Integer cartId);

    @Select("""
            SELECT EXISTS(SELECT *
                          FROM tb_order
                          WHERE status_id = 9
                            AND retailer_account_id = #{retailerId}
                            AND store_id != #{storeId});
            """)
    boolean checkForCartInOtherStore(Integer storeId, Integer retailerId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE retailer_account_id = #{currentUserId} AND status_id = 3 AND id = #{id});
            """)
    boolean checkForDispatchingOrder(Integer id, Integer currentUserId);
}
