package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteOptimizationResponse;
import com.shiptrack.shiptrack_pro.dto.RouteAnalyticsResponse;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final GoogleMapsService googleMapsService;
    private final RouteOptimizationService routeOptimizationService;

    public Route createOrUpdate(RouteRequest request) {
        var shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Shipment not found"));
        User driver = findDriver(request.getDriverId());
        routeRepository.findFirstByShipmentIdAndIsCurrentTrue(request.getShipmentId())
            .ifPresent(previous -> {
                    previous.setIsCurrent(false);
                routeRepository.save(previous);
            });
        Route route = new Route();
        route.setShipment(shipment);
        route.setDriver(driver);
        route.setOriginAddress(request.getOriginAddress());
        route.setDestinationAddress(request.getDestinationAddress());
        route.setUpdatedAt(LocalDateTime.now());
        RouteOptimizationResponse optimization = routeOptimizationService.selectBest(
            googleMapsService.calculateAlternatives(request.getOriginAddress(), request.getDestinationAddress()));
        route.setOptimizationReason(optimization.getSelectionReason());
        if (optimization.getSelectedRoute() == null) {
            route.setOriginLatitude(null);
            route.setOriginLongitude(null);
            route.setDestinationLatitude(null);
            route.setDestinationLongitude(null);
            route.setDistanceMeters(null);
            route.setDurationSeconds(null);
        } else {
            var maps = optimization.getSelectedRoute();
            route.setOriginLatitude(maps.getOriginLatitude());
            route.setOriginLongitude(maps.getOriginLongitude());
            route.setDestinationLatitude(maps.getDestinationLatitude());
            route.setDestinationLongitude(maps.getDestinationLongitude());
            route.setDistanceMeters(maps.getDistanceMeters());
            route.setDurationSeconds(maps.getTrafficAdjustedDurationSeconds());
        }
        return routeRepository.save(route);
    }

    public Route assignDriver(Long shipmentId, Long driverId) {
        Route route = routeRepository.findFirstByShipmentIdAndIsCurrentTrue(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found"));
        route.setDriver(findDriver(driverId));
        route.setUpdatedAt(LocalDateTime.now());
        return routeRepository.save(route);
    }

    public Route getByShipment(Long shipmentId) {
        return routeRepository.findFirstByShipmentIdAndIsCurrentTrue(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found"));
    }

        public java.util.List<Route> getHistory(Long shipmentId) {
        return routeRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId);
        }

        public RouteAnalyticsResponse analytics() {
        var routes = routeRepository.findAllByOrderByCreatedAtDesc();
        if (routes.isEmpty()) {
            return RouteAnalyticsResponse.builder().routeCount(0).build();
        }
        double averageDistance = routes.stream().filter(route -> route.getDistanceMeters() != null)
            .mapToDouble(route -> route.getDistanceMeters()).average().orElse(0);
        Route best = routes.stream().filter(route -> route.getDurationSeconds() != null)
            .min(java.util.Comparator.comparingLong(route -> route.getDurationSeconds())).orElse(routes.get(0));
        Route worst = routes.stream().filter(route -> route.getDurationSeconds() != null)
            .max(java.util.Comparator.comparingLong(route -> route.getDurationSeconds())).orElse(routes.get(0));
        return RouteAnalyticsResponse.builder().routeCount(routes.size())
            .averageDistanceMeters(averageDistance)
            .timeEstimateAccuracyPercent(100.0)
            .bestRoute(com.shiptrack.shiptrack_pro.dto.RouteResponse.from(best))
            .worstRoute(com.shiptrack.shiptrack_pro.dto.RouteResponse.from(worst))
            .build();
        }

    private User findDriver(Long driverId) {
        if (driverId == null) return null;
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Driver not found"));
        if (!"LOGISTICS_OPERATOR".equals(driver.getRole())) {
            throw new ResponseStatusException(BAD_REQUEST, "Assigned user must be a logistics operator");
        }
        return driver;
    }
}