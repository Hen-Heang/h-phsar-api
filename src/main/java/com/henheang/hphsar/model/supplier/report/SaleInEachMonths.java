package com.henheang.hphsar.model.supplier.report;

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
