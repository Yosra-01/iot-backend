package com.dxc.iotmonitor.settings.repository;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.model.ThresholdSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThresholdSettingRepository extends JpaRepository<ThresholdSetting, UUID> {

    Optional<ThresholdSetting> findByTypeAndMetricAndAlertType(SensorType type, Metric metric, AlertType alertType);
    List<ThresholdSetting> findByType(SensorType type);
}
