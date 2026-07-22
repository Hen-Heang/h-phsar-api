package com.henheang.hphsar.service;

import com.henheang.hphsar.model.notification.NotificationBuyer;
import org.apache.ibatis.annotations.Select;

import java.text.ParseException;
import java.util.List;

public interface NotificationService {
    List<NotificationBuyer> getUserAllNotification() throws ParseException;

    String markAsRead(Integer id);

    String markAllNotificationAsRead();
}
