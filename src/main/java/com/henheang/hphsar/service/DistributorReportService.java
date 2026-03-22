package com.henheang.hphsar.service;

import com.henheang.hphsar.controller.distributor.report.DistributorReportController;
import com.henheang.hphsar.model.report.DistributorReport;

import java.text.ParseException;

public interface DistributorReportService {
    DistributorReport getDistributorReport(String startDate, String endDate) throws ParseException;
}
