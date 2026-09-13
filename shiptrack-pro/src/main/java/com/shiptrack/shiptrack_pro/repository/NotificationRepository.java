package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndShipmentIdAndTypeAndSentAtAfter(
            Long userId, Long shipmentId, String type, LocalDateTime sentAfter);

        java.util.Optional<Notification> findFirstByUserIdAndShipmentIdAndTypeAndSentAtAfterOrderBySentAtDesc(
            Long userId, Long shipmentId, String type, LocalDateTime sentAfter);
}