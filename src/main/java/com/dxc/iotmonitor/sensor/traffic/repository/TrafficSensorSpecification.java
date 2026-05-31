package com.dxc.iotmonitor.sensor.traffic.repository;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrafficSensorSpecification {

    public static Specification<TrafficSensorData> filterBy(
            String location, Integer minDensity, Integer maxDensity,
            Float minSpeed, Float maxSpeed, CongestionLevel congestionLevel,
            LocalDateTime timestampStart, LocalDateTime timestampEnd) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (minDensity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("trafficDensity"), minDensity));
            }
            if (maxDensity != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("trafficDensity"), maxDensity));
            }
            if (minSpeed != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("avgSpeed"), minSpeed));
            }
            if (maxSpeed != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("avgSpeed"), maxSpeed));
            }
            if (congestionLevel != null) {
                predicates.add(cb.equal(root.get("congestionLevel"), congestionLevel));
            }
            if (timestampStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), timestampStart));
            }
            if (timestampEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), timestampEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}