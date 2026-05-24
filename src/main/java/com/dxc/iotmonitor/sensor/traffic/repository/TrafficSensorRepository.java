package com.dxc.iotmonitor.sensor.traffic.repository;

import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrafficSensorRepository extends JpaRepository<TrafficSensorData, UUID>, JpaSpecificationExecutor<TrafficSensorData> {

    List<TrafficSensorData> findAllByOrderByTimestampDesc();

    Optional<TrafficSensorData> findTopByOrderByTimestampDesc();
}