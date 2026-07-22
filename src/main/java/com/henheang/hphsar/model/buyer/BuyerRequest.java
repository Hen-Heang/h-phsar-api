package com.henheang.hphsar.model.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BuyerRequest {

    private String firstName;
    private String lastName;
    private String gender;
    private String address;
    private String primaryPhoneNumber;
    private List<String> additionalPhoneNumber;
    private String profileImage;

}
