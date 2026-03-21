package com.henheang.stock_flow_commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    private Integer id;
    private Integer storeId;
    private String storeName;
    private String storeImage;
    private String storeAddress;
    private String storePrimaryPhone;
    private String storeEmail;
    private List<String> storeAdditionalPhone;
    private Integer retailerId;
    private String retailerImage;
    private String name; // retailer name
    private String retailerPhone;
    private String retailerEmail;
    private String address; // retailer address
    private String date; // order date
    private Double total; // grand total price
    private String status;
}
