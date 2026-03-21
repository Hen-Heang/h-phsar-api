package com.henheang.stock_flow_commerce.common.api;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private Integer code;
    private String message;
    private T data;
    private String timestamp;
    @Builder
    private ApiResponse(int status, Integer code, String message, T data, String timestamp) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

}
