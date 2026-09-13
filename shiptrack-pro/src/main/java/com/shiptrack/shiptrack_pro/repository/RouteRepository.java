package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
    Optional<Route> findFirstByShipmentIdAndIsCurrentTrue(Long shipmentId);
    List<Route> findByDriverId(Long driverId);
    List<Route> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("update Route r set r.isCurrent = true where r.isCurrent is null")
    int markLegacyRoutesCurrent();

    default List<Route> findByShipmentId(Long shipmentId) {
        return findByShipmentIdOrderByCreatedAtDesc(shipmentId);
    }
}