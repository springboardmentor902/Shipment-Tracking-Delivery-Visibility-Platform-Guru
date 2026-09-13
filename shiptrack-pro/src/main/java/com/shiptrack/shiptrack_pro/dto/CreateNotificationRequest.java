package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNotificationRequest {
    @NotBlank
    private String type;

    private Long shipmentId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;
}