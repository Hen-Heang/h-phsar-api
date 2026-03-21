package com.henheang.stock_flow_commerce.model.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistributorReport {
    private Double totalExpense;
    private Double totalProfit;
    private Integer totalOrder;
    private List<String> period;
    private List<String> periodName;
    List<Integer> orderPerMonth;
}
