package com.henheang.stock_flow_commerce.model.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummery {
    private Integer id;
    private Integer storeId;
    private String storeName;
    private String storeImage;
    private Double total;
}
