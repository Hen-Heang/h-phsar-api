package com.henheang.stock_flow_commerce.model.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartOrder {
    private Integer productId;
    private Integer qty;
}
