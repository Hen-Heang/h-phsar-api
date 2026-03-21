package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.retailer.report.RetailerReport;

import java.text.ParseException;

public interface RetailerReportService {
    RetailerReport getRetailerMonthlyReport(String startDate, String endDate) throws ParseException;

//    RetailerReport getRetailerMonthlyReport(Timestamp startDate, Timestamp endDate);


}
