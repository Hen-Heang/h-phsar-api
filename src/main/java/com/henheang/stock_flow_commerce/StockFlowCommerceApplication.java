package com.henheang.stock_flow_commerce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.henheang.stock_flow_commerce.repository")
public class StockFlowCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockFlowCommerceApplication.class, args);
    }

}
