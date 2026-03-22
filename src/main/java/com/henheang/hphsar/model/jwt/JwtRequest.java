package com.henheang.hphsar.model.jwt;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class JwtRequest implements Serializable {
    @Serial
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

}
