package com.henheang.hphsar.model.order;

import com.henheang.hphsar.model.product.ProductOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    private Order order;
    private List<ProductOrder> products;
}
