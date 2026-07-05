package com.dxc.iotmonitor.sensor.streetlight.repository;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.streetlight.model.StreetLightSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreetLightSensorRepository extends JpaRepository<StreetLightSensorData, UUID>,
        JpaSpecificationExecutor<StreetLightSensorData> {

    List<StreetLightSensorData> findAllByOrderByTimestampDesc();

    Optional<StreetLightSensorData> findTopByOrderByTimestampDesc();

    @Query("SELECT COUNT(d) as totalReadings, " +
           "AVG(d.brightnessLevel) as avgBrightness, " +
           "MIN(d.brightnessLevel) as minBrightness, " +
           "MAX(d.brightnessLevel) as maxBrightness, " +
           "AVG(d.powerConsumption) as avgPowerConsumption, " +
           "MIN(d.powerConsumption) as minPowerConsumption, " +
           "MAX(d.powerConsumption) as maxPowerConsumption " +
           "FROM StreetLightSensorData d " +
           "WHERE (:from IS NULL OR d.timestamp >= :from) " +
           "AND (:to IS NULL OR d.timestamp <= :to) " +
           "AND (:location IS NULL OR d.location = :location)")
    StatsProjection findStats(@Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("location") String location);

    @Query("SELECT d.status as status, COUNT(d) as count " +
           "FROM StreetLightSensorData d " +
           "WHERE (:from IS NULL OR d.timestamp >= :from) " +
           "AND (:to IS NULL OR d.timestamp <= :to) " +
           "AND (:location IS NULL OR d.location = :location) " +
           "GROUP BY d.status")
    List<StatusDistributionProjection> findStatusDistribution(@Param("from") LocalDateTime from,
                                                               @Param("to") LocalDateTime to,
                                                               @Param("location") String location);

    @Query("SELECT FUNCTION('DATE', d.timestamp) as date, " +
           "AVG(d.brightnessLevel) as avgBrightness, " +
           "AVG(d.powerConsumption) as avgPowerConsumption " +
           "FROM StreetLightSensorData d " +
           "WHERE d.timestamp >= :from AND d.timestamp <= :to " +
           "AND (:location IS NULL OR d.location = :location) " +
           "GROUP BY FUNCTION('DATE', d.timestamp) " +
           "ORDER BY FUNCTION('DATE', d.timestamp)")
    List<DailyAverageProjection> findDailyAverages(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to,
                                                    @Param("location") String location);

    interface StatsProjection {
        Long getTotalReadings();
        Double getAvgBrightness();
        Integer getMinBrightness();
        Integer getMaxBrightness();
        Double getAvgPowerConsumption();
        Float getMinPowerConsumption();
        Float getMaxPowerConsumption();
    }

    interface StatusDistributionProjection {
        LightStatus getStatus();
        Long getCount();
    }

    interface DailyAverageProjection {
        LocalDate getDate();
        Double getAvgBrightness();
        Double getAvgPowerConsumption();
    }
}
