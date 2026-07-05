package com.dxc.iotmonitor.sensor.common;

import com.dxc.iotmonitor.enums.Metric;

import java.util.Map;

public interface ReadingsExtractor<E> {

    Map<Metric, Float> extract(E entity);
}
