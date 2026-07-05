package com.dxc.iotmonitor.sensor.traffic.repository;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.traffic.model.TrafficSensorData;
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
public interface TrafficSensorRepository extends JpaRepository<TrafficSensorData, UUID>, JpaSpecificationExecutor<TrafficSensorData> {

    List<TrafficSensorData> findAllByOrderByTimestampDesc();

    Optional<TrafficSensorData> findTopByOrderByTimestampDesc();

    @Query("SELECT COUNT(d) as totalReadings, " +
           "AVG(d.trafficDensity) as avgTrafficDensity, " +
           "MIN(d.trafficDensity) as minTrafficDensity, " +
           "MAX(d.trafficDensity) as maxTrafficDensity, " +
           "AVG(d.avgSpeed) as avgSpeed, " +
           "MIN(d.avgSpeed) as minSpeed, " +
           "MAX(d.avgSpeed) as maxSpeed " +
           "FROM TrafficSensorData d " +
           "WHERE (:from IS NULL OR d.timestamp >= :from) " +
           "AND (:to IS NULL OR d.timestamp <= :to) " +
           "AND (:location IS NULL OR d.location = :location)")
    StatsProjection findStats(@Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("location") String location);

    @Query("SELECT d.congestionLevel as congestionLevel, COUNT(d) as count " +
           "FROM TrafficSensorData d " +
           "WHERE (:from IS NULL OR d.timestamp >= :from) " +
           "AND (:to IS NULL OR d.timestamp <= :to) " +
           "AND (:location IS NULL OR d.location = :location) " +
           "GROUP BY d.congestionLevel")
    List<CongestionDistributionProjection> findCongestionLevelDistribution(@Param("from") LocalDateTime from,
                                                                            @Param("to") LocalDateTime to,
                                                                            @Param("location") String location);

    @Query("SELECT FUNCTION('DATE', d.timestamp) as date, " +
           "AVG(d.trafficDensity) as avgTrafficDensity, " +
           "AVG(d.avgSpeed) as avgSpeed " +
           "FROM TrafficSensorData d " +
           "WHERE d.timestamp >= :from AND d.timestamp <= :to " +
           "AND (:location IS NULL OR d.location = :location) " +
           "GROUP BY FUNCTION('DATE', d.timestamp) " +
           "ORDER BY FUNCTION('DATE', d.timestamp)")
    List<DailyAverageProjection> findDailyAverages(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to,
                                                    @Param("location") String location);

    interface StatsProjection {
        Long getTotalReadings();
        Double getAvgTrafficDensity();
        Integer getMinTrafficDensity();
        Integer getMaxTrafficDensity();
        Double getAvgSpeed();
        Float getMinSpeed();
        Float getMaxSpeed();
    }

    interface CongestionDistributionProjection {
        CongestionLevel getCongestionLevel();
        Long getCount();
    }

    interface DailyAverageProjection {
        LocalDate getDate();
        Double getAvgTrafficDensity();
        Double getAvgSpeed();
    }
}
