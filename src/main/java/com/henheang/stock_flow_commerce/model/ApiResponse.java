package com.henheang.stock_flow_commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private Integer status;
    private String message;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    private String date;
}
