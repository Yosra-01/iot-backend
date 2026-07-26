package com.dxc.iotmonitor.user.mapper;

import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.model.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {

    ProfileResponse toResponse(User user);
}
