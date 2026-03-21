package com.henheang.stock_flow_commerce.model.retailer.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RetailerReport {

    private Integer totalOrder;
    private List<Integer> totalRejectedAndAccepted;
    private Integer totalQuantityOrder;
    private List<String> categoryNameOrdered;
    private Integer totalPurchasedShop;
    private Integer totalExpenseOrdered;
    private Integer totalRatingShop;
    private Double averageMonthlyExpense;
    private Double totalYearlyExpense;
    private List<Integer>totalExpenseInEachMonth;
    private List<String>monthAndYearLabel;
    private List<Integer>totalQtyEachCategory;




}


