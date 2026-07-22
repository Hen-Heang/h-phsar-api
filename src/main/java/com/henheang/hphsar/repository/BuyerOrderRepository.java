package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.Cart.CartSummery;
import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.product.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuyerOrderRepository {

    Boolean checkForCart(Integer storeId, Integer buyerId);

    Boolean checkForCartOrPending(Integer storeId, Integer buyerId);

    Integer createCart(Integer storeId, Integer buyerId);

    Integer getOrderIdByStoreIdAndBuyerId(Integer storeId, Integer buyerId);

    Boolean checkStock(Integer productId, Integer qty);

    Double getProductPrice(Integer productId);

    String addProductToCart(Integer orderId, Integer storeProductId, Integer qty, Double price);

    ProductOrder getProductFromCart(Integer orderId, Integer storeProductId);

    boolean productIsInCart(Integer storeProductId, Integer orderId);

    String updateProductQtyFromCart(Integer storeProductId, Integer orderId, Integer qty, Double price);

    String removeProductFromCart(Integer storeProductId, Integer orderId);

    Order getOrderByOrderId(Integer id);

    List<ProductOrder> getProductOrderByOrderId(Integer orderId, @Param("pageNumber") Integer pageNumber, @Param("pageSize") Integer pageSize);

    boolean isCartExist(Integer orderId);

    Integer getTotalProduct(Integer orderId);

    String cancelCart(Integer orderId);

    String saveToDraft(Integer orderId);

    String confirmOrder(Integer orderId);

    List<Order> getUserOrderActivities(String sort, Integer buyerId, Integer pageNumber, Integer pageSize);

    Integer getTotalOrder(Integer buyerId);

    boolean orderIsConfirming(Integer id);

    boolean checkOrderExist(Integer id);

    // Ownership-scoped existence check — prevents one buyer from acting on another buyer's order (IDOR fix).
    boolean checkOrderExistForBuyer(Integer id, Integer buyerId);

    String confirmTransaction(Integer id);

    boolean checkOrderIsComplete(Integer id);

    Integer getStoreProductId(Integer storeId, Integer productId);

    String deleteOrderDetail(Integer orderId);

    String deleteOrder(Integer orderId);

    boolean checkUserInfo(Integer buyerId);

    boolean checkForAnyCart(Integer currentUserId);

    List<CartSummery> getAllCarts(Integer currentUserId);

    Integer getUserCartId(Integer buyerId);

    Integer getStoreIdByOrderId(Integer cartId);

    boolean checkForCartInOtherStore(Integer storeId, Integer buyerId);

    boolean checkForDispatchingOrder(Integer id, Integer currentUserId);
}