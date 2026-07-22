package com.henheang.hphsar.model.buyer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Buyer {
    private Integer id;
    private Integer buyerAccountId;
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
