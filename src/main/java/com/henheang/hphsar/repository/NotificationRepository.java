package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.notification.NotificationBuyer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationRepository {
    Integer createBuyerNotification(Integer buyerId, int notificationType, Integer orderId, String summery, String title, String description, boolean isRead);

    Integer createSupplierNotification(Integer supplierId, int notificationType, Integer orderId, String summery, String title, String description, boolean isRead);

    void deleteNotification(Integer check);

    boolean checkForBuyerNotification(Integer currentUserId);

    boolean checkForSupplierNotification(Integer currentUserId);

    List<NotificationBuyer> getBuyerUserAllNotification(Integer currentUserId);

    List<NotificationBuyer> getSupplierUserAllNotification(Integer currentUserId);

    boolean checkForBuyerNotificationById(Integer id, Integer currentUserId);

    boolean checkForSupplierNotificationById(Integer id, Integer currentUserId);

    String markAsReadBuyer(Integer id, Integer currentUserId);

    String markAsReadSupplier(Integer id, Integer currentUserId);

    boolean checkBuyerUnReadNotification(Integer currentUserId);

    boolean checkSupplierUnReadNotification(Integer currentUserId);

    String markAllNotificationAsReadBuyer(Integer currentUserId);

    String markAllNotificationAsReadSupplier(Integer currentUserId);

}