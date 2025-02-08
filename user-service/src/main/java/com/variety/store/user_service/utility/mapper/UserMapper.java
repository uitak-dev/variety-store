package com.variety.store.user_service.utility.mapper;

import com.variety.store.user_service.domain.dto.request.UserRequest;
import com.variety.store.user_service.domain.dto.response.UserResponse;
import com.variety.store.user_service.domain.entity.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponse convertToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(AddressMapper.convertToResponse(user.getAddress()))
                .roles(Optional.ofNullable(user.getUserRoles())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(userRole ->
                                RoleMapper.convertToResponse(userRole.getRole())
                        )
                        .collect(Collectors.toSet()))
                .build();
    }

    public static User convertToEntity(UserRequest userRequest) {

        return User.builder()
                .id(userRequest.getId())
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .address(AddressMapper.convertToEntity(userRequest.getAddress()))
                .build();
        // UserRole 추가는 서비스 계층에서 처리.
    }
}
