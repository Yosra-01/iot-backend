package com.dxc.iotmonitor.scheduler;

import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.enums.SensorType;
import com.dxc.iotmonitor.sensor.SensorLocations;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorRequest;
import com.dxc.iotmonitor.sensor.airpollution.dto.AirPollutionSensorResponse;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorRequest;
import com.dxc.iotmonitor.sensor.streetlight.dto.StreetLightSensorResponse;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorRequest;
import com.dxc.iotmonitor.sensor.traffic.dto.TrafficSensorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class SensorScheduler {

    private static final AtomicInteger trafficRunCount = new AtomicInteger(0);
    private static final AtomicInteger airPollutionRunCount = new AtomicInteger(0);
    private static final AtomicInteger streetLightRunCount = new AtomicInteger(0);
    private static final ZoneId CAIRO_ZONE = ZoneId.of("Africa/Cairo");

    private final RestTemplate restTemplate;

    @Value("${iot.simulation.base-url:http://localhost:8080}")
    private String simulationBaseUrl;

    @SuppressWarnings("unused")
    @Value("${scheduler.traffic.cron}")
    private String trafficCron;

    @SuppressWarnings("unused")
    @Value("${scheduler.airpollution.cron}")
    private String airPollutionCron;

    @SuppressWarnings("unused")
    @Value("${scheduler.streetlight.cron}")
    private String streetLightCron;

    @Scheduled(cron = "${scheduler.traffic.cron}")
    public void sendTrafficReading() {
        int run = trafficRunCount.incrementAndGet();
        try {
            TrafficSensorRequest request = buildTrafficRequest();
            ResponseEntity<TrafficSensorResponse> response = restTemplate.postForEntity(
                    apiUrl("/api/sensors/traffic"),
                    request,
                    TrafficSensorResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                TrafficSensorResponse body = response.getBody();
                if (body != null) {
                    log.info("""

                            [SensorScheduler][sendTrafficReading] Run #{}
                              ┌─────────────────────────────────────────┐
                              │ id             : {}                     
                              │ location       : {}                     
                              │ timestamp      : {}                     
                              │ trafficDensity : {} vehicles            
                              │ avgSpeed       : {} km/h                
                              │ congestion     : {}                     
                              │ status         : {} ✓                   
                              └─────────────────────────────────────────┘""",
                            run,
                            body.getId(), body.getLocation(), body.getTimestamp(),
                            body.getTrafficDensity(), body.getAvgSpeed(),
                            body.getCongestionLevel(), response.getStatusCode());
                } else {
                    log.warn("[SensorScheduler][sendTrafficReading] status={} run={}", response.getStatusCode(), run);
                }
            } else {
                log.warn("[SensorScheduler][sendTrafficReading] status={} run={}", response.getStatusCode(), run);
            }
        } catch (Exception e) {
            log.warn("[SensorScheduler][sendTrafficReading] scheduled post failed: run={}", run, e);
        }
    }

    @Scheduled(cron = "${scheduler.airpollution.cron}")
    public void sendAirPollutionReading() {
        int run = airPollutionRunCount.incrementAndGet();
        try {
            AirPollutionSensorRequest request = buildAirPollutionRequest();
            ResponseEntity<AirPollutionSensorResponse> response = restTemplate.postForEntity(
                    apiUrl("/api/sensors/air-pollution"),
                    request,
                    AirPollutionSensorResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                AirPollutionSensorResponse body = response.getBody();
                if (body != null) {
                    log.info("""

                            [SensorScheduler][sendAirPollutionReading] Run #{}
                              ┌─────────────────────────────────────────┐
                              │ id             : {}                     
                              │ location       : {}                     
                              │ timestamp      : {}                     
                              │ pm2_5          : {} µg/m³               
                              │ pm10           : {} µg/m³               
                              │ co             : {} mg/m³               
                              │ no2            : {} µg/m³               
                              │ so2            : {} µg/m³               
                              │ ozone          : {} µg/m³               
                              │ pollutionLevel : {}                     
                              │ status         : {} ✓                   
                              └─────────────────────────────────────────┘""",
                            run,
                            body.getId(), body.getLocation(), body.getTimestamp(),
                            body.getPm25(), body.getPm10(), body.getCo(),
                            body.getNo2(), body.getSo2(), body.getOzone(),
                            body.getPollutionLevel(), response.getStatusCode());
                } else {
                    log.warn("[SensorScheduler][sendAirPollutionReading] status={} run={}", response.getStatusCode(), run);
                }
            } else {
                log.warn("[SensorScheduler][sendAirPollutionReading] status={} run={}", response.getStatusCode(), run);
            }
        } catch (Exception e) {
            log.warn("[SensorScheduler][sendAirPollutionReading] scheduled post failed: run={}", run, e);
        }
    }

    @Scheduled(cron = "${scheduler.streetlight.cron}")
    public void sendStreetLightReading() {
        int run = streetLightRunCount.incrementAndGet();
        try {
            StreetLightSensorRequest request = buildStreetLightRequest();
            ResponseEntity<StreetLightSensorResponse> response = restTemplate.postForEntity(
                    apiUrl("/api/sensors/street-lights"),
                    request,
                    StreetLightSensorResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                StreetLightSensorResponse body = response.getBody();
                if (body != null) {
                    log.info("""

                            [SensorScheduler][sendStreetLightReading] Run #{}
                              ┌─────────────────────────────────────────┐
                              │ id               : {}                   
                              │ location         : {}                   
                              │ timestamp        : {}                   
                              │ brightnessLevel  : {}%                  
                              │ powerConsumption : {} W                 
                              │ status           : {}                   
                              │ http             : {} ✓                 
                              └─────────────────────────────────────────┘""",
                            run,
                            body.getId(), body.getLocation(), body.getTimestamp(),
                            body.getBrightnessLevel(), body.getPowerConsumption(),
                            body.getStatus(), response.getStatusCode());
                } else {
                    log.warn("[SensorScheduler][sendStreetLightReading] status={} run={}", response.getStatusCode(), run);
                }
            } else {
                log.warn("[SensorScheduler][sendStreetLightReading] status={} run={}", response.getStatusCode(), run);
            }
        } catch (Exception e) {
            log.warn("[SensorScheduler][sendStreetLightReading] scheduled post failed: run={}", run, e);
        }
    }

    private static final int TRAFFIC_DENSITY_MAX = 500;
    private static final float TRAFFIC_SPEED_MAX = 120f;
    private static final int STREET_BRIGHTNESS_MAX = 100;
    private static final float STREET_POWER_MAX = 5000f;

    /**
     * Traffic density band (fraction of {@link #TRAFFIC_DENSITY_MAX}) for each congestion level.
     */
    private static float[] trafficDensityBand(CongestionLevel level) {
        return switch (level) {
            case LOW -> new float[]{0.02f, 0.26f};
            case MODERATE -> new float[]{0.22f, 0.48f};
            case HIGH -> new float[]{0.42f, 0.72f};
            case SEVERE -> new float[]{0.68f, 1.0f};
        };
    }

    /**
     * Average speed band (fraction of {@link #TRAFFIC_SPEED_MAX}): inverse of congestion — severe traffic is slow.
     */
    private static float[] trafficSpeedBand(CongestionLevel level) {
        return switch (level) {
            case LOW -> new float[]{0.55f, 0.98f};
            case MODERATE -> new float[]{0.38f, 0.68f};
            case HIGH -> new float[]{0.22f, 0.52f};
            case SEVERE -> new float[]{0.05f, 0.38f};
        };
    }

    private static float randomMetricInBand(ThreadLocalRandom rnd, float metricMax, float[] band) {
        float lo = metricMax * band[0];
        float hi = metricMax * band[1];
        float v = lo + rnd.nextFloat() * (hi - lo);
        return Math.round(v * 100.0f) / 100.0f;
    }

    private static int randomIntInBand(ThreadLocalRandom rnd, int maxInclusive, float[] band) {
        int lo = Math.max(0, (int) Math.floor(maxInclusive * band[0]));
        int hi = Math.min(maxInclusive, (int) Math.ceil(maxInclusive * band[1]));
        if (hi <= lo) {
            hi = Math.min(maxInclusive, lo + 1);
        }
        return rnd.nextInt(lo, hi + 1);
    }

    /**
     * Brightness % band (0–100) by lamp status: ON is visibly lit, OFF is near-dark.
     */
    private static float[] streetBrightnessBand(LightStatus status) {
        return switch (status) {
            case ON -> new float[]{0.48f, 1.0f};
            case OFF -> new float[]{0.0f, 0.10f};
        };
    }

    /**
     * Power draw band (fraction of {@link #STREET_POWER_MAX}): ON draws meaningful load, OFF minimal.
     */
    private static float[] streetPowerBand(LightStatus status) {
        return switch (status) {
            case ON -> new float[]{0.15f, 0.98f};
            case OFF -> new float[]{0.0f, 0.06f};
        };
    }

    private TrafficSensorRequest buildTrafficRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        String trafficLocation = randomLocation(SensorType.TRAFFIC, rnd);
        CongestionLevel congestion = CongestionLevel.values()[rnd.nextInt(CongestionLevel.values().length)];
        float[] densityBand = trafficDensityBand(congestion);
        float[] speedBand = trafficSpeedBand(congestion);
        return TrafficSensorRequest.builder()
                .location(trafficLocation)
                .timestamp(LocalDateTime.now(CAIRO_ZONE).withNano(0))
                .trafficDensity(randomIntInBand(rnd, TRAFFIC_DENSITY_MAX, densityBand))
                .avgSpeed(randomMetricInBand(rnd, TRAFFIC_SPEED_MAX, speedBand))
                .congestionLevel(congestion)
                .build();
    }

    /** Upper bounds aligned with the scheduler's previous random ranges (service validation uses similar scales). */
    private static final float AIR_PM25_MAX = 500f;
    private static final float AIR_PM10_MAX = 600f;
    private static final float AIR_CO_MAX = 50f;
    private static final float AIR_NO2_MAX = 200f;
    private static final float AIR_SO2_MAX = 350f;
    private static final float AIR_OZONE_MAX = 300f;

    /**
     * Fraction of each metric's max for a coherent reading: GOOD stays low across pollutants, HAZARDOUS stays high.
     */
    private static float[] pollutionBandFractions(PollutionLevel level) {
        return switch (level) {
            case GOOD -> new float[]{0.03f, 0.18f};
            case MODERATE -> new float[]{0.15f, 0.38f};
            case UNHEALTHY -> new float[]{0.32f, 0.58f};
            case VERY_UNHEALTHY -> new float[]{0.52f, 0.82f};
            case HAZARDOUS -> new float[]{0.75f, 1.0f};
        };
    }

    private AirPollutionSensorRequest buildAirPollutionRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        String airLocation = randomLocation(SensorType.AIR_POLLUTION, rnd);
        PollutionLevel level = PollutionLevel.values()[rnd.nextInt(PollutionLevel.values().length)];
        float[] band = pollutionBandFractions(level);
        return AirPollutionSensorRequest.builder()
                .location(airLocation)
                .timestamp(LocalDateTime.now(CAIRO_ZONE).withNano(0))
                .pm25(randomMetricInBand(rnd, AIR_PM25_MAX, band))
                .pm10(randomMetricInBand(rnd, AIR_PM10_MAX, band))
                .co(randomMetricInBand(rnd, AIR_CO_MAX, band))
                .no2(randomMetricInBand(rnd, AIR_NO2_MAX, band))
                .so2(randomMetricInBand(rnd, AIR_SO2_MAX, band))
                .ozone(randomMetricInBand(rnd, AIR_OZONE_MAX, band))
                .pollutionLevel(level)
                .build();
    }

    private StreetLightSensorRequest buildStreetLightRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        String lightLocation = randomLocation(SensorType.STREET_LIGHT, rnd);
        LightStatus status = LightStatus.values()[rnd.nextInt(LightStatus.values().length)];
        float[] brightnessBand = streetBrightnessBand(status);
        float[] powerBand = streetPowerBand(status);
        return StreetLightSensorRequest.builder()
                .location(lightLocation)
                .timestamp(LocalDateTime.now(CAIRO_ZONE).withNano(0))
                .brightnessLevel(randomIntInBand(rnd, STREET_BRIGHTNESS_MAX, brightnessBand))
                .powerConsumption(randomMetricInBand(rnd, STREET_POWER_MAX, powerBand))
                .status(status)
                .build();
    }

    private static String randomLocation(SensorType type, ThreadLocalRandom rnd) {
        List<String> locations = SensorLocations.forType(type);
        return locations.get(rnd.nextInt(locations.size()));
    }

    private String apiUrl(String path) {
        String base = simulationBaseUrl.endsWith("/")
                ? simulationBaseUrl.substring(0, simulationBaseUrl.length() - 1)
                : simulationBaseUrl;
        return base + path;
    }
}
