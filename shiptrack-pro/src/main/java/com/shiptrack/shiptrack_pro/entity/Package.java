package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, length = 160)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private int quantity = 1;

    private Double weight;
    private Double length;
    private Double width;
    private Double height;
}