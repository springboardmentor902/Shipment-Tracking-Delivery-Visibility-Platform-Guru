package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.CreateShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.CreateTrackingEventRequest;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;
    private final UserRepository userRepository;

    @PostMapping("/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse createShipment(@Valid @RequestBody CreateShipmentRequest request,
                                            Authentication authentication) {
        return ShipmentResponse.from(shipmentService.create(request, currentUser(authentication)));
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> getShipments(Authentication authentication) {
        User user = currentUser(authentication);
        return shipmentService.findVisibleShipments(authentication, user).stream()
                .map(ShipmentResponse::from)
                .toList();
    }

    @PostMapping("/tracking/shipments/{shipmentId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingEvent addTrackingEvent(@PathVariable Long shipmentId,
                                          @Valid @RequestBody CreateTrackingEventRequest request) {
        return shipmentService.addTrackingEvent(shipmentId, request);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
    }
}