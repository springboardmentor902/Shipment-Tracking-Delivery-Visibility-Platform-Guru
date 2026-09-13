package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import com.shiptrack.shiptrack_pro.dto.RouteOptimizationResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RouteOptimizationService {
    public RouteOptimizationResponse selectBest(List<RouteAlternativeDTO> alternatives) {
        if (alternatives == null || alternatives.isEmpty()) {
            return RouteOptimizationResponse.builder()
                    .alternatives(List.of())
                    .selectionReason("Google Maps alternatives were unavailable; route saved without estimates.")
                    .build();
        }
        RouteAlternativeDTO selected = alternatives.stream()
                .min(Comparator.comparingLong(RouteAlternativeDTO::getTrafficAdjustedDurationSeconds))
                .orElseThrow();
        String reason = "Selected the route with the lowest traffic-adjusted duration ("
                + selected.getTrafficAdjustedDurationSeconds() + " seconds).";
        return RouteOptimizationResponse.builder()
                .selectedRoute(selected)
                .selectionReason(reason)
                .alternatives(alternatives)
                .build();
    }
}