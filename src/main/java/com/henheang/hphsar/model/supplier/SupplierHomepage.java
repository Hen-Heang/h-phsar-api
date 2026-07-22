package com.henheang.hphsar.model.supplier;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierHomepage {

    private Integer newOrder;
    private Integer preparing;
    private Integer dispatch;
    private Integer confirming;
    private Integer completed ;

}
