package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficFilterParams;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

            addLocationPredicate(predicates, root, cb, filters);
            addDensityPredicates(predicates, root, cb, filters);
            addSpeedPredicates(predicates, root, cb, filters);
            addCongestionAndTimestampPredicates(predicates, root, cb, filters);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addLocationPredicate(List<Predicate> predicates, Root<TrafficSensorData> root,
                                      CriteriaBuilder cb, TrafficFilterParams filters) {
        if (filters.location() != null && !filters.location().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("location")), "%" + filters.location().toLowerCase() + "%"));
        }
    }

    private void addDensityPredicates(List<Predicate> predicates, Root<TrafficSensorData> root,
                                      CriteriaBuilder cb, TrafficFilterParams filters) {
        if (filters.minDensity() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trafficDensity"), filters.minDensity()));
        }
        if (filters.maxDensity() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trafficDensity"), filters.maxDensity()));
        }
    }

    private void addSpeedPredicates(List<Predicate> predicates, Root<TrafficSensorData> root,
                                    CriteriaBuilder cb, TrafficFilterParams filters) {
        if (filters.minSpeed() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("avgSpeed"), filters.minSpeed()));
        }
        if (filters.maxSpeed() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("avgSpeed"), filters.maxSpeed()));
        }
    }

    private void addCongestionAndTimestampPredicates(List<Predicate> predicates, Root<TrafficSensorData> root,
                                                     CriteriaBuilder cb, TrafficFilterParams filters) {
        if (filters.congestionLevel() != null) {
            predicates.add(cb.equal(root.get("congestionLevel"), filters.congestionLevel()));
        }
        if (filters.timestampStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filters.timestampStart()));
        }
        if (filters.timestampEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filters.timestampEnd()));
        }
    }
}
