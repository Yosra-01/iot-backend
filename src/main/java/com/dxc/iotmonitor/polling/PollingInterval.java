package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "polling_intervals")
@Getter
@Setter
@NoArgsConstructor
public class PollingInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "traffic_interval", nullable = false)
    private Integer trafficInterval;

    @Column(name = "air_pollution_interval", nullable = false)
    private Integer airPollutionInterval;

    @Column(name = "street_light_interval", nullable = false)
    private Integer streetLightInterval;
}
