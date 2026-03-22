package com.henheang.hphsar.model.invoice;

import com.henheang.hphsar.model.order.Order;
import com.henheang.hphsar.model.product.ProductOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    private Order order;
    private List<ProductOrder> products;
}
