package com.henheang.stock_flow_commerce.controller.distributor.notification;

import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.notification.NotificationRetailer;
import com.henheang.stock_flow_commerce.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Tag(name = "Distributor Notification Controller")
@RequestMapping("${base.distributor.v1}notifications")
@SecurityRequirement(name = "bearer")
@RestController
public class NotificationDistributorController {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final NotificationService notificationService;

    public NotificationDistributorController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get all notification")
    @GetMapping("")
    public ResponseEntity<?> getUserAllNotification() throws ParseException {
        ApiResponse<List<NotificationRetailer>> response = ApiResponse.<List<NotificationRetailer>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch notifications successfully")
                .data(notificationService.getUserAllNotification())
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Integer id){
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Mark all as read.")
                .data(notificationService.markAsRead(id))
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark all as read")
    @PutMapping("/read")
    public ResponseEntity<?> markAllNotificationAsRead(){
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("fetched notification detail.")
                .data(notificationService.markAllNotificationAsRead())
                .date(formatter.format(new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

}
