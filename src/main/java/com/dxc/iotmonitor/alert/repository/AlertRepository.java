package com.dxc.iotmonitor.alert.repository;

import com.dxc.iotmonitor.alert.model.AlertData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<AlertData, UUID> {

    List<AlertData> findAllByOrderByTriggeredAtDesc();

    @Override
    Optional<AlertData> findById(UUID id);
}
