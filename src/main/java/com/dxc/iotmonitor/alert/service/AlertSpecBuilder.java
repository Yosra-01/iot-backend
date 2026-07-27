package com.dxc.iotmonitor.alert.service;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.alert.dto.AlertFilterParams;
import com.dxc.iotmonitor.sensor.common.SpecBuilder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
}
