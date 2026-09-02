package com.henheang.hphsar.model.product;

import com.henheang.hphsar.model.category.CategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private Integer qty;
    private Double price;
    private String image;
    private String description;
    private Integer categoryId;
    /**
     * 카테고리를 id 대신 이름으로 지정할 때 사용한다. {@code categoryId} 가 없을 때만 읽으며,
     * 해당 이름의 카테고리가 없으면 만들어 현재 스토어에 연결한 뒤 그 id 를 쓴다.
     * 기존 클라이언트는 계속 {@code categoryId} 만 보내면 된다.
     */
    private String categoryName;
    private Boolean isPublish;
}
