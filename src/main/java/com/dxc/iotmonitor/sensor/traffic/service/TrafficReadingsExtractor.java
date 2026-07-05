package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.sensor.common.ReadingsExtractor;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrafficReadingsExtractor implements ReadingsExtractor<TrafficSensorData> {

    @Override
    public Map<Metric, Float> extract(TrafficSensorData entity) {
        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.TRAFFIC_DENSITY, (float) entity.getTrafficDensity());
        readings.put(Metric.AVG_SPEED, entity.getAvgSpeed());
        return readings;
    }
}
