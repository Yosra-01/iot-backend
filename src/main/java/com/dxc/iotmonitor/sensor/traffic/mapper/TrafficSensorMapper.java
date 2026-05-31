package com.dxc.iotmonitor.sensor.traffic.mapper;

import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrafficSensorMapper {

    TrafficSensorResponse toResponse(TrafficSensorData entity);

    TrafficSensorData toEntity(TrafficSensorRequest request);
}
