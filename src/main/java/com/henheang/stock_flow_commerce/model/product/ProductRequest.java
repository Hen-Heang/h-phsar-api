package com.henheang.stock_flow_commerce.model.product;

import com.henheang.stock_flow_commerce.model.category.CategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private Integer qty;
    private Double price;
    private String image;
    private String description;
    private Integer categoryId;
    private Boolean isPublish;
}