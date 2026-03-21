package com.henheang.stock_flow_commerce.model.store;

import com.henheang.stock_flow_commerce.model.category.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreRetailer {
    private Integer id;
    private String name;
    private String bannerImage;
    private String address;
    private String primaryPhone;
    private List<String> additionalPhone;
    private List<Category> categories;
    private Integer distributorAccountId;
    private String description;
    private String createdDate;
    private String updatedDate;
    private Boolean isPublish;
    private Boolean isActive;
    private Double rating;
    private Integer ratingCount;
    private Boolean isBookmarked;
}
