package com.henheang.stock_flow_commerce.model.distributor;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistributorHomepage {

    private Integer newOrder;
    private Integer preparing;
    private Integer dispatch;
    private Integer confirming;
    private Integer completed ;

}
