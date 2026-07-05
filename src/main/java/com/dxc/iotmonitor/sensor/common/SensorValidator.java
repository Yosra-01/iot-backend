package com.dxc.iotmonitor.sensor.common;

public interface SensorValidator<RQ> {

    void validate(RQ request);
}
