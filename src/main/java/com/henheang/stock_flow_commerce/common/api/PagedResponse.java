package com.henheang.stock_flow_commerce.common.api;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Builder

public class PagedResponse <T>{

    private int status;
    private String message;
    private List<T> data;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    private String timestamp;
    @Builder
    private PagedResponse(int status, String message, List<T> data, int page, int size, long totalElements, int totalPages, String timestamp) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.timestamp = timestamp;
    }
}
