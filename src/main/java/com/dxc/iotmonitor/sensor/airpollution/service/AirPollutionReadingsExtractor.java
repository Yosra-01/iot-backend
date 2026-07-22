package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.common.ReadingsExtractor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class AirPollutionReadingsExtractor implements ReadingsExtractor<AirPollutionSensorData> {

    @Override
    public Map<Metric, Float> extract(AirPollutionSensorData entity) {
        Map<Metric, Float> readings = new EnumMap<>(Metric.class);
        readings.put(Metric.CO, entity.getCo());
        readings.put(Metric.OZONE, entity.getOzone());
        return readings;
    }
}
