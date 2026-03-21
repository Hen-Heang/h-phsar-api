package com.henheang.stock_flow_commerce.model.retailer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RetailerRequest {

    private String firstName;
    private String lastName;
    private String gender;
    private String address;
    private String primaryPhoneNumber;
    private List<String> additionalPhoneNumber;
    private String profileImage;

}
