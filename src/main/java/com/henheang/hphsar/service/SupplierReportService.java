package com.henheang.hphsar.service;

import com.henheang.hphsar.controller.supplier.report.SupplierReportController;
import com.henheang.hphsar.model.report.SupplierReport;

import java.text.ParseException;

public interface SupplierReportService {
    SupplierReport getSupplierReport(String startDate, String endDate) throws ParseException;
}
