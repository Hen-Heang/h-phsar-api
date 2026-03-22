package com.henheang.hphsar.service;
import com.henheang.hphsar.model.distributor.DistributorHomepage;
import com.henheang.hphsar.model.order.OrderChartByMonth;
import com.henheang.hphsar.model.order.OrderChartByYear;

import java.text.ParseException;

public interface DistributorHomepageService {

    DistributorHomepage getNewOrder(Integer currentUserId);

    OrderChartByMonth getTotalByMonth(Integer currentUserId , String startDate, String endDate) throws ParseException;

//    OrderChartByYear getTotalByYear(Integer currentUserId, String startDate, String endDated) throws ParseException;
}
