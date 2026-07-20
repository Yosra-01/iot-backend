package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionFilterParams;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

            addLocationPredicate(predicates, root, cb, filters);
            addPm2_5Predicates(predicates, root, cb, filters);
            addPm10Predicates(predicates, root, cb, filters);
            addCoPredicates(predicates, root, cb, filters);
            addNo2Predicates(predicates, root, cb, filters);
            addSo2Predicates(predicates, root, cb, filters);
            addOzonePredicates(predicates, root, cb, filters);
            addPollutionLevelAndTimestampPredicates(predicates, root, cb, filters);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addLocationPredicate(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                      CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.location() != null && !filters.location().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("location")), "%" + filters.location().toLowerCase() + "%"));
        }
    }

    private void addPm2_5Predicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                    CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.minPm2_5() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("pm2_5"), filters.minPm2_5()));
        }
        if (filters.maxPm2_5() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("pm2_5"), filters.maxPm2_5()));
        }
    }

    private void addPm10Predicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                   CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.minPm10() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("pm10"), filters.minPm10()));
        }
        if (filters.maxPm10() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("pm10"), filters.maxPm10()));
        }
    }

    private void addCoPredicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                 CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.minCo() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("co"), filters.minCo()));
        }
        if (filters.maxCo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("co"), filters.maxCo()));
        }
    }

    private void addNo2Predicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                  CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.minNo2() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("no2"), filters.minNo2()));
        }
        if (filters.maxNo2() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("no2"), filters.maxNo2()));
        }
    }

    private void addSo2Predicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                  CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.minSo2() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("so2"), filters.minSo2()));
        }
        if (filters.maxSo2() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("so2"), filters.maxSo2()));
        }
    }

    private void addOzonePredicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                    CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.minOzone() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("ozone"), filters.minOzone()));
        }
        if (filters.maxOzone() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("ozone"), filters.maxOzone()));
        }
    }

    private void addPollutionLevelAndTimestampPredicates(List<Predicate> predicates, Root<AirPollutionSensorData> root,
                                                         CriteriaBuilder cb, AirPollutionFilterParams filters) {
        if (filters.pollutionLevel() != null) {
            predicates.add(cb.equal(root.get("pollutionLevel"), filters.pollutionLevel()));
        }
        if (filters.timestampStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filters.timestampStart()));
        }
        if (filters.timestampEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filters.timestampEnd()));
        }
    }
}
