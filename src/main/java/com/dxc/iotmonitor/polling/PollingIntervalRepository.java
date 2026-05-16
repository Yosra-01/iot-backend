package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PollingIntervalRepository extends JpaRepository<PollingInterval, UUID> {

    Optional<PollingInterval> findByUser(User user);
}
