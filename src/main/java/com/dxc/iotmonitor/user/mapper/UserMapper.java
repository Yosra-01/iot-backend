package com.dxc.iotmonitor.user.mapper;

import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "profilePicture", ignore = true)
    ProfileResponse toResponse(User user);
}
