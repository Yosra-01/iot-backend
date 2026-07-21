package com.dxc.iotmonitor.sensor.common;

public interface SensorValidator<Q> {

    void validate(Q request);
}
