package com.henheang.stock_flow_commerce.model.distributor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistributorTotal {
    private Integer totalProducts;
    private Integer totalOrders;
    private Integer productSold;
    private Integer totalOrderForChart;


}
