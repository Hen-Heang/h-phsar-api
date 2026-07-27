package com.henheang.hphsar.controller.buyer.notification;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.model.notification.NotificationBuyer;
import com.henheang.hphsar.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@Tag(name = "Buyer Notification Controller")
@RequestMapping("${base.buyer.v1}/notifications")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class BuyerNotificationController extends BaseController {
    private final NotificationService notificationService;

    @Operation(summary = "Get all notification")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationBuyer>>> getUserAllNotification() throws ParseException {
        return ok("Fetch notifications successfully", notificationService.getUserAllNotification());
    }

    @Operation(summary = "Mark as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markNotificationAsRead(@PathVariable Integer id){
        return ok("Mark all as read.", notificationService.markAsRead(id));
    }

    @Operation(summary = "Mark all as read")
    @PutMapping("/read")
    public ResponseEntity<ApiResponse<String>> markAllNotificationAsRead(){
        return ok(Code.NOTIFICATION_FETCHED, notificationService.markAllNotificationAsRead());
    }

}
