package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.history.ImportHistory;
import com.henheang.hphsar.model.history.OrderDetailHistory;
import com.henheang.hphsar.model.history.OrderHistory;
import com.henheang.hphsar.model.product.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface HistoryRepository {

    List<ImportHistory> getImportHistory(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    Integer getImportDetailQty(Integer productId, Date date, Integer storeId);

    Double getImportDetailPrice(Integer productId, Date date, Integer storeId);

    Double getImportDetailTotal(Integer productId, Date date, Integer storeId);

    String getCategoryNameById(Integer id, Integer test);

    String getProductNameByid(Integer id);

    Integer findTotalImportDetail(Integer storeId);

    Integer findTotalOrderHistory(Integer storeId);

    List<OrderDetailHistory> getOrderHistory(String sort, Integer pageNumber, Integer pageSize, Integer storeId);

    ProductOrder getProductOrderForOrderDetail(Integer orderId);

    OrderHistory getOrderByOrderId(Integer orderId);

    Integer findTotalBuyerOrder(Integer currentUserId);

    List<OrderDetailHistory> getBuyerOrderHistory(String sort, Integer pageNumber, Integer pageSize, Integer currentUserId);

    OrderHistory getBuyerOrderByOrderId(Integer orderId);

    Integer findTotalBuyerDraft(Integer currentUserId);

    List<OrderDetailHistory> getBuyerDraft(String sort, Integer pageNumber, Integer pageSize, Integer currentUserId);

    // Ownership-scoped: a draft may only be seen/mutated by the buyer it belongs to.
    // The buyer id must come from the authenticated principal in the service layer,
    // never from the request — see HistoryServiceImplV1.
    boolean existsDraftByIdAndBuyerId(@Param("draftId") Integer draftId, @Param("buyerAccountId") Integer buyerAccountId);

    int deleteDraftByIdAndBuyerId(@Param("draftId") Integer draftId, @Param("buyerAccountId") Integer buyerAccountId);

    int submitDraftByIdAndBuyerId(@Param("draftId") Integer draftId, @Param("buyerAccountId") Integer buyerAccountId);

    OrderDetailHistory getDraftHistory(Integer id, Integer currentUserId);
}