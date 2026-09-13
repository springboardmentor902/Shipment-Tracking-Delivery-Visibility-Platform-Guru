package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Route;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RouteResponse {
    Long id;
    Long shipmentId;
    Long driverId;
    String originAddress;
    String destinationAddress;
    Double originLatitude;
    Double originLongitude;
    Double destinationLatitude;
    Double destinationLongitude;
    Double distanceMeters;
    Long durationSeconds;
    boolean current;
    String optimizationReason;

    public static RouteResponse from(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .shipmentId(route.getShipment().getId())
                .driverId(route.getDriver() == null ? null : route.getDriver().getId())
                .originAddress(route.getOriginAddress())
                .destinationAddress(route.getDestinationAddress())
                .originLatitude(route.getOriginLatitude())
                .originLongitude(route.getOriginLongitude())
                .destinationLatitude(route.getDestinationLatitude())
                .destinationLongitude(route.getDestinationLongitude())
                .distanceMeters(route.getDistanceMeters())
                .durationSeconds(route.getDurationSeconds())
                .current(Boolean.TRUE.equals(route.getIsCurrent()))
                .optimizationReason(route.getOptimizationReason())
                .build();
    }
}