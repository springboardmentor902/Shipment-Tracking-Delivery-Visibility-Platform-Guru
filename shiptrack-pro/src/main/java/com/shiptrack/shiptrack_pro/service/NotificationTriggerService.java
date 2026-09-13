package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationTriggerService {

    private final NotificationService notificationService;

    @Value("${notification.delay-risk-threshold:0.7}")
    private double delayRiskThreshold;

    public void onTrackingEventAdded(User customer, Long shipmentId) {
        notificationService.send(NotificationType.SHIPMENT_UPDATE, customer, shipmentId);
    }

    public void onDelayRiskScoreChanged(User customer, Long shipmentId,
                                        double previousScore, double currentScore) {
        if (previousScore < delayRiskThreshold && currentScore >= delayRiskThreshold) {
            notificationService.send(NotificationType.DELAY_WARNING, customer, shipmentId);
        }
    }
}