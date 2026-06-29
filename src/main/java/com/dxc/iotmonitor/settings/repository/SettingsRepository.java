package com.dxc.iotmonitor.settings.repository;

import com.dxc.iotmonitor.enums.AlertType;
import com.dxc.iotmonitor.enums.Metric;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.settings.model.Settings;
import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, UUID> {

    List<Settings> findByUser(User user);

    Optional<Settings> findByUserAndTypeAndMetricAndAlertType(
            User user,
            SensorType type,
            Metric metric,
            AlertType alertType
    );

    void deleteByUser(User user);
}
