package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RouteOptimizationResponse {
    RouteAlternativeDTO selectedRoute;
    String selectionReason;
    List<RouteAlternativeDTO> alternatives;
}