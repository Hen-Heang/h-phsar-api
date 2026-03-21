package com.henheang.stock_flow_commerce.model.retailer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Retailer {
    private Integer id;
    private Integer retailerAccountId;
    private String firstName;
    private String lastName;
    private String gender;
    private String address;
    private String primaryPhoneNumber;
    private String profileImage;
    private String createdDate;
    private String updatedDate;
    private List<String> additionalPhoneNumber;
}
