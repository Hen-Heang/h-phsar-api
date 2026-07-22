package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.buyer.report.CategoryNameAndTotalOfQty;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BuyerReportRepository {

    Integer getTotalMonthlyOrderByCurrentMonth(Integer currentUserId, Integer year, Integer month);

    Integer getTotalRejectedAndAccepted(Integer currentUserId, Integer statusId, Integer year, Integer month);

    Integer getTotalOrderFromDifferentYear(Integer currentUserId,Integer year, Integer month);

    Integer getTotalAcceptedAndRejectedFromDifferentYear(Integer currentUserId, Integer statusId,Integer year, Integer month );

    Integer getTotalQuantityOrder(Integer currentUserId, Integer statusId, Integer year, Integer month);

    List<String> getCategoryNameOrder(Integer currentUserId, Integer statusId, Integer year, Integer month);

    List<Integer> getPurchasedShopOrdered(Integer currentUserId, Integer statusId, Integer year, Integer month);

    Integer getTotalExpense(Integer currentUserId, Integer statusId, Integer year, Integer month);

    Integer getTotalRatingStore(Integer currentUserId, Integer year, Integer month);

    Double getTotalYearlyExpense(Integer currentUserId, Integer statusId, Integer year);

    Integer getTotalQuantityInDifferenceYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    List<String> getCategoryNameOrderIndDifferentYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    List<Integer> getTotalPurchasedShopDifferent(Integer currentUserId, Integer statusId, Integer year, Integer month);

    Integer getTotalExpenseInDifferentYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    Integer getRatingInDifferentYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    Double getTotalYearlyInDifferentYear(Integer currentUserId, Integer statusId, Integer year);

    List<Integer> getTotalQtyInEachCategory(Integer currentUserId, Integer statusId, Integer year, Integer month);

    List<CategoryNameAndTotalOfQty> getCategoryNameAndTotalItem(Integer currentUserId, Integer statusId, Integer year, Integer month);
}