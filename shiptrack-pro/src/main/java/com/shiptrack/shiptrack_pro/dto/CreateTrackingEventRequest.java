package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTrackingEventRequest {
    @NotBlank
    private String status;

    private String location;
    private String description;
    private double delayRiskScore;
}