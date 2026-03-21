package com.henheang.stock_flow_commerce.model.distributor.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributorReport {

    List<String> months;
    List <Integer> totalOrder;
    private Double totalSale;
}
