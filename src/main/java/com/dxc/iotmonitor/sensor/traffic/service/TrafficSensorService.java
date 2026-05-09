package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.mapper.TrafficSensorMapper;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrafficSensorService {

    private final TrafficSensorRepository trafficSensorRepository;
    private final TrafficSensorMapper trafficSensorMapper;

    public TrafficSensorResponse save(TrafficSensorRequest request) {
        TrafficSensorData entity = trafficSensorMapper.toEntity(request);
        TrafficSensorData savedEntity = trafficSensorRepository.save(entity);
        log.info("Traffic sensor data saved successfully for location: {}", request.getLocation());
        return trafficSensorMapper.toResponse(savedEntity);
    }

    public List<TrafficSensorResponse> getAll() {
        List<TrafficSensorData> entities = trafficSensorRepository.findAll();
        return entities.stream()
                .map(trafficSensorMapper::toResponse)
                .toList();
    }

    public void flush() {
        trafficSensorRepository.deleteAll();
        log.info("[TrafficSensorService][flush] Traffic sensor table flushed successfully");
    }
}
