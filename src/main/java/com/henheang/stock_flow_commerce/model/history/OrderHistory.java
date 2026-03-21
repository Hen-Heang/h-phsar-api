package com.henheang.stock_flow_commerce.model.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderHistory {
    private Integer id;
    private Integer storeId;
    private Integer retailerId;
    private String name; // retailer name
    private String image;
    private String address; // retailer address
    private String date; // order date
    private Double total; // grand total price
    private String status;
}
