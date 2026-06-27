package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.sensor.common.ReadingsExtractor;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StreetLightReadingsExtractor implements ReadingsExtractor<StreetLightSensorData> {

    @Override
    public Map<Metric, Float> extract(StreetLightSensorData entity) {
        Map<Metric, Float> readings = new HashMap<>();
        readings.put(Metric.BRIGHTNESS_LEVEL, (float) entity.getBrightnessLevel());
        readings.put(Metric.POWER_CONSUMPTION, entity.getPowerConsumption());
        return readings;
    }
}
