package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateShipmentRequest {
    @NotBlank
    private String trackingNumber;

    @Valid
    private List<PackageRequest> packages = new ArrayList<>();
}