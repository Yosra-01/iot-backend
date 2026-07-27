package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.sensor.common.ReadingsExtractor;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class TrafficReadingsExtractor implements ReadingsExtractor<TrafficSensorData> {

    @Override
    public Map<Metric, Float> extract(TrafficSensorData entity) {
        Map<Metric, Float> readings = new EnumMap<>(Metric.class);
        readings.put(Metric.TRAFFIC_DENSITY, (float) entity.getTrafficDensity());
        readings.put(Metric.AVG_SPEED, entity.getAvgSpeed());
        return readings;
    }
}
