package com.henheang.stock_flow_commerce.model.appUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
// used for transferring data without exposing info
public class AppUserDto {
    private Integer id;
    private String email;
//    private String role;
    private Integer roleId;
    private Boolean isVerified;
    private Boolean isActive;
}
