package com.variety.store.user_service.utility.mapper;

import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.domain.entity.Role;

public class RoleMapper {

    public static Role convertToEntity(RoleDto roleDto) {
        return Role.builder()
                .id(roleDto.getId())
                .name(roleDto.getName())
                .description(roleDto.getDescription())
                .build();
    }

    public static RoleDto convertToDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
