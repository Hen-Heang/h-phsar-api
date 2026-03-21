package com.henheang.stock_flow_commerce.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEditRequest {
    private String name;
    private Double price;
    private String image;
    private String description;
    private Integer categoryId;
    private Boolean isPublish;
}