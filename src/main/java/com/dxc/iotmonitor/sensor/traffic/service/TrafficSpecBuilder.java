package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrafficSpecBuilder implements SpecBuilder<TrafficSensorData, TrafficFilterParams> {

    @Override
    public Specification<TrafficSensorData> build(TrafficFilterParams filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.location() != null && !filters.location().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + filters.location().toLowerCase() + "%"));
            }
            if (filters.minDensity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("trafficDensity"), filters.minDensity()));
            }
            if (filters.maxDensity() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("trafficDensity"), filters.maxDensity()));
            }
            if (filters.minSpeed() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("avgSpeed"), filters.minSpeed()));
            }
            if (filters.maxSpeed() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("avgSpeed"), filters.maxSpeed()));
            }
            if (filters.congestionLevel() != null) {
                predicates.add(cb.equal(root.get("congestionLevel"), filters.congestionLevel()));
            }
            if (filters.timestampStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filters.timestampStart()));
            }
            if (filters.timestampEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filters.timestampEnd()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
