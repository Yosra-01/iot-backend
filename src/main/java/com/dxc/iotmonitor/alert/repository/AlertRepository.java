package com.dxc.iotmonitor.alert.repository;

import com.dxc.iotmonitor.alert.AlertData;
import com.dxc.iotmonitor.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<AlertData, UUID> {

    List<AlertData> findByUserOrderByTriggeredAtDesc(User user);

    long countByUser(User user);
}
