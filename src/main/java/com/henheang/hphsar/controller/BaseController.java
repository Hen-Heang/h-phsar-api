package com.henheang.hphsar.controller;


import com.henheang.hphsar.common.api.*;
import com.henheang.hphsar.utils.DateTimeUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseController {

protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
    ApiResponse<T> body = ApiResponse.<T>builder()
            .status(HttpStatus.OK.value())
            .code(Code.SUCCESS.getCode())
            .message(message)
            .data(data)
            .timestamp(DateTimeUtil.format(LocalDateTime.now()))
            .build();
    return ResponseEntity.ok(body);
}

    /** Same as ok(String, T) but also attaches a Set-Cookie header (e.g. the refresh-token cookie). */
    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data, ResponseCookie cookie) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .code(Code.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
    }

    protected <T> ResponseEntity<ApiResponse<T>> ok(Code code, T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .status(code.getHttpCode())
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .build();
        return ResponseEntity.status(code.getHttpCode()).body(body);
    }

    protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .code(Code.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    protected <T> ResponseEntity<ApiResponse<T>> created(Code code, T data) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .status(code.getHttpCode())
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .build();
        return ResponseEntity.status(code.getHttpCode()).body(body);
    }

    protected <T> ResponseEntity<PagedResponse<T>> okPage(
            String message,
            List<T> items,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        PagedResponse<T> body = PagedResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .build();

        return ResponseEntity.ok(body);
    }

    protected <T> ResponseEntity<PagedResponse<T>> okPage(
            Code code,
            List<T> items,
            int page,
            int size,
            long totalElements
    ) {
        return okPage(code.getMessage(), items, page, size, totalElements);
    }

}
