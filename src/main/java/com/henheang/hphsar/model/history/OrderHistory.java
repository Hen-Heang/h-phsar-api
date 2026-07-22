package com.henheang.hphsar.model.history;

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
    private Integer buyerId;
    private String name; // buyer name
    private String image;
    private String address; // buyer address
    private String date; // order date
    private Double total; // grand total price
    private String status;
}
