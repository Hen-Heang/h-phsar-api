package com.henheang.hphsar.model.history;

import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.product.ProductOrder;
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
