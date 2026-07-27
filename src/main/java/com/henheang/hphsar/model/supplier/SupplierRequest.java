package com.henheang.hphsar.model.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierRequest {
//    private Integer supplierAccountId;
    private String firstName;
    private String lastName;
    private String gender;
    private String profileImage;
}
