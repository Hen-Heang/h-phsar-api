package com.henheang.hphsar.service;
import com.henheang.hphsar.model.distributor.DistributorHomepage;
import com.henheang.hphsar.model.order.OrderChartByMonth;

public interface DistributorHomepageService {

    DistributorHomepage getNewOrder(Integer currentUserId);

    OrderChartByMonth getTotalByMonth(Integer currentUserId, String startDate, String endDate);
}
