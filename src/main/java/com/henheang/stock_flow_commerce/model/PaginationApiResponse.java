package com.henheang.stock_flow_commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationApiResponse<T> {
    private Integer status;
    private String message;
    private T data;
    private Integer totalPage;
    private String date;
}
