package com.henheang.stock_flow_commerce.model.retailer.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryNameAndTotalOfQty {
    private String categoryName;
    private Integer totalItem;

    public String getCategoryName() {
        return categoryName;
    }

    public int getTotalItem() {
        return totalItem;
    }
}
