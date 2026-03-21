package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.invoice.Invoice;
import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.order.OrderDetail;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderDistributorRepository {

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id IN (1, 2, 3, 4, 5)
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
    List<Order> getAllOrderCurrentUserSortByCreatedDateASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id IN (1, 2, 3, 4, 5)
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
    List<Order> getAllOrderCurrentUserSortByCreatedDateDESC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 1
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
    List<Order> getNewOrderCurrentUserSortByCreatedDateASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 1
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
    List<Order> getNewOrderCurrentUserSortByCreatedDateDESC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 2
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
    List<Order> getPreparingOrderCurrentUserSortByCreatedDateASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 2
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
    List<Order> getPreparingOrderCurrentUserSortByCreatedDateDESC(Integer pageNumber, Integer pageSize, Integer storeId);


    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 3
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
    List<Order> getDispatchingOrderCurrentUserSortByCreatedDateASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 3
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
    List<Order> getDispatchingOrderCurrentUserSortByCreatedDateDESC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 4
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
    List<Order> getConfirmingOrderCurrentUserSortByCreatedDateASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 4
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
    List<Order> getConfirmingOrderCurrentUserSortByCreatedDateDESC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 5
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
    List<Order> getCompleteOrderCurrentUserSortByCreatedDateASC(Integer pageNumber, Integer pageSize, Integer storeId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 5
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
    List<Order> getCompleteOrderCurrentUserSortByCreatedDateDESC(Integer pageNumber, Integer pageSize, Integer storeId);


    @Select("""
            SELECT count(*) FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 2;
            """)
    Integer getCurrentStoreTotalPreparingOrder(Integer storeId);

    @Select("""
            SELECT sum(unit_price * qty) FROM tb_order_detail
            WHERE order_id = #{orderId};
            """)
    Double getTotalOrderAmount(Integer orderId);


    @Select("""
            SELECT name FROM tb_status
            WHERE id = #{statusId};
            """)
    String getStatusByStatusId(Integer statusId);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE store_id = #{storeId};
            """)
    Integer getCurrentStoreTotalOrder(Integer storeId);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 1;
            """)
    Integer getCurrentStoreTotalNewOrder(Integer storeId);


    @Select("""
            SELECT count(*) FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 3;
            """)
    Integer getTotalDispatchingOrder(Integer storeId);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 4;
            """)
    Integer getTotalConfirmingOrder(Integer storeId);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE store_id = #{storeId}
            AND status_id = 5;
            """)
    Integer getTotalCompleteOrder(Integer storeId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND store_id = #{storeId});
            """)
    Boolean checkOrderExist(Integer orderId, Integer storeId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND status_id = 1);
            """)
    Boolean checkForPendingOrder(Integer orderId);

    @Select("""
            UPDATE tb_order
            SET status_id = 2
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String acceptPendingOrder(Integer orderId);

    @Select("""
            UPDATE tb_order
            SET status_id = 6
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String declinePendingOrder(Integer orderId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND status_id = 2);
            """)
    Boolean checkForPreparingOrder(Integer orderId);

    @Select("""
            UPDATE tb_order
            SET status_id = 3
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String finishPreparing(Integer orderId);

    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND status_id = 3);
            """)
    Boolean checkForDispatchOrder(Integer orderId);

    @Select("""
            UPDATE tb_order
            SET status_id = 4
            WHERE id = #{orderId}
            RETURNING 1;
            """)
    String orderDelivered(Integer orderId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE id = #{id};
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getProductOrderForOrderDetail"))
    OrderDetail getOrderDetailsByOrderId(Integer id);

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
    ProductOrder getProductOrder(Integer orderId);

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
            SELECT p.id                                                              AS productId,
                   p.name                                                            AS productName,
                   p.image,
                   (SELECT qty FROM tb_store_product_detail WHERE product_id = p.id) AS inStock,
                   od.qty,
                   od.unit_price                                                     AS unitPrice,
                   (od.qty * od.unit_price)                                          AS subTotal
            FROM tb_order_detail od
                     JOIN tb_product p ON od.product_id = p.id
            WHERE order_id = #{orderId};
            """)
    List<ProductOrder> getProductOrderByOrderId(Integer orderId);

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
    Order getOrderByOrderId(Integer orderId);


    @Select("""
            SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND status_id = 5);
            """)
    Boolean checkForCompleteOrder(Integer orderId);

    @Select("""
            SELECT id, store_id AS storeId, retailer_account_id AS retailerId, created_date AS date, status_id
            FROM tb_order
            WHERE id = #{id};
            """)
    @Result(property = "order", column = "id",
            one = @One(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getOrderByOrderId"))
    @Result(property = "products", column = "id",
            many = @Many(select = "com.henheang.stock_flow_commerce.repository.OrderDistributorRepository.getProductOrder"))
    Invoice getInvoiceByOrderId(Integer orderId);

    @Select("""
            SELECT count(*)
            FROM tb_store_product_detail AS S
                     JOIN tb_order_detail O ON S.id = O.store_product_id
            WHERE O.id IN (SELECT id FROM tb_order_detail WHERE order_id = #{orderId})
              AND s.qty >= O.qty;
            """)
    Integer productEligibleCount(Integer orderId);

    @Select("""
            SELECT count(*) FROM tb_order_detail WHERE order_id = #{orderId};
            """)
    Integer getProductDetailCount(Integer orderId);

    @Update("""
            UPDATE tb_store_product_detail
            SET qty = qty - (SELECT qty FROM tb_order_detail WHERE order_id = #{orderId} AND tb_order_detail.store_product_id = #{productId})
            WHERE id = (SELECT store_product_id FROM tb_order_detail WHERE order_id = #{orderId}
              AND tb_order_detail.store_product_id = #{productId})
              AND store_id = #{storeId};
            """)
    void deductStock(Integer orderId, Integer productId, Integer storeId);

    @Select("""
            SELECT store_product_id FROM tb_order_detail
            WHERE order_id = #{orderId};
            """)
    List<Integer> getAllProductIdFromProductDetails(Integer orderId);

    @Select("""
            SELECT distributor_account_id
            FROM tb_store
            WHERE id = (SELECT store_id FROM tb_order WHERE id = #{cartId});
            """)
    Integer getDistributorIdByOrderId(Integer cartId);
}
