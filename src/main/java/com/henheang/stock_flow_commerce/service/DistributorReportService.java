package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.controller.distributor.report.DistributorReportController;
import com.henheang.stock_flow_commerce.model.report.DistributorReport;

import java.text.ParseException;

public interface DistributorReportService {
    DistributorReport getDistributorReport(String startDate, String endDate) throws ParseException;
}
