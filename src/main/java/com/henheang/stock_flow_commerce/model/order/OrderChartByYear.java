package com.henheang.stock_flow_commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor@Builder

public class OrderChartByYear {
    private Integer totalOrder ;
    private Integer totalProductImport ;
    private Integer totalProductSold ;
    private List<String> year;
    private List<Integer> totalOrderEachYear;

}