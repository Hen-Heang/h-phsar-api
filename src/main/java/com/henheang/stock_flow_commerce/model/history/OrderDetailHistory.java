package com.henheang.stock_flow_commerce.model.history;

import com.henheang.stock_flow_commerce.model.order.Order;
import com.henheang.stock_flow_commerce.model.product.ProductOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailHistory {
    private OrderHistory order;
    private List<ProductOrder> products;
}
