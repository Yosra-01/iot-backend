package com.dxc.iotmonitor.alert.service;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.alert.dto.AlertFilterParams;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AlertSpecBuilder implements SpecBuilder<AlertData, AlertFilterParams> {

    @Override
    public Specification<AlertData> build(AlertFilterParams filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addLocationPredicate(predicates, root, cb, filters);
            addEqualsPredicates(predicates, root, cb, filters);
            addDateRangePredicates(predicates, root, cb, filters);
            addReadPredicate(predicates, root, cb, filters);
            addReadingLevelPredicates(predicates, root, query, cb, filters);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addLocationPredicate(List<Predicate> predicates, Root<AlertData> root,
                                      CriteriaBuilder cb, AlertFilterParams filters) {
        if (filters.location() != null && !filters.location().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("location")), "%" + filters.location().toLowerCase() + "%"));
        }
    }

    private void addEqualsPredicates(List<Predicate> predicates, Root<AlertData> root,
                                     CriteriaBuilder cb, AlertFilterParams filters) {
        if (filters.sensorType() != null) {
            predicates.add(cb.equal(root.get("sensorType"), filters.sensorType()));
        }
        if (filters.metric() != null) {
            predicates.add(cb.equal(root.get("metric"), filters.metric()));
        }
        if (filters.alertType() != null) {
            predicates.add(cb.equal(root.get("alertType"), filters.alertType()));
        }
    }

    private void addDateRangePredicates(List<Predicate> predicates, Root<AlertData> root,
                                        CriteriaBuilder cb, AlertFilterParams filters) {
        if (filters.triggeredStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("triggeredAt"), filters.triggeredStart()));
        }
        if (filters.triggeredEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("triggeredAt"), filters.triggeredEnd()));
        }
    }

    private void addReadPredicate(List<Predicate> predicates, Root<AlertData> root,
                                  CriteriaBuilder cb, AlertFilterParams filters) {
        if (filters.read() == Boolean.TRUE) {
            predicates.add(cb.isNotNull(root.get("readAt")));
        } else if (filters.read() == Boolean.FALSE) {
            predicates.add(cb.isNull(root.get("readAt")));
        }
    }

    private void addReadingLevelPredicates(List<Predicate> predicates, Root<AlertData> root,
                                           CriteriaQuery<?> query, CriteriaBuilder cb, AlertFilterParams filters) {
        if (filters.pollutionLevel() != null) {
            predicates.add(cb.equal(root.get("sensorType"), SensorType.AIR_POLLUTION));
            predicates.add(existsAirPollutionReading(root, query, cb, filters));
        }
        if (filters.congestionLevel() != null) {
            predicates.add(cb.equal(root.get("sensorType"), SensorType.TRAFFIC));
            predicates.add(existsTrafficReading(root, query, cb, filters));
        }
        if (filters.status() != null) {
            predicates.add(cb.equal(root.get("sensorType"), SensorType.STREET_LIGHT));
            predicates.add(existsStreetLightReading(root, query, cb, filters));
        }
    }

    private Predicate existsAirPollutionReading(Root<AlertData> root, CriteriaQuery<?> query,
                                                CriteriaBuilder cb, AlertFilterParams filters) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<AirPollutionSensorData> reading = subquery.from(AirPollutionSensorData.class);
        subquery.select(cb.literal(1)).where(
                cb.equal(reading.get("id"), root.get("readingId")),
                cb.equal(reading.get("pollutionLevel"), filters.pollutionLevel()));
        return cb.exists(subquery);
    }

    private Predicate existsTrafficReading(Root<AlertData> root, CriteriaQuery<?> query,
                                           CriteriaBuilder cb, AlertFilterParams filters) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<TrafficSensorData> reading = subquery.from(TrafficSensorData.class);
        subquery.select(cb.literal(1)).where(
                cb.equal(reading.get("id"), root.get("readingId")),
                cb.equal(reading.get("congestionLevel"), filters.congestionLevel()));
        return cb.exists(subquery);
    }

    private Predicate existsStreetLightReading(Root<AlertData> root, CriteriaQuery<?> query,
                                               CriteriaBuilder cb, AlertFilterParams filters) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<StreetLightSensorData> reading = subquery.from(StreetLightSensorData.class);
        subquery.select(cb.literal(1)).where(
                cb.equal(reading.get("id"), root.get("readingId")),
                cb.equal(reading.get("status"), filters.status()));
        return cb.exists(subquery);
    }
}
