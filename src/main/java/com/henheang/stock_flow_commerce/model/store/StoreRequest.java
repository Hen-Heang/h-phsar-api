package com.henheang.stock_flow_commerce.model.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreRequest {
    private String name;
    private String bannerImage;
    private String address;
    private String primaryPhone;
    private List<String> additionalPhone;
    private String description;
    private Boolean isPublish;
}
                                         