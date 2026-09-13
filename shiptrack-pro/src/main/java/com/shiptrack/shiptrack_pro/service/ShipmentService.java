package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.CreateShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.CreateTrackingEventRequest;
import com.shiptrack.shiptrack_pro.dto.PackageRequest;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.entity.Package;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final NotificationTriggerService notificationTriggerService;
    private final RouteRepository routeRepository;

    public Shipment create(CreateShipmentRequest request, User customer) {
        Shipment shipment = Shipment.builder()
                .customer(customer)
                .trackingNumber(request.getTrackingNumber())
                .build();
        for (PackageRequest packageRequest : request.getPackages()) {
            shipment.getPackages().add(Package.builder()
                    .shipment(shipment)
                    .description(packageRequest.getDescription())
                    .quantity(packageRequest.getQuantity())
                    .weight(packageRequest.getWeight())
                    .length(packageRequest.getLength())
                    .width(packageRequest.getWidth())
                    .height(packageRequest.getHeight())
                    .build());
        }
        return shipmentRepository.save(shipment);
    }

    public java.util.List<Shipment> findVisibleShipments(Authentication authentication, User user) {
        if (hasRole(authentication, "ADMINISTRATOR")) {
            return shipmentRepository.findAll();
        }
        if (hasRole(authentication, "LOGISTICS_OPERATOR")) {
            return routeRepository.findByDriverId(user.getId()).stream()
                    .map(Route::getShipment)
                    .distinct()
                    .toList();
        }
        return shipmentRepository.findByCustomerId(user.getId());
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public TrackingEvent addTrackingEvent(Long shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Shipment not found"));
        double previousRisk = shipment.getDelayRiskScore();
        shipment.setStatus(request.getStatus());
        shipment.setDelayRiskScore(request.getDelayRiskScore());
        shipmentRepository.save(shipment);
        TrackingEvent event = trackingEventRepository.save(TrackingEvent.builder()
                .shipment(shipment)
                .status(request.getStatus())
                .location(request.getLocation())
                .description(request.getDescription())
                .delayRiskScore(request.getDelayRiskScore())
                .build());
        notificationTriggerService.onTrackingEventAdded(shipment.getCustomer(), shipment.getId());
        notificationTriggerService.onDelayRiskScoreChanged(shipment.getCustomer(), shipment.getId(), previousRisk, request.getDelayRiskScore());
        return event;
    }
}