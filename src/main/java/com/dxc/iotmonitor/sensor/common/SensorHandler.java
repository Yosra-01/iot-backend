package com.dxc.iotmonitor.sensor.common;

import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SensorHandler<E, Q, R, F> {

    R save(Q request, Optional<User> user);

    R getById(String id);

    R getLatest();

    void flush();

    Page<R> getFiltered(F filters, Pageable pageable);
}
