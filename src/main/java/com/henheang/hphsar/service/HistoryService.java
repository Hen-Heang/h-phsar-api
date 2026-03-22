package com.henheang.hphsar.service;

import com.henheang.hphsar.model.history.ImportHistory;
import com.henheang.hphsar.model.history.OrderDetailHistory;
import com.henheang.hphsar.model.order.OrderDetail;

import java.text.ParseException;
import java.util.List;

public interface HistoryService {
    List<ImportHistory> getProductImportHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException;
    Integer findTotalImportPage(Integer pageSize);

    List<OrderDetailHistory> getOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    Integer findTotalOrderPage(Integer pageSize);

    List<OrderDetailHistory> getRetailerOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException;

    Integer findRetailerTotalOrderPage(Integer pageSize);

    Integer findRetailerTotalDraftPage(Integer pageSize);

    List<OrderDetailHistory> getDraftHistory(String sort, Integer pageNumber, Integer pageSize);

    String deleteDraftById(Integer id);

    OrderDetailHistory updateDraftById(Integer id) throws ParseException;
}
