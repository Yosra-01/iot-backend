package com.dxc.iotmonitor.sensor.traffic;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficStatsResponse;
import com.dxc.iotmonitor.sensor.traffic.repository.TrafficSensorRepository;
import com.dxc.iotmonitor.sensor.traffic.service.TrafficSensorHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrafficStatsHandlerTest {

    @Mock
    private TrafficSensorRepository trafficSensorRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private TrafficSensorHandler trafficSensorHandler;

    @Test
    void getStats_withData_returnsFullResponse() {
        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 3, 23, 59, 59);

        TrafficSensorRepository.StatsProjection statsMock = mock(TrafficSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(4L);
        when(statsMock.getAvgTrafficDensity()).thenReturn(250.0);
        when(statsMock.getMinTrafficDensity()).thenReturn(100);
        when(statsMock.getMaxTrafficDensity()).thenReturn(400);
        when(statsMock.getAvgSpeed()).thenReturn(55.0);
        when(statsMock.getMinSpeed()).thenReturn(30.0f);
        when(statsMock.getMaxSpeed()).thenReturn(80.0f);

        TrafficSensorRepository.CongestionDistributionProjection distMock1 =
                mock(TrafficSensorRepository.CongestionDistributionProjection.class);
        when(distMock1.getCongestionLevel()).thenReturn(CongestionLevel.MODERATE);
        when(distMock1.getCount()).thenReturn(2L);
        TrafficSensorRepository.CongestionDistributionProjection distMock2 =
                mock(TrafficSensorRepository.CongestionDistributionProjection.class);
        when(distMock2.getCongestionLevel()).thenReturn(CongestionLevel.HIGH);
        when(distMock2.getCount()).thenReturn(1L);

        TrafficSensorRepository.DailyAverageProjection dailyMock =
                mock(TrafficSensorRepository.DailyAverageProjection.class);
        when(dailyMock.getDate()).thenReturn(LocalDate.of(2026, 6, 1));
        when(dailyMock.getAvgTrafficDensity()).thenReturn(300.0);
        when(dailyMock.getAvgSpeed()).thenReturn(45.0);

        when(trafficSensorRepository.findStats(from, to, null)).thenReturn(statsMock);
        when(trafficSensorRepository.findCongestionLevelDistribution(from, to, null))
                .thenReturn(List.of(distMock1, distMock2));
        when(trafficSensorRepository.findDailyAverages(from, to, null))
                .thenReturn(List.of(dailyMock));
        when(alertRepository.countAlerts(SensorType.TRAFFIC, null, from, to)).thenReturn(7L);

        TrafficStatsResponse result = trafficSensorHandler.getStats(from, to, null);

        assertNotNull(result);
        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertNull(result.getLocation());
        assertEquals(4L, result.getTotalReadings());
        assertEquals(250.0, result.getAvgTrafficDensity());
        assertEquals(100, result.getMinTrafficDensity());
        assertEquals(400, result.getMaxTrafficDensity());
        assertEquals(55.0, result.getAvgSpeed());
        assertEquals(30.0f, result.getMinSpeed());
        assertEquals(80.0f, result.getMaxSpeed());
        assertEquals(7L, result.getAlertsTriggered());
        assertEquals(2, result.getCongestionLevelDistribution().size());
        assertEquals(2L, result.getCongestionLevelDistribution().get(CongestionLevel.MODERATE));
        assertEquals(1L, result.getCongestionLevelDistribution().get(CongestionLevel.HIGH));
        assertEquals(1, result.getDailyAverages().size());
        assertEquals("2026-06-01", result.getDailyAverages().get(0).date());
        assertEquals(300.0, result.getDailyAverages().get(0).avgTrafficDensity());
        assertEquals(45.0, result.getDailyAverages().get(0).avgSpeed());
    }

    @Test
    void getStats_withNoData_returnsEmptyState() {
        TrafficSensorRepository.StatsProjection statsMock = mock(TrafficSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(0L);
        when(statsMock.getAvgTrafficDensity()).thenReturn(null);
        when(statsMock.getMinTrafficDensity()).thenReturn(null);
        when(statsMock.getMaxTrafficDensity()).thenReturn(null);
        when(statsMock.getAvgSpeed()).thenReturn(null);
        when(statsMock.getMinSpeed()).thenReturn(null);
        when(statsMock.getMaxSpeed()).thenReturn(null);

        when(trafficSensorRepository.findStats(null, null, null)).thenReturn(statsMock);
        when(trafficSensorRepository.findCongestionLevelDistribution(null, null, null))
                .thenReturn(List.of());
        when(alertRepository.countAlerts(SensorType.TRAFFIC, null, null, null)).thenReturn(0L);

        TrafficStatsResponse result = trafficSensorHandler.getStats(null, null, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotalReadings());
        assertNull(result.getAvgTrafficDensity());
        assertNull(result.getMinTrafficDensity());
        assertNull(result.getMaxTrafficDensity());
        assertNull(result.getAvgSpeed());
        assertNull(result.getMinSpeed());
        assertNull(result.getMaxSpeed());
        assertEquals(0L, result.getAlertsTriggered());
        assertTrue(result.getCongestionLevelDistribution().isEmpty());
        assertTrue(result.getDailyAverages().isEmpty());
        verify(trafficSensorRepository, never()).findDailyAverages(any(), any(), any());
    }

    @Test
    void getStats_withRangeOver90Days_throwsException() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficSensorHandler.getStats(from, to, null));

        assertEquals("range too wide for daily breakdown", ex.getMessage());
    }

    @Test
    void getStats_withNoDateRange_returnsNoDailyAverages() {
        TrafficSensorRepository.StatsProjection statsMock = mock(TrafficSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(4L);
        when(statsMock.getAvgTrafficDensity()).thenReturn(250.0);
        when(statsMock.getMinTrafficDensity()).thenReturn(100);
        when(statsMock.getMaxTrafficDensity()).thenReturn(400);
        when(statsMock.getAvgSpeed()).thenReturn(55.0);
        when(statsMock.getMinSpeed()).thenReturn(30.0f);
        when(statsMock.getMaxSpeed()).thenReturn(80.0f);

        TrafficSensorRepository.CongestionDistributionProjection distMock =
                mock(TrafficSensorRepository.CongestionDistributionProjection.class);
        when(distMock.getCongestionLevel()).thenReturn(CongestionLevel.MODERATE);
        when(distMock.getCount()).thenReturn(4L);

        when(trafficSensorRepository.findStats(null, null, null)).thenReturn(statsMock);
        when(trafficSensorRepository.findCongestionLevelDistribution(null, null, null))
                .thenReturn(List.of(distMock));
        when(alertRepository.countAlerts(SensorType.TRAFFIC, null, null, null)).thenReturn(0L);

        TrafficStatsResponse result = trafficSensorHandler.getStats(null, null, null);

        assertNotNull(result);
        assertEquals(4L, result.getTotalReadings());
        assertTrue(result.getDailyAverages().isEmpty());
        verify(trafficSensorRepository, never()).findDailyAverages(any(), any(), any());
    }

    @Test
    void getStats_withFromAfterTo_throwsException() {
        LocalDateTime from = LocalDateTime.of(2026, 6, 3, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficSensorHandler.getStats(from, to, null));

        assertEquals("invalid date range: 'from' must be before 'to'", ex.getMessage());
    }

    @Test
    void getStats_withLocationTooLong_throwsException() {
        String longLocation = "A".repeat(101);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficSensorHandler.getStats(null, null, longLocation));

        assertEquals("location must not exceed 100 characters", ex.getMessage());
    }

    @Test
    void getStats_withFromOnlyBeyond90Days_throwsException() {
        LocalDateTime from = LocalDateTime.now().minusDays(91);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficSensorHandler.getStats(from, null, null));

        assertEquals("range too wide for daily breakdown", ex.getMessage());
    }
}
