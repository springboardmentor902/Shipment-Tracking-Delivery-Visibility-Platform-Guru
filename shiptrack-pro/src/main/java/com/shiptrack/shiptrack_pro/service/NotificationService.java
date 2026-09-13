package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.NotificationResponse;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.notification.NotificationType;

import java.util.List;

public interface NotificationService {
    NotificationResponse send(NotificationType type, User user, Long shipmentId);

    NotificationResponse send(NotificationType type, User user, Long shipmentId,
                              String title, String message);

    List<NotificationResponse> getForUser(User user);

    NotificationResponse markAsRead(Long notificationId, User user);
}