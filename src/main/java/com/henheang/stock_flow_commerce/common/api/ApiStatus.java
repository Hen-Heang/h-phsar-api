package com.henheang.stock_flow_commerce.common.api;

public record ApiStatus(int code, String message) {
    public ApiStatus(Code statusCode) {
        this(statusCode.getCode(), statusCode.getMessage());
    }
}
