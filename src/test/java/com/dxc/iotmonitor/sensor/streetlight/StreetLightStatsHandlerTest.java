package com.dxc.iotmonitor.sensor.streetlight;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightStatsResponse;
import com.dxc.iotmonitor.sensor.streetlight.repository.StreetLightSensorRepository;
import com.dxc.iotmonitor.sensor.streetlight.service.StreetLightSensorHandler;
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
class StreetLightStatsHandlerTest {

    @Mock
    private StreetLightSensorRepository streetLightSensorRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private StreetLightSensorHandler streetLightSensorHandler;

    @Test
    void getStats_withData_returnsFullResponse() {
        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 3, 23, 59, 59);

        StreetLightSensorRepository.StatsProjection statsMock = mock(StreetLightSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(4L);
        when(statsMock.getAvgBrightness()).thenReturn(70.0);
        when(statsMock.getMinBrightness()).thenReturn(20);
        when(statsMock.getMaxBrightness()).thenReturn(100);
        when(statsMock.getAvgPowerConsumption()).thenReturn(1200.0);
        when(statsMock.getMinPowerConsumption()).thenReturn(300.0f);
        when(statsMock.getMaxPowerConsumption()).thenReturn(2000.0f);

        StreetLightSensorRepository.StatusDistributionProjection distMock =
                mock(StreetLightSensorRepository.StatusDistributionProjection.class);
        when(distMock.getStatus()).thenReturn(LightStatus.ON);
        when(distMock.getCount()).thenReturn(3L);

        StreetLightSensorRepository.DailyAverageProjection dailyMock =
                mock(StreetLightSensorRepository.DailyAverageProjection.class);
        when(dailyMock.getDate()).thenReturn(LocalDate.of(2026, 6, 1));
        when(dailyMock.getAvgBrightness()).thenReturn(80.0);
        when(dailyMock.getAvgPowerConsumption()).thenReturn(1250.0);

        when(streetLightSensorRepository.findStats(from, to, null)).thenReturn(statsMock);
        when(streetLightSensorRepository.findStatusDistribution(from, to, null))
                .thenReturn(List.of(distMock));
        when(streetLightSensorRepository.findDailyAverages(from, to, null))
                .thenReturn(List.of(dailyMock));
        when(alertRepository.countAlerts(SensorType.STREET_LIGHT, null, from, to)).thenReturn(5L);

        StreetLightStatsResponse result = streetLightSensorHandler.getStats(from, to, null);

        assertNotNull(result);
        assertEquals(from, result.getFrom());
        assertEquals(to, result.getTo());
        assertNull(result.getLocation());
        assertEquals(4L, result.getTotalReadings());
        assertEquals(70.0, result.getAvgBrightness());
        assertEquals(20, result.getMinBrightness());
        assertEquals(100, result.getMaxBrightness());
        assertEquals(1200.0, result.getAvgPowerConsumption());
        assertEquals(300.0f, result.getMinPowerConsumption());
        assertEquals(2000.0f, result.getMaxPowerConsumption());
        assertEquals(5L, result.getAlertsTriggered());
        assertEquals(1, result.getStatusDistribution().size());
        assertEquals(3L, result.getStatusDistribution().get(LightStatus.ON));
        assertEquals(1, result.getDailyAverages().size());
        assertEquals("2026-06-01", result.getDailyAverages().get(0).date());
        assertEquals(80.0, result.getDailyAverages().get(0).avgBrightness());
        assertEquals(1250.0, result.getDailyAverages().get(0).avgPowerConsumption());
    }

    @Test
    void getStats_withNoDateRange_returnsNoDailyAverages() {
        StreetLightSensorRepository.StatsProjection statsMock = mock(StreetLightSensorRepository.StatsProjection.class);
        when(statsMock.getTotalReadings()).thenReturn(4L);
        when(statsMock.getAvgBrightness()).thenReturn(70.0);
        when(statsMock.getMinBrightness()).thenReturn(20);
        when(statsMock.getMaxBrightness()).thenReturn(100);
        when(statsMock.getAvgPowerConsumption()).thenReturn(1200.0);
        when(statsMock.getMinPowerConsumption()).thenReturn(300.0f);
        when(statsMock.getMaxPowerConsumption()).thenReturn(2000.0f);

        StreetLightSensorRepository.StatusDistributionProjection distMock =
                mock(StreetLightSensorRepository.StatusDistributionProjection.class);
        when(distMock.getStatus()).thenReturn(LightStatus.ON);
        when(distMock.getCount()).thenReturn(4L);

        when(streetLightSensorRepository.findStats(null, null, null)).thenReturn(statsMock);
        when(streetLightSensorRepository.findStatusDistribution(null, null, null))
                .thenReturn(List.of(distMock));
        when(alertRepository.countAlerts(SensorType.STREET_LIGHT, null, null, null)).thenReturn(0L);

        StreetLightStatsResponse result = streetLightSensorHandler.getStats(null, null, null);

        assertNotNull(result);
        assertTrue(result.getDailyAverages().isEmpty());
        verify(streetLightSensorRepository, never()).findDailyAverages(any(), any(), any());
    }

    @Test
    void getStats_withRangeOver90Days_throwsException() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> streetLightSensorHandler.getStats(from, to, null));

        assertEquals("range too wide for daily breakdown", ex.getMessage());
    }

    @Test
    void getStats_withLocationTooLong_throwsException() {
        String longLocation = "A".repeat(101);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> streetLightSensorHandler.getStats(null, null, longLocation));

        assertEquals("location must not exceed 100 characters", ex.getMessage());
    }

    @Test
    void getStats_withFromOnlyBeyond90Days_throwsException() {
        LocalDateTime from = LocalDateTime.now().minusDays(91);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> streetLightSensorHandler.getStats(from, null, null));

        assertEquals("range too wide for daily breakdown", ex.getMessage());
    }
}
