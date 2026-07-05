package com.dxc.iotmonitor.sensor.airpollution.repository;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.airpollution.model.AirPollutionSensorData;
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
public interface AirPollutionSensorRepository extends JpaRepository<AirPollutionSensorData, UUID>,
        JpaSpecificationExecutor<AirPollutionSensorData> {

    List<AirPollutionSensorData> findAllByOrderByTimestampDesc();

    Optional<AirPollutionSensorData> findTopByOrderByTimestampDesc();

    @Query("SELECT COUNT(d) as totalReadings, " +
           "AVG(d.co) as avgCo, " +
           "MIN(d.co) as minCo, " +
           "MAX(d.co) as maxCo, " +
           "AVG(d.ozone) as avgOzone, " +
           "MIN(d.ozone) as minOzone, " +
           "MAX(d.ozone) as maxOzone " +
           "FROM AirPollutionSensorData d " +
           "WHERE (:from IS NULL OR d.timestamp >= :from) " +
           "AND (:to IS NULL OR d.timestamp <= :to) " +
           "AND (:location IS NULL OR d.location = :location)")
    StatsProjection findStats(@Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("location") String location);

    @Query("SELECT d.pollutionLevel as pollutionLevel, COUNT(d) as count " +
           "FROM AirPollutionSensorData d " +
           "WHERE (:from IS NULL OR d.timestamp >= :from) " +
           "AND (:to IS NULL OR d.timestamp <= :to) " +
           "AND (:location IS NULL OR d.location = :location) " +
           "GROUP BY d.pollutionLevel")
    List<PollutionDistributionProjection> findPollutionLevelDistribution(@Param("from") LocalDateTime from,
                                                                          @Param("to") LocalDateTime to,
                                                                          @Param("location") String location);

    @Query("SELECT FUNCTION('DATE', d.timestamp) as date, " +
           "AVG(d.co) as avgCo, " +
           "AVG(d.ozone) as avgOzone " +
           "FROM AirPollutionSensorData d " +
           "WHERE d.timestamp >= :from AND d.timestamp <= :to " +
           "AND (:location IS NULL OR d.location = :location) " +
           "GROUP BY FUNCTION('DATE', d.timestamp) " +
           "ORDER BY FUNCTION('DATE', d.timestamp)")
    List<DailyAverageProjection> findDailyAverages(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to,
                                                    @Param("location") String location);

    interface StatsProjection {
        Long getTotalReadings();
        Double getAvgCo();
        Float getMinCo();
        Float getMaxCo();
        Double getAvgOzone();
        Float getMinOzone();
        Float getMaxOzone();
    }

    interface PollutionDistributionProjection {
        PollutionLevel getPollutionLevel();
        Long getCount();
    }

    interface DailyAverageProjection {
        LocalDate getDate();
        Double getAvgCo();
        Double getAvgOzone();
    }
}
