package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.notification.NotificationRetailer;
import com.henheang.stock_flow_commerce.repository.NotificationRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.NotificationService;
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
    public List<NotificationRetailer> getUserAllNotification() throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        List<NotificationRetailer> notifications = new ArrayList<>();
        if (appUser.getRoleId() == 2) {
            // check if there is notification
            if (!notificationRepository.checkForRetailerNotification(currentUserId)) {
                throw new NotFoundException("Notification not found.");
            }
            // check if retailer have unread notification
//            if (!notificationRepository.checkRetailerUnReadNotification(currentUserId)) {
//                throw new NotFoundException("You have no unread notification.");
//            }
            // get notification
            notifications = notificationRepository.getRetailerUserAllNotification(currentUserId);
        } else if (appUser.getRoleId() == 1) {
            // check if there is notification in store
            if (!notificationRepository.checkForDistributorNotification(currentUserId)) {
                throw new NotFoundException("Notification not found.");
            }
            // check if retailer have unread notification
//            if (!notificationRepository.checkDistributorUnReadNotification(currentUserId)) {
//                throw new NotFoundException("You have no unread notification.");
//            }
            // get notification
            notifications = notificationRepository.getDistributorUserAllNotification(currentUserId);

        }
        // check and format
        if (notifications == null) {
            throw new InternalServerErrorException("Fail to fetch notification");
        }
        for (NotificationRetailer notification : notifications) {
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
            if (!notificationRepository.checkForRetailerNotificationById(id, currentUserId)) {
                throw new NotFoundException("This notification does not exist.");
            }
            // check if retailer have unread notification
            if (!notificationRepository.checkRetailerUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark as read
            String confirm = notificationRepository.markAsReadRetailer(id, currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        } else if (appUser.getRoleId() == 1){
            // check notification exist
            if (!notificationRepository.checkForDistributorNotificationById(id, currentUserId)) {
                throw new NotFoundException("This notification does not exist.");
            }
            // check if retailer have unread notification
            if (!notificationRepository.checkDistributorUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark as read
            String confirm = notificationRepository.markAsReadDistributor(id, currentUserId);
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
            if (!notificationRepository.checkForRetailerNotification(currentUserId)) {
                throw new NotFoundException("Notification not found.");
            }
            // check if retailer have unread notification
            if (!notificationRepository.checkRetailerUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark all as read
            String confirm = notificationRepository.markAllNotificationAsReadRetailer(currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        } else if (appUser.getRoleId() == 1) {
            // check if there is notification
            if (!notificationRepository.checkForDistributorNotification(currentUserId)) {
                throw new NotFoundException("Notification not found.");
            }
            // check if retailer have unread notification
            if (!notificationRepository.checkDistributorUnReadNotification(currentUserId)) {
                throw new NotFoundException("You have no unread notification.");
            }
            // mark all as read
            String confirm = notificationRepository.markAllNotificationAsReadDistributor(currentUserId);
            if (!Objects.equals(confirm, "1")) {
                throw new InternalServerErrorException("Fail to change status");
            }
        }
        return "Updated all notification to read.";
    }
}
