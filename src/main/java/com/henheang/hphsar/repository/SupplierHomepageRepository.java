package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.supplier.SupplierHomepage;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SupplierHomepageRepository {
    Integer getStoreId(Integer currentUserId);

    SupplierHomepage getHomepageCounts(Integer storeId);

    Integer getNewOrder(Integer storeId);

    Integer getPreparing(Integer storeId);

    Integer getDispatch(Integer storeId);

    Integer getConfirming(Integer storeId);

    Integer getCompleted(Integer storeId);

    Integer getTotalProductImport(Integer storeId, String startDate, String endDate);

    Integer getTotalOrderByMonth(Integer storeId, String startDate, String endDate);

    Integer getTotalProductSold(Integer storeId, String startDate, String endDate);

    Integer getTotalOrderEachMonth(Integer storeId, Integer startMonth, Integer startYear);

    //year
    Integer getTotalOrderByYear(Integer storeId, Integer startYear, Integer endYear);

    Integer getTotalProductImportByYear(Integer storeId, Integer startYear, Integer endYear);

    Integer getTotalProductSoldByYear(Integer storeId, Integer startYear, Integer endYear);

    Integer getTotalOrderEachYear(Integer storeId, Integer startYear);
}