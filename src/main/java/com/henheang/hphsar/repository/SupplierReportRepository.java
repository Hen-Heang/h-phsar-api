package com.henheang.hphsar.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SupplierReportRepository {
    Double getTotalExpense(String startDate, String endDate, Integer storeId);

    Double getTotalProfit(String startDate, String endDate, Integer storeId);

    Integer getOrder(String startDate, String endDate, Integer storeId);

    List<String> getPeriod(String startDate, String endDate, Integer storeId);

    Integer getOrderOfMonth(Integer storeId, String startDate, String endDate);
}
