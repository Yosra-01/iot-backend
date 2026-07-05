package com.dxc.iotmonitor.sensor.common;

import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SensorHandler<E, RQ, RS, F> {

    RS save(RQ request, Optional<User> user);

    RS getById(String id);

    RS getLatest();

    void flush();

    Page<RS> getFiltered(F filters, Pageable pageable);
}
