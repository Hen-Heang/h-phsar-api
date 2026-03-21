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
// this class create user template
public class AppUser implements UserDetails {
    private Integer id;
    private String email;
    private String password;
    private String role;
    private Integer roleId;
    private Boolean isVerified;
    private Boolean isActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roleId == 1){
            setRole("DISTRIBUTOR");
        } else if (roleId == 2){
            setRole("RETAILER");
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.toUpperCase());
        return Collections.singletonList(authority);
    }

//    this is for granting access based on role

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // set expire
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //Is this account enabled
    @Override
    public boolean isEnabled() {
        return true;
    }
}
