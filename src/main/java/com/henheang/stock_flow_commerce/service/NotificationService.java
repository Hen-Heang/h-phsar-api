package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.notification.NotificationRetailer;
import org.apache.ibatis.annotations.Select;

import java.text.ParseException;
import java.util.List;

public interface NotificationService {
    List<NotificationRetailer> getUserAllNotification() throws ParseException;

    String markAsRead(Integer id);

    String markAllNotificationAsRead();
}
