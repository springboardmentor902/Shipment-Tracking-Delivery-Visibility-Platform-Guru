package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ShipmentResponse {
    Long id;
    Long customerId;
    String trackingNumber;
    String status;
    double delayRiskScore;
    LocalDateTime createdAt;
    List<PackageResponse> packages;

    public static ShipmentResponse from(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .customerId(shipment.getCustomer().getId())
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .delayRiskScore(shipment.getDelayRiskScore())
                .createdAt(shipment.getCreatedAt())
                .packages(shipment.getPackages().stream().map(PackageResponse::from).toList())
                .build();
    }
}