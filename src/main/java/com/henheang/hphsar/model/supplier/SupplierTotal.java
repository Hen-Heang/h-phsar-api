package com.henheang.hphsar.model.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierTotal {
    private Integer totalProducts;
    private Integer totalOrders;
    private Integer productSold;
    private Integer totalOrderForChart;


}
