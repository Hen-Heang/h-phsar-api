package com.henheang.hphsar.model.appUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserSummaryDto {
    private Integer id;
    private String email;
    private Integer roleId;
    private String fullName;
    private String phone;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdDate;
}
