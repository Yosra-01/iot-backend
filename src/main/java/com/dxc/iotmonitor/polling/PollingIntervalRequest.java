package com.dxc.iotmonitor.polling;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PollingIntervalRequest {

    @NotNull(message = "trafficInterval is required")
    @Min(value = 1, message = "trafficInterval must be between 1 and 60")
    @Max(value = 60, message = "trafficInterval must be between 1 and 60")
    private Integer trafficInterval;

    @NotNull(message = "airPollutionInterval is required")
    @Min(value = 1, message = "airPollutionInterval must be between 1 and 60")
    @Max(value = 60, message = "airPollutionInterval must be between 1 and 60")
    private Integer airPollutionInterval;

    @NotNull(message = "streetLightInterval is required")
    @Min(value = 1, message = "streetLightInterval must be between 1 and 60")
    @Max(value = 60, message = "streetLightInterval must be between 1 and 60")
    private Integer streetLightInterval;
}
