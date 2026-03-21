package com.henheang.stock_flow_commerce.model.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRetailer {
    private Integer id; // notification id
    private String notificationType; // id
    private Integer orderId;
    private String store; // store name
    private String image; // store image
    private String title;
    private String description;
    private boolean seen;
    private String createdDate;
}
