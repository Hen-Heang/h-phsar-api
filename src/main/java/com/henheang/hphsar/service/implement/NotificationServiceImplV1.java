package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.notification.NotificationBuyer;
import com.henheang.hphsar.repository.NotificationRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.NotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationServiceImplV1 implements NotificationService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final StoreRepository storeRepository;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImplV1(StoreRepository storeRepository, NotificationRepository notificationRepository) {
        this.storeRepository = storeRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationBuyer> getUserAllNotification() throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        List<NotificationBuyer> notifications = new ArrayList<>();
        if (appUser.getRoleId() == 2) {
            if (!notificationRepository.checkForBuyerNotification(currentUserId)) {
                return notifications; // empty list — no notifications yet
            }
            notifications = notificationRepository.getBuyerUserAllNotification(currentUserId);
        } else if (appUser.getRoleId() == 1) {
            if (!notificationRepository.checkForSupplierNotification(currentUserId)) {
                return notifications; // empty list — no notifications yet
            }
            notifications = notificationRepository.getSupplierUserAllNotification(currentUserId);
        }
        // check and format
        if (notifications == null) {
            throw new InternalServerErrorException("Fail to fetch notification");
        }
        for (NotificationBuyer notification : notifications) {
            notification.setCreatedDate(formatter.format(formatter.parse(notification.getCreatedDate())));
        }
        return notifications;
    }

    @Override
    public String markAsRead(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (appUser.getRoleId() == 2) {
            // check notification exist
            if (!notificationRepository.checkForBuyerNotificationById(id, currentUserId)) {
                throw new NotFoundException("This notification does not exist.");
            }
            // check if user has unread notification
            if (!notificationRepository.checkBuyerUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark as read
            String confirm = notificationRepository.markAsReadBuyer(id, currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        } else if (appUser.getRoleId() == 1){
            // check notification exist
            if (!notificationRepository.checkForSupplierNotificationById(id, currentUserId)) {
                throw new NotFoundException("This notification does not exist.");
            }
            // check if user has unread notification
            if (!notificationRepository.checkSupplierUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark as read
            String confirm = notificationRepository.markAsReadSupplier(id, currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        }
        return "Notification updated to read.";
    }

    @Override
    public String markAllNotificationAsRead() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (appUser.getRoleId() == 2) {
            // check if there is notification
            if (!notificationRepository.checkForBuyerNotification(currentUserId)) {
                throw new NotFoundException("Notification not found.");
            }
            // check if user has unread notification
            if (!notificationRepository.checkBuyerUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark all as read
            String confirm = notificationRepository.markAllNotificationAsReadBuyer(currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        } else if (appUser.getRoleId() == 1) {
            // check if there is notification
            if (!notificationRepository.checkForSupplierNotification(currentUserId)) {
                throw new NotFoundException("Notification not found.");
            }
            // check if user has unread notification
            if (!notificationRepository.checkSupplierUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark all as read
            String confirm = notificationRepository.markAllNotificationAsReadSupplier(currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        }
        return "Updated all notification to read.";
    }
}
