package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverAssignmentRequest {
    @NotNull
    private Long driverId;
}