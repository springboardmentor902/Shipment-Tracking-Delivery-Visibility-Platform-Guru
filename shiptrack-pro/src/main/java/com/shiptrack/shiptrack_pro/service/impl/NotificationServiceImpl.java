package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.NotificationResponse;
import com.shiptrack.shiptrack_pro.entity.Notification;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.notification.NotificationStatus;
import com.shiptrack.shiptrack_pro.notification.NotificationType;
import com.shiptrack.shiptrack_pro.repository.NotificationRepository;
import com.shiptrack.shiptrack_pro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Value("${notification.duplicate-window-minutes:60}")
    private long duplicateWindowMinutes;

    @Value("${spring.mail.username:no-reply@shiptrack.local}")
    private String senderAddress;

    @Override
    public NotificationResponse send(NotificationType type, User user, Long shipmentId) {
        String title = type == NotificationType.DELAY_WARNING
                ? "Shipment delay warning" : "Shipment tracking update";
        String message = type == NotificationType.DELAY_WARNING
                ? "A shipment may be delayed. Please review its latest status."
                : "There is a new tracking update for your shipment.";
        return send(type, user, shipmentId, title, message);
    }

    @Override
    public NotificationResponse send(NotificationType type, User user, Long shipmentId,
                                     String title, String message) {
        LocalDateTime duplicateCutoff = LocalDateTime.now().minusMinutes(duplicateWindowMinutes);
        if (notificationRepository.existsByUserIdAndShipmentIdAndTypeAndSentAtAfter(
                user.getId(), shipmentId, type.name(), duplicateCutoff)) {
            return notificationRepository
                .findFirstByUserIdAndShipmentIdAndTypeAndSentAtAfterOrderBySentAtDesc(
                    user.getId(), shipmentId, type.name(), duplicateCutoff)
                .map(NotificationResponse::from)
                .orElseThrow();
        }

        Notification notification = notificationRepository.save(Notification.builder()
                .userId(user.getId())
                .shipmentId(shipmentId)
                .type(type.name())
                .title(title)
                .message(message)
                .status(NotificationStatus.PENDING.name())
                .build());

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(senderAddress);
            mail.setTo(user.getEmail());
            mail.setSubject(title);
            mail.setText(message);
            mailSender.send(mail);
            notification.setStatus(NotificationStatus.SENT.name());
            notification.setSentAt(LocalDateTime.now());
        } catch (RuntimeException deliveryFailure) {
            notification.setStatus(NotificationStatus.FAILED.name());
        }

        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    public List<NotificationResponse> getForUser(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Notification not found"));
        if (!notification.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(NOT_FOUND, "Notification not found");
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return NotificationResponse.from(notificationRepository.save(notification));
    }
}