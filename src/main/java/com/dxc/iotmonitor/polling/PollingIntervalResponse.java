package com.dxc.iotmonitor.polling;

import lombok.Data;

import java.util.UUID;

@Data
public class PollingIntervalResponse {

    private UUID id;
    private Integer trafficInterval;
    private Integer airPollutionInterval;
    private Integer streetLightInterval;
}
