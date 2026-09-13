package com.shiptrack.shiptrack_pro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleMapsService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5)).build();

    @Value("${google.maps.api-key:}")
    private String apiKey;

    public MapsRouteData calculateRoute(String origin, String destination) {
        List<RouteAlternativeDTO> alternatives = calculateAlternatives(origin, destination);
        if (alternatives.isEmpty()) return null;
        RouteAlternativeDTO selected = alternatives.get(0);
        return new MapsRouteData(selected.getOriginLatitude(), selected.getOriginLongitude(),
                selected.getDestinationLatitude(), selected.getDestinationLongitude(),
                selected.getDistanceMeters(), selected.getDurationSeconds());
    }

    public List<RouteAlternativeDTO> calculateAlternatives(String origin, String destination) {
        if (apiKey == null || apiKey.isBlank()) return List.of();
        try {
            JsonNode originLocation = geocode(origin);
            JsonNode destinationLocation = geocode(destination);
            if (originLocation.isMissingNode() || destinationLocation.isMissingNode()) return List.of();
            double originLat = originLocation.path("lat").asDouble();
            double originLng = originLocation.path("lng").asDouble();
            double destinationLat = destinationLocation.path("lat").asDouble();
            double destinationLng = destinationLocation.path("lng").asDouble();
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                    + originLat + "," + originLng + "&destination=" + destinationLat + "," + destinationLng
                    + "&alternatives=true&departure_time=" + Instant.now().getEpochSecond()
                    + "&key=" + encode(apiKey);
            JsonNode routes = getJson(url).path("routes");
            List<RouteAlternativeDTO> result = new ArrayList<>();
            for (JsonNode route : routes) {
                JsonNode leg = route.path("legs").path(0);
                if (leg.isMissingNode()) continue;
                long duration = leg.path("duration").path("value").asLong();
                long trafficDuration = leg.path("duration_in_traffic").path("value").asLong(duration);
                result.add(RouteAlternativeDTO.builder()
                        .distanceMeters(leg.path("distance").path("value").asDouble())
                        .durationSeconds(duration)
                        .trafficAdjustedDurationSeconds(trafficDuration)
                        .summary(route.path("summary").asText("Google Maps route"))
                        .originLatitude(originLat).originLongitude(originLng)
                        .destinationLatitude(destinationLat).destinationLongitude(destinationLng)
                        .build());
            }
            return result;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private JsonNode geocode(String address) throws Exception {
        return getJson("https://maps.googleapis.com/maps/api/geocode/json?address="
                + encode(address) + "&key=" + encode(apiKey)).path("results").path(0)
                .path("geometry").path("location");
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(8)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) throw new IllegalStateException("Google Maps request failed");
        return objectMapper.readTree(response.body());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record MapsRouteData(double originLatitude, double originLongitude,
                                double destinationLatitude, double destinationLongitude,
                                double distanceMeters, long durationSeconds) { }
}