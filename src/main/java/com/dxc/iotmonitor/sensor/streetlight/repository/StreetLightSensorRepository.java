package com.dxc.iotmonitor.sensor.streetlight.repository;

import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StreetLightSensorRepository extends JpaRepository<StreetLightSensorData, UUID> {

    List<StreetLightSensorData> findAllByOrderByTimestampDesc();
}
