package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PackageRequest {
    @NotBlank
    private String description;

    @Min(1)
    private int quantity = 1;

    private Double weight;
    private Double length;
    private Double width;
    private Double height;
}