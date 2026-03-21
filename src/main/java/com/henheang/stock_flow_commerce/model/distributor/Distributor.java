package com.henheang.stock_flow_commerce.model.distributor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Distributor {
    private Integer id;
    private Integer distributorAccountId;
    private String firstName;
    private String lastName;
    private String gender;
    private String profileImage;
    private String createdDate;
    private String updatedDate;
}
