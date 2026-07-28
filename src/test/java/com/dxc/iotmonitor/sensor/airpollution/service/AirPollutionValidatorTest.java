package com.dxc.iotmonitor.sensor.airpollution.service;

import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirPollutionValidatorTest {

    private final AirPollutionValidator validator = new AirPollutionValidator();

    private static final String VALID_LOCATION = SensorLocations.AIR_POLLUTION.get(0);
    private static final LocalDateTime PAST_TIMESTAMP =
            LocalDateTime.now(ZoneId.of("Africa/Cairo")).minusHours(1);

    @ParameterizedTest
    @MethodSource("invalidLocationCases")
    void validate_invalidLocation_throwsIllegalArgument(String location, String expectedMessage) {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(location)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals(expectedMessage, ex.getMessage());
    }

    private static Stream<Arguments> invalidLocationCases() {
        return Stream.of(
                Arguments.of(null, "location is required"),
                Arguments.of("   ", "location is required"),
                Arguments.of("MARS", "invalid location for this sensor type")
        );
    }

    @Test
    void validate_nullTimestamp_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(null)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("timestamp is required", ex.getMessage());
    }

    @Test
    void validate_futureTimestamp_throwsIllegalArgument() {
        LocalDateTime futureTimestamp = LocalDateTime.now(ZoneId.of("Africa/Cairo")).plusHours(1);
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(futureTimestamp)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("timestamp must not be in the future", ex.getMessage());
    }

    @Test
    void validate_invalidPm25_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(600.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("pm2_5 must be between 0 and 500", ex.getMessage());
    }

    @Test
    void validate_invalidPm10_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(700.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("pm10 must be between 0 and 600", ex.getMessage());
    }

    @Test
    void validate_invalidCo_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(60.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("co must be between 0 and 50", ex.getMessage());
    }

    @Test
    void validate_invalidNo2_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(250.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("no2 must be between 0 and 200", ex.getMessage());
    }

    @Test
    void validate_invalidSo2_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(400.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("so2 must be between 0 and 350", ex.getMessage());
    }

    @Test
    void validate_invalidOzone_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(350.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("ozone must be between 0 and 300", ex.getMessage());
    }

    @Test
    void validate_nullPollutionLevel_throwsIllegalArgument() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(null)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("pollutionLevel is required", ex.getMessage());
    }

    @Test
    void validate_validRequest_doesNotThrow() {
        AirPollutionSensorRequest request = AirPollutionSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .pm25(10.0f)
                .pm10(20.0f)
                .co(1.0f)
                .no2(5.0f)
                .so2(5.0f)
                .ozone(10.0f)
                .pollutionLevel(PollutionLevel.GOOD)
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }
}
