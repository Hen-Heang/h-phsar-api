package com.henheang.hphsar.service;

import com.henheang.hphsar.model.buyer.report.BuyerReport;

import java.text.ParseException;

public interface BuyerReportService {
    BuyerReport getBuyerMonthlyReport(String startDate, String endDate) throws ParseException;

//    BuyerReport getBuyerMonthlyReport(Timestamp startDate, Timestamp endDate);


}
