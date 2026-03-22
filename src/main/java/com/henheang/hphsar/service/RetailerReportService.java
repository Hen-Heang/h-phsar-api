package com.henheang.hphsar.service;

import com.henheang.hphsar.model.retailer.report.RetailerReport;

import java.text.ParseException;

public interface RetailerReportService {
    RetailerReport getRetailerMonthlyReport(String startDate, String endDate) throws ParseException;

//    RetailerReport getRetailerMonthlyReport(Timestamp startDate, Timestamp endDate);


}
