package com.dxc.iotmonitor.sensor.airpollution.mapper;

import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AirPollutionSensorMapper {

    AirPollutionSensorResponse toResponse(AirPollutionSensorData entity);

    AirPollutionSensorData toEntity(AirPollutionSensorRequest request);
}
