package com.dxc.iotmonitor.sensor.streetlight.service;

import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreetLightValidatorTest {

    private final StreetLightValidator validator = new StreetLightValidator();

    private static final String VALID_LOCATION = SensorLocations.STREET_LIGHT.get(0);
    private static final LocalDateTime PAST_TIMESTAMP =
            LocalDateTime.now(ZoneId.of("Africa/Cairo")).minusHours(1);

    @Test
    void validate_nullLocation_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(null)
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("location is required", ex.getMessage());
    }

    @Test
    void validate_blankLocation_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location("   ")
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("location is required", ex.getMessage());
    }

    @Test
    void validate_invalidLocation_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location("MARS")
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("invalid location for this sensor type", ex.getMessage());
    }

    @Test
    void validate_nullTimestamp_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(null)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("timestamp is required", ex.getMessage());
    }

    @Test
    void validate_futureTimestamp_throwsIllegalArgument() {
        LocalDateTime futureTimestamp = LocalDateTime.now(ZoneId.of("Africa/Cairo")).plusHours(1);
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(futureTimestamp)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("timestamp must not be in the future", ex.getMessage());
    }

    @Test
    void validate_invalidBrightnessLevel_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(150)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("brightnessLevel must be between 0 and 100", ex.getMessage());
    }

    @Test
    void validate_invalidPowerConsumption_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(50)
                .powerConsumption(6000.0f)
                .status(LightStatus.ON)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("powerConsumption must be between 0 and 5000", ex.getMessage());
    }

    @Test
    void validate_nullStatus_throwsIllegalArgument() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(null)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("status is required", ex.getMessage());
    }

    @Test
    void validate_validRequest_doesNotThrow() {
        StreetLightSensorRequest request = StreetLightSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .brightnessLevel(50)
                .powerConsumption(100.0f)
                .status(LightStatus.ON)
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }
}
