package com.dxc.iotmonitor.sensor.traffic.service;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
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

class TrafficValidatorTest {

    private final TrafficValidator validator = new TrafficValidator();

    private static final String VALID_LOCATION = SensorLocations.TRAFFIC.get(0);
    private static final LocalDateTime PAST_TIMESTAMP =
            LocalDateTime.now(ZoneId.of("Africa/Cairo")).minusHours(1);

    @ParameterizedTest
    @MethodSource("invalidLocationCases")
    void validate_invalidLocation_throwsIllegalArgument(String location, String expectedMessage) {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(location)
                .timestamp(PAST_TIMESTAMP)
                .trafficDensity(100)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.LOW)
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
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(null)
                .trafficDensity(100)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.LOW)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("timestamp is required", ex.getMessage());
    }

    @Test
    void validate_futureTimestamp_throwsIllegalArgument() {
        LocalDateTime futureTimestamp = LocalDateTime.now(ZoneId.of("Africa/Cairo")).plusHours(1);
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(futureTimestamp)
                .trafficDensity(100)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.LOW)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("timestamp must not be in the future", ex.getMessage());
    }

    @Test
    void validate_invalidTrafficDensity_throwsIllegalArgument() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .trafficDensity(600)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.LOW)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("trafficDensity must be between 0 and 500", ex.getMessage());
    }

    @Test
    void validate_invalidAvgSpeed_throwsIllegalArgument() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .trafficDensity(100)
                .avgSpeed(200.0f)
                .congestionLevel(CongestionLevel.LOW)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("avgSpeed must be between 0 and 120", ex.getMessage());
    }

    @Test
    void validate_nullCongestionLevel_throwsIllegalArgument() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .trafficDensity(100)
                .avgSpeed(50.0f)
                .congestionLevel(null)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));

        assertEquals("congestionLevel is required", ex.getMessage());
    }

    @Test
    void validate_validRequest_doesNotThrow() {
        TrafficSensorRequest request = TrafficSensorRequest.builder()
                .location(VALID_LOCATION)
                .timestamp(PAST_TIMESTAMP)
                .trafficDensity(100)
                .avgSpeed(50.0f)
                .congestionLevel(CongestionLevel.LOW)
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }
}
