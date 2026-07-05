package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightFilterParams;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StreetLightSpecBuilder implements SpecBuilder<StreetLightSensorData, StreetLightFilterParams> {

    @Override
    public Specification<StreetLightSensorData> build(StreetLightFilterParams filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.location() != null && !filters.location().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + filters.location().toLowerCase() + "%"));
            }
            if (filters.minBrightness() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("brightnessLevel"), filters.minBrightness()));
            }
            if (filters.maxBrightness() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("brightnessLevel"), filters.maxBrightness()));
            }
            if (filters.minPower() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("powerConsumption"), filters.minPower()));
            }
            if (filters.maxPower() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("powerConsumption"), filters.maxPower()));
            }
            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
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
