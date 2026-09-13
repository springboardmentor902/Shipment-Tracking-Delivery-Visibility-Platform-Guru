package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.DriverAssignmentRequest;
import com.shiptrack.shiptrack_pro.dto.RouteRequest;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.dto.RouteAnalyticsResponse;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteResponse create(@Valid @RequestBody RouteRequest request) {
        return RouteResponse.from(routeService.createOrUpdate(request));
    }

    @PutMapping("/{shipmentId}/driver")
    public RouteResponse assignDriver(@PathVariable Long shipmentId,
                                      @Valid @RequestBody DriverAssignmentRequest request) {
        return RouteResponse.from(routeService.assignDriver(shipmentId, request.getDriverId()));
    }

    @GetMapping("/{shipmentId}")
    public RouteResponse get(@PathVariable Long shipmentId, Authentication authentication) {
        Route route = routeService.getByShipment(shipmentId);
        User user = currentUser(authentication);
        boolean admin = hasRole(authentication, "ADMINISTRATOR");
        boolean operator = hasRole(authentication, "LOGISTICS_OPERATOR");
        boolean owner = route.getShipment().getCustomer().getId().equals(user.getId());
        boolean assigned = route.getDriver() != null && route.getDriver().getId().equals(user.getId());
        if (!admin && !operator && !owner && !assigned) {
            throw new ResponseStatusException(FORBIDDEN, "You cannot view this route");
        }
        return RouteResponse.from(route);
    }

    @GetMapping("/{shipmentId}/history")
    public java.util.List<RouteResponse> history(@PathVariable Long shipmentId, Authentication authentication) {
        java.util.List<Route> history = routeService.getHistory(shipmentId);
        if (history.isEmpty()) return java.util.List.of();
        User user = currentUser(authentication);
        Route latest = history.get(0);
        boolean allowed = hasRole(authentication, "ADMINISTRATOR")
                || hasRole(authentication, "LOGISTICS_OPERATOR")
                || latest.getShipment().getCustomer().getId().equals(user.getId());
        if (!allowed) throw new ResponseStatusException(FORBIDDEN, "You cannot view this route history");
        return history.stream().map(RouteResponse::from).toList();
    }

    @GetMapping("/analytics/summary")
    public RouteAnalyticsResponse analytics(Authentication authentication) {
        if (!hasRole(authentication, "ADMINISTRATOR")) {
            throw new ResponseStatusException(FORBIDDEN, "Administrator access required");
        }
        return routeService.analytics();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}