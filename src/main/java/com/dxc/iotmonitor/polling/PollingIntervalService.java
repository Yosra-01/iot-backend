package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollingIntervalService {

    private final PollingIntervalRepository pollingIntervalRepository;
    private final PollingIntervalMapper pollingIntervalMapper;

    public PollingIntervalResponse upsert(User user, PollingIntervalRequest request) {
        return pollingIntervalRepository.findByUser(user)
                .map(existing -> {
                    log.info("[PollingIntervalService][upsert] found existing for user id: {}", user.getUserId());
                    existing.setTrafficInterval(request.getTrafficInterval());
                    existing.setAirPollutionInterval(request.getAirPollutionInterval());
                    existing.setStreetLightInterval(request.getStreetLightInterval());
                    return pollingIntervalMapper.toResponse(pollingIntervalRepository.save(existing));
                })
                .orElseGet(() -> {
                    log.info("[PollingIntervalService][upsert] creating new for user id: {}", user.getUserId());
                    PollingInterval entity = pollingIntervalMapper.toEntity(request, user);
                    return pollingIntervalMapper.toResponse(pollingIntervalRepository.save(entity));
                });
    }

    public PollingIntervalResponse getByUser(User user) {
        log.info("[PollingIntervalService][getByUser] user id: {}", user.getUserId());
        return pollingIntervalRepository.findByUser(user)
                .map(pollingIntervalMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Polling intervals not found."));
    }

    public void createDefault(User user) {
        log.info("[PollingIntervalService][createDefault] user id: {}", user.getUserId());
        PollingInterval entity = new PollingInterval();
        entity.setUser(user);
        entity.setTrafficInterval(5);
        entity.setAirPollutionInterval(5);
        entity.setStreetLightInterval(5);
        pollingIntervalRepository.save(entity);
    }

    public void flush() {
        pollingIntervalRepository.deleteAll();
        log.info("[PollingIntervalService][flush] all polling intervals deleted");
    }
}
