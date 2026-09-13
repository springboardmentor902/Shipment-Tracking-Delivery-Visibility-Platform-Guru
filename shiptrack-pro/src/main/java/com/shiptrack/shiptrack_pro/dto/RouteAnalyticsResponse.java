package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RouteAnalyticsResponse {
    double averageDistanceMeters;
    double timeEstimateAccuracyPercent;
    RouteResponse bestRoute;
    RouteResponse worstRoute;
    long routeCount;
}