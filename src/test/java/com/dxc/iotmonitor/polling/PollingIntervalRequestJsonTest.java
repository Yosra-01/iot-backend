package com.dxc.iotmonitor.polling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
@TestPropertySource(properties = "spring.jackson.deserialization.accept-float-as-int=false")
class PollingIntervalRequestJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsFractionalTrafficInterval() {
        String json = """
                {"trafficInterval":5.5,"airPollutionInterval":5,"streetLightInterval":5}
                """;

        assertThatThrownBy(() -> objectMapper.readValue(json, PollingIntervalRequest.class))
                .hasMessageContaining("Floating-point");
    }

    @Test
    void acceptsWholeNumberIntervals() throws Exception {
        String json = """
                {"trafficInterval":5,"airPollutionInterval":10,"streetLightInterval":15}
                """;

        PollingIntervalRequest request = objectMapper.readValue(json, PollingIntervalRequest.class);

        assertThat(request.getTrafficInterval()).isEqualTo(5);
        assertThat(request.getAirPollutionInterval()).isEqualTo(10);
        assertThat(request.getStreetLightInterval()).isEqualTo(15);
    }
}
