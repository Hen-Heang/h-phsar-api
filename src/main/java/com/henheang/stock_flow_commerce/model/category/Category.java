package com.henheang.stock_flow_commerce.model.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {
   private Integer id;
   private String name;
   private String createdDate;
   private String updatedDate;
}
