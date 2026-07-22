package com.henheang.hphsar.controller.supplier.notification;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;

@Tag(name = "Supplier Notification Controller")
@RequestMapping("${base.supplier.v1}/notifications")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class SupplierNotificationController extends BaseController {
    private final NotificationService notificationService;

    @Operation(summary = "Get all notification")
    @GetMapping
    public ResponseEntity<?> getUserAllNotification() throws ParseException {
        return ok("fetched notification detail.", notificationService.getUserAllNotification());
    }

    @Operation(summary = "Mark as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Integer id) {
        return ok("fetched notification detail.", notificationService.markAsRead(id));
    }

    @Operation(summary = "Mark all as read")
    @PutMapping("/read")
    public ResponseEntity<?> markAllNotificationAsRead() {
        return ok("fetched notification detail.", notificationService.markAllNotificationAsRead());
    }
}
