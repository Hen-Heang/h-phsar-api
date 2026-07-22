package com.henheang.hphsar.model.order;

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
    private Integer buyerId;
    private String buyerImage;
    private String name; // buyer name
    private String buyerPhone;
    private String buyerEmail;
    private String address; // buyer address
    private String date; // order date
    private Double total; // grand total price
    private String status;
}
