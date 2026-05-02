package com.dxc.iotmonitor.auth.mapper;

import com.dxc.iotmonitor.auth.dto.SignupRequest;
import com.dxc.iotmonitor.auth.dto.AuthResponse;
import com.dxc.iotmonitor.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    User toEntity(SignupRequest request);
    AuthResponse toResponse(User user);

}
