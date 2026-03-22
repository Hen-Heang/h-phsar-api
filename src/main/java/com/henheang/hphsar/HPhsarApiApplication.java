package com.henheang.hphsar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.henheang.hphsar.repository")
public class HPhsarApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HPhsarApiApplication.class, args);
    }

}