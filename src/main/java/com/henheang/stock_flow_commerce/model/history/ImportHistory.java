package com.henheang.stock_flow_commerce.model.history;

import com.henheang.stock_flow_commerce.model.category.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportHistory {
    private Integer id; // import product
    private Integer importId;
    private String date;
    private String category;
    private String name; //product name
    private Integer qty;
    private Double price; //import price
    private Double total; //total price
}
