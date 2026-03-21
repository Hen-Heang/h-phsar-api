package com.henheang.stock_flow_commerce.service;
import com.henheang.stock_flow_commerce.model.distributor.DistributorHomepage;
import com.henheang.stock_flow_commerce.model.order.OrderChartByMonth;
import com.henheang.stock_flow_commerce.model.order.OrderChartByYear;

import java.text.ParseException;

public interface DistributorHomepageService {

    DistributorHomepage getNewOrder(Integer currentUserId);

    OrderChartByMonth getTotalByMonth(Integer currentUserId , String startDate, String endDate) throws ParseException;

//    OrderChartByYear getTotalByYear(Integer currentUserId, String startDate, String endDated) throws ParseException;
}
