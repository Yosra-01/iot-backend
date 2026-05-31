package com.dxc.iotmonitor.sensor.streetlight.mapper;

import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StreetLightSensorMapper {

    StreetLightSensorResponse toResponse(StreetLightSensorData entity);

    StreetLightSensorData toEntity(StreetLightSensorRequest request);
}
