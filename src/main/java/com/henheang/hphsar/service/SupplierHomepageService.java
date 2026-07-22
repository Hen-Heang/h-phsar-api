package com.henheang.hphsar.service;
import com.henheang.hphsar.model.supplier.SupplierHomepage;
import com.henheang.hphsar.model.order.OrderChartByMonth;

public interface SupplierHomepageService {

    SupplierHomepage getNewOrder(Integer currentUserId);

    OrderChartByMonth getTotalByMonth(Integer currentUserId, String startDate, String endDate);
}
