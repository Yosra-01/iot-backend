package com.dxc.iotmonitor.sensor.common;

import org.springframework.data.jpa.domain.Specification;

public interface SpecBuilder<E, F> {

    Specification<E> build(F filters);
}
