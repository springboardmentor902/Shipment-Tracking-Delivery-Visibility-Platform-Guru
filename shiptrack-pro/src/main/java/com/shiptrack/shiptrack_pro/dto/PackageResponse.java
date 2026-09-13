package com.shiptrack.shiptrack_pro.dto;

import com.shiptrack.shiptrack_pro.entity.Package;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PackageResponse {
    Long id;
    String description;
    int quantity;
    Double weight;
    Double length;
    Double width;
    Double height;

    public static PackageResponse from(Package value) {
        return PackageResponse.builder().id(value.getId()).description(value.getDescription())
                .quantity(value.getQuantity()).weight(value.getWeight()).length(value.getLength())
                .width(value.getWidth()).height(value.getHeight()).build();
    }
}