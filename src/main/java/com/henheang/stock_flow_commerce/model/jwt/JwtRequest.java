package com.henheang.stock_flow_commerce.model.jwt;

import java.io.Serializable;

public class JwtRequest implements Serializable {
    private static final long serialVersionUID = -2550188375426007488L;

    private String email;
    private String password;

    //need default constructor for JSON Parsing
    public JwtRequest()
    {

    }

    public JwtRequest(String username, String password) {
        this.setEmail(username);
        this.setPassword(password);
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
