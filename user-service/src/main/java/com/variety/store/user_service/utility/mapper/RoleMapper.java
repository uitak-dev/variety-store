package com.variety.store.user_service.utility.mapper;

import com.variety.store.user_service.domain.dto.request.RoleRequest;
import com.variety.store.user_service.domain.dto.response.RoleResponse;
import com.variety.store.user_service.domain.entity.Role;

public class RoleMapper {

    public static RoleResponse convertToResponse(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }

    public static Role convertToEntity(RoleRequest roleRequest) {

        return Role.builder()
                .id(roleRequest.getId())
                .name(roleRequest.getName())
                .description(roleRequest.getDescription())
                .build();
    }
}
