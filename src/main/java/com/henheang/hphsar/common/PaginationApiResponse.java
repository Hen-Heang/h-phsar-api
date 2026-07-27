package com.henheang.hphsar.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationApiResponse<T> {
    private Integer status;
    private String message;
    private T data;
    private Integer totalPage;
    private Integer totalElements;
    private String date;
}
