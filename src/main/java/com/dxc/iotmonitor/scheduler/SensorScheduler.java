package com.dxc.iotmonitor.scheduler;

import com.dxc.iotmonitor.enums.AirPollutionLocation;
import com.dxc.iotmonitor.enums.CongestionLevel;
import com.dxc.iotmonitor.enums.LightStatus;
import com.dxc.iotmonitor.enums.PollutionLevel;
import com.dxc.iotmonitor.enums.StreetLightLocation;
import com.dxc.iotmonitor.enums.TrafficLocation;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class SensorScheduler {

    private static final AtomicInteger trafficRunCount = new AtomicInteger(0);
    private static final AtomicInteger airPollutionRunCount = new AtomicInteger(0);
    private static final AtomicInteger streetLightRunCount = new AtomicInteger(0);

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
                    log.info("\n[SensorScheduler][sendTrafficReading] Run #{}\n" +
                            "  ┌─────────────────────────────────────────┐\n" +
                            "  │ id             : {}                     \n" +
                            "  │ location       : {}                     \n" +
                            "  │ timestamp      : {}                     \n" +
                            "  │ trafficDensity : {} vehicles            \n" +
                            "  │ avgSpeed       : {} km/h                \n" +
                            "  │ congestion     : {}                     \n" +
                            "  │ status         : {} ✓                   \n" +
                            "  └─────────────────────────────────────────┘",
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
                    log.info("\n[SensorScheduler][sendAirPollutionReading] Run #{}\n" +
                            "  ┌─────────────────────────────────────────┐\n" +
                            "  │ id             : {}                     \n" +
                            "  │ location       : {}                     \n" +
                            "  │ timestamp      : {}                     \n" +
                            "  │ pm2_5          : {} µg/m³               \n" +
                            "  │ pm10           : {} µg/m³               \n" +
                            "  │ co             : {} mg/m³               \n" +
                            "  │ no2            : {} µg/m³               \n" +
                            "  │ so2            : {} µg/m³               \n" +
                            "  │ ozone          : {} µg/m³               \n" +
                            "  │ pollutionLevel : {}                     \n" +
                            "  │ status         : {} ✓                   \n" +
                            "  └─────────────────────────────────────────┘",
                            run,
                            body.getId(), body.getLocation(), body.getTimestamp(),
                            body.getPm2_5(), body.getPm10(), body.getCo(),
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
                    log.info("\n[SensorScheduler][sendStreetLightReading] Run #{}\n" +
                            "  ┌─────────────────────────────────────────┐\n" +
                            "  │ id               : {}                   \n" +
                            "  │ location         : {}                   \n" +
                            "  │ timestamp        : {}                   \n" +
                            "  │ brightnessLevel  : {}%                  \n" +
                            "  │ powerConsumption : {} W                 \n" +
                            "  │ status           : {}                   \n" +
                            "  │ http             : {} ✓                 \n" +
                            "  └─────────────────────────────────────────┘",
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

    private TrafficSensorRequest buildTrafficRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        TrafficLocation[] trafficLocations = TrafficLocation.values();
        TrafficLocation trafficLocation = trafficLocations[rnd.nextInt(trafficLocations.length)];
        return TrafficSensorRequest.builder()
                .location(trafficLocation)
                .timestamp(LocalDateTime.now().withNano(0))
                .trafficDensity(rnd.nextInt(0, 501))
                .avgSpeed(Math.round(rnd.nextFloat() * 120 * 100.0f) / 100.0f)
                .congestionLevel(CongestionLevel.values()[rnd.nextInt(CongestionLevel.values().length)])
                .build();
    }

    private AirPollutionSensorRequest buildAirPollutionRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        AirPollutionLocation[] airLocations = AirPollutionLocation.values();
        AirPollutionLocation airLocation = airLocations[rnd.nextInt(airLocations.length)];
        return AirPollutionSensorRequest.builder()
                .location(airLocation)
                .timestamp(LocalDateTime.now().withNano(0))
                .pm2_5(Math.round(rnd.nextFloat() * 500 * 100.0f) / 100.0f)
                .pm10(Math.round(rnd.nextFloat() * 600 * 100.0f) / 100.0f)
                .co(Math.round(rnd.nextFloat() * 50 * 100.0f) / 100.0f)
                .no2(Math.round(rnd.nextFloat() * 200 * 100.0f) / 100.0f)
                .so2(Math.round(rnd.nextFloat() * 350 * 100.0f) / 100.0f)
                .ozone(Math.round(rnd.nextFloat() * 300 * 100.0f) / 100.0f)
                .pollutionLevel(PollutionLevel.values()[rnd.nextInt(PollutionLevel.values().length)])
                .build();
    }

    private StreetLightSensorRequest buildStreetLightRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        StreetLightLocation[] lightLocations = StreetLightLocation.values();
        StreetLightLocation lightLocation = lightLocations[rnd.nextInt(lightLocations.length)];
        return StreetLightSensorRequest.builder()
                .location(lightLocation)
                .timestamp(LocalDateTime.now().withNano(0))
                .brightnessLevel(rnd.nextInt(0, 101))
                .powerConsumption(Math.round(rnd.nextFloat() * 5000 * 100.0f) / 100.0f)
                .status(LightStatus.values()[rnd.nextInt(LightStatus.values().length)])
                .build();
    }

    private String apiUrl(String path) {
        String base = simulationBaseUrl.endsWith("/")
                ? simulationBaseUrl.substring(0, simulationBaseUrl.length() - 1)
                : simulationBaseUrl;
        return base + path;
    }
}
