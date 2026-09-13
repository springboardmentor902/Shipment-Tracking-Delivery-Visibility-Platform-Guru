package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RouteAlternativeDTO {
    double distanceMeters;
    long durationSeconds;
    long trafficAdjustedDurationSeconds;
    String summary;
    double originLatitude;
    double originLongitude;
    double destinationLatitude;
    double destinationLongitude;
}