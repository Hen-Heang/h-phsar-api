package com.henheang.stock_flow_commerce.model.distributor.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class SaleInEachMonths {

    private String monthsName;
    private Double totalSale;

}
