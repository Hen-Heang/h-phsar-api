package com.henheang.hphsar.model.supplier.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierReport {

    List<String> months;
    List <Integer> totalOrder;
    private Double totalSale;
}
