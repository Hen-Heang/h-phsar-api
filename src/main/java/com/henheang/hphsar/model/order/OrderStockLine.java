package com.henheang.hphsar.model.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One order line item's stock-deduction target: which store-specific
 * inventory row ({@code tb_store_product_detail.id}) to deduct from, and how
 * much. Deliberately separate from {@link com.henheang.hphsar.model.product.ProductOrder}
 * (a display/UI model) — this is only used internally to drive atomic stock deduction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStockLine {
    private Integer storeProductId;
    private Integer qty;
}
