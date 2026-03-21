package com.henheang.stock_flow_commerce.model.Cart;

import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart {
    private Order order;
    private List<ProductOrder> products;
}
