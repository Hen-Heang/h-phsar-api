package com.henheang.stock_flow_commerce.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ProductImport {
    private Integer id;
    private Integer qty;
    private Double price;
}
