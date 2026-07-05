package com.dxc.iotmonitor.sensor.airpollution;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionStatsResponse;
import com.dxc.iotmonitor.sensor.airpollution.repository.AirPollutionSensorRepository;
import com.dxc.iotmonitor.sensor.airpollution.service.AirPollutionSensorHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
class AirPollutionStatsHandlerTest {

    @Mock
    private AirPollutionSensorRepository airPollutionSensorRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AirPollutionSensorHandler airPollutionSensorHandler;

    @Test
    void getStats_withData_returnsFullResponse() {
        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 3, 23, 59, 59);

        AirPollutionSensorRepository.StatsProjection statsMock = mock(AirPollutionSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(4L);
        when(statsMock.getAvgCo()).thenReturn(21.25);
        when(statsMock.getMinCo()).thenReturn(10.0f);
        when(statsMock.getMaxCo()).thenReturn(35.0f);
        when(statsMock.getAvgOzone()).thenReturn(140.0);
        when(statsMock.getMinOzone()).thenReturn(60.0f);
        when(statsMock.getMaxOzone()).thenReturn(200.0f);

        AirPollutionSensorRepository.PollutionDistributionProjection distMock =
                mock(AirPollutionSensorRepository.PollutionDistributionProjection.class);
        when(distMock.getPollutionLevel()).thenReturn(PollutionLevel.UNHEALTHY);
        when(distMock.getCount()).thenReturn(2L);

        AirPollutionSensorRepository.DailyAverageProjection dailyMock =
                mock(AirPollutionSensorRepository.DailyAverageProjection.class);
        when(dailyMock.getDate()).thenReturn(LocalDate.of(2026, 6, 1));
        when(dailyMock.getAvgCo()).thenReturn(20.0);
        when(dailyMock.getAvgOzone()).thenReturn(150.0);

        when(airPollutionSensorRepository.findStats(from, to, null)).thenReturn(statsMock);
        when(airPollutionSensorRepository.findPollutionLevelDistribution(from, to, null))
                .thenReturn(List.of(distMock));
        when(airPollutionSensorRepository.findDailyAverages(from, to, null))
                .thenReturn(List.of(dailyMock));
        when(alertRepository.countAlerts(SensorType.AIR_POLLUTION, null, from, to)).thenReturn(3L);

        AirPollutionStatsResponse result = airPollutionSensorHandler.getStats(from, to, null);

        assertNotNull(result);
        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertNull(result.getLocation());
        assertEquals(4L, result.getTotalReadings());
        assertEquals(21.25, result.getAvgCo());
        assertEquals(10.0f, result.getMinCo());
        assertEquals(35.0f, result.getMaxCo());
        assertEquals(140.0, result.getAvgOzone());
        assertEquals(60.0f, result.getMinOzone());
        assertEquals(200.0f, result.getMaxOzone());
        assertEquals(3L, result.getAlertsTriggered());
        assertEquals(1, result.getPollutionLevelDistribution().size());
        assertEquals(2L, result.getPollutionLevelDistribution().get(PollutionLevel.UNHEALTHY));
        assertEquals(1, result.getDailyAverages().size());
        assertEquals("2026-06-01", result.getDailyAverages().get(0).date());
        assertEquals(20.0, result.getDailyAverages().get(0).avgCo());
        assertEquals(150.0, result.getDailyAverages().get(0).avgOzone());
    }

    @Test
    void getStats_withNoDateRange_returnsNoDailyAverages() {
        AirPollutionSensorRepository.StatsProjection statsMock = mock(AirPollutionSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(4L);
        when(statsMock.getAvgCo()).thenReturn(21.25);
        when(statsMock.getMinCo()).thenReturn(10.0f);
        when(statsMock.getMaxCo()).thenReturn(35.0f);
        when(statsMock.getAvgOzone()).thenReturn(140.0);
        when(statsMock.getMinOzone()).thenReturn(60.0f);
        when(statsMock.getMaxOzone()).thenReturn(200.0f);

        AirPollutionSensorRepository.PollutionDistributionProjection distMock =
                mock(AirPollutionSensorRepository.PollutionDistributionProjection.class);
        when(distMock.getPollutionLevel()).thenReturn(PollutionLevel.GOOD);
        when(distMock.getCount()).thenReturn(4L);

        when(airPollutionSensorRepository.findStats(null, null, null)).thenReturn(statsMock);
        when(airPollutionSensorRepository.findPollutionLevelDistribution(null, null, null))
                .thenReturn(List.of(distMock));
        when(alertRepository.countAlerts(SensorType.AIR_POLLUTION, null, null, null)).thenReturn(0L);

        AirPollutionStatsResponse result = airPollutionSensorHandler.getStats(null, null, null);

        assertNotNull(result);
        assertTrue(result.getDailyAverages().isEmpty());
        verify(airPollutionSensorRepository, never()).findDailyAverages(any(), any(), any());
    }

    @Test
    void getStats_withRangeOver90Days_throwsException() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> airPollutionSensorHandler.getStats(from, to, null));

        assertEquals("range too wide for daily breakdown", ex.getMessage());
    }

    @Test
    void getStats_withLocationTooLong_throwsException() {
        String longLocation = "A".repeat(101);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> airPollutionSensorHandler.getStats(null, null, longLocation));

        assertEquals("location must not exceed 100 characters", ex.getMessage());
    }

    @Test
    void getStats_withFromOnlyBeyond90Days_throwsException() {
        LocalDateTime from = LocalDateTime.now().minusDays(91);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> airPollutionSensorHandler.getStats(from, null, null));

        assertEquals("range too wide for daily breakdown", ex.getMessage());
    }
}
