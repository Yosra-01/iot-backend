package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.mapper.AirPollutionSensorMapper;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AirPollutionSensorService {

    private final AirPollutionSensorRepository airPollutionSensorRepository;
    private final AirPollutionSensorMapper airPollutionSensorMapper;

    public AirPollutionSensorResponse save(AirPollutionSensorRequest request) {
        if (request.getPm2_5() == null || request.getPm2_5() < 0 || request.getPm2_5() > 500) {
            throw new IllegalArgumentException("pm2_5 must be between 0 and 500");
        }
        if (request.getPm10() == null || request.getPm10() < 0 || request.getPm10() > 600) {
            throw new IllegalArgumentException("pm10 must be between 0 and 600");
        }
        if (request.getCo() == null || request.getCo() < 0 || request.getCo() > 50) {
            throw new IllegalArgumentException("co must be between 0 and 50");
        }
        if (request.getNo2() == null || request.getNo2() < 0 || request.getNo2() > 200) {
            throw new IllegalArgumentException("no2 must be between 0 and 200");
        }
        if (request.getSo2() == null || request.getSo2() < 0 || request.getSo2() > 350) {
            throw new IllegalArgumentException("so2 must be between 0 and 350");
        }
        if (request.getOzone() == null || request.getOzone() < 0 || request.getOzone() > 300) {
            throw new IllegalArgumentException("ozone must be between 0 and 300");
        }

        log.info("[AirPollutionSensorService][save] saving air pollution reading for location: {}", request.getLocation());

        AirPollutionSensorData entity = airPollutionSensorMapper.toEntity(request);
        AirPollutionSensorData savedEntity = airPollutionSensorRepository.save(entity);

        log.info("[AirPollutionSensorService][save] saved successfully with id: {}", savedEntity.getId());

        return airPollutionSensorMapper.toResponse(savedEntity);
    }

    public List<AirPollutionSensorResponse> getAll() {
        log.info("[AirPollutionSensorService][getAll] fetching all air pollution readings");

        List<AirPollutionSensorData> entities = airPollutionSensorRepository.findAllByOrderByTimestampDesc();
        List<AirPollutionSensorResponse> responses = entities.stream()
                .map(airPollutionSensorMapper::toResponse)
                .toList();

        log.info("[AirPollutionSensorService][getAll] found {} readings", responses.size());

        return responses;
    }

    public void flush() {
        log.info("[AirPollutionSensorService][flush] flushing all air pollution readings");

        airPollutionSensorRepository.deleteAll();

        log.info("[AirPollutionSensorService][flush] flushed successfully");
    }
}
