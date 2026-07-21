package com.dxc.iotmonitor.alert.repository;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<AlertData, UUID>, JpaSpecificationExecutor<AlertData> {

    long countByUser(User user);

    long countByUserAndReadAtIsNull(User user);

    void deleteByUser(User user);

    @Query("SELECT COUNT(a) FROM AlertData a " +
           "WHERE a.sensorType = :sensorType " +
           "AND (:location IS NULL OR a.location = :location) " +
           "AND (:from IS NULL OR a.triggeredAt >= :from) " +
           "AND (:to IS NULL OR a.triggeredAt <= :to)")
    long countAlerts(@Param("sensorType") SensorType sensorType,
                     @Param("location") String location,
                     @Param("from") LocalDateTime from,
                     @Param("to") LocalDateTime to);
}
