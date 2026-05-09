package com.dxc.iotmonitor.sensor.airpollution.repository;

import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AirPollutionSensorRepository extends JpaRepository<AirPollutionSensorData, UUID> {

    List<AirPollutionSensorData> findAllByOrderByTimestampDesc();
}
