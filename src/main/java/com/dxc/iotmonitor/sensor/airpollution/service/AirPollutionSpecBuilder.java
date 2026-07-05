package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionFilterParams;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AirPollutionSpecBuilder implements SpecBuilder<AirPollutionSensorData, AirPollutionFilterParams> {

    @Override
    public Specification<AirPollutionSensorData> build(AirPollutionFilterParams filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.location() != null && !filters.location().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + filters.location().toLowerCase() + "%"));
            }
            if (filters.minPm2_5() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pm2_5"), filters.minPm2_5()));
            }
            if (filters.maxPm2_5() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pm2_5"), filters.maxPm2_5()));
            }
            if (filters.minPm10() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pm10"), filters.minPm10()));
            }
            if (filters.maxPm10() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pm10"), filters.maxPm10()));
            }
            if (filters.minCo() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("co"), filters.minCo()));
            }
            if (filters.maxCo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("co"), filters.maxCo()));
            }
            if (filters.minNo2() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("no2"), filters.minNo2()));
            }
            if (filters.maxNo2() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("no2"), filters.maxNo2()));
            }
            if (filters.minSo2() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("so2"), filters.minSo2()));
            }
            if (filters.maxSo2() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("so2"), filters.maxSo2()));
            }
            if (filters.minOzone() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ozone"), filters.minOzone()));
            }
            if (filters.maxOzone() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ozone"), filters.maxOzone()));
            }
            if (filters.pollutionLevel() != null) {
                predicates.add(cb.equal(root.get("pollutionLevel"), filters.pollutionLevel()));
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
