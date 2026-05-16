package com.dxc.iotmonitor.polling;

import com.dxc.iotmonitor.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class PollingIntervalMapper {

    public PollingInterval toEntity(PollingIntervalRequest request, User user) {
        PollingInterval entity = new PollingInterval();
        entity.setUser(user);
        entity.setTrafficInterval(request.getTrafficInterval());
        entity.setAirPollutionInterval(request.getAirPollutionInterval());
        entity.setStreetLightInterval(request.getStreetLightInterval());
        return entity;
    }

    public PollingIntervalResponse toResponse(PollingInterval entity) {
        PollingIntervalResponse response = new PollingIntervalResponse();
        response.setId(entity.getId());
        response.setTrafficInterval(entity.getTrafficInterval());
        response.setAirPollutionInterval(entity.getAirPollutionInterval());
        response.setStreetLightInterval(entity.getStreetLightInterval());
        return response;
    }
}
