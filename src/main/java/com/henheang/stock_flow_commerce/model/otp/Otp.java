package com.henheang.stock_flow_commerce.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Otp {
    private Integer id;
    private Integer accountId;
    private Integer otpCode;
    private String email;
    private Date createdDate;
}
