package com.henheang.stock_flow_commerce.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOrder {
    private Integer productId;
    private String productName; // product name
    private String image;
    private Integer inStock;
    private Integer qty;
    private Double unitPrice;
    private Double subTotal;
}
