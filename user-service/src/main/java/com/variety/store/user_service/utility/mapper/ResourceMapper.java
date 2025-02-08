package com.variety.store.user_service.utility.mapper;

import com.variety.store.user_service.domain.dto.request.ResourceRequest;
import com.variety.store.user_service.domain.dto.response.ResourceResponse;
import com.variety.store.user_service.domain.entity.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceMapper {

    public static ResourceResponse convertToResponse(Resource resource) {

        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .pattern(resource.getPattern())
                .httpMethod(resource.getHttpMethod())
                .description(resource.getDescription())
                .priority(resource.getPriority())
                .roles(Optional.ofNullable(resource.getResourceRoles())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(resourceRole ->
                                RoleMapper.convertToResponse(resourceRole.getRole())
                        )
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Resource convertToEntity(ResourceRequest resourceRequest) {

        return Resource.builder()
                .name(resourceRequest.getName())
                .pattern(resourceRequest.getPattern())
                .httpMethod(resourceRequest.getHttpMethod())
                .description(resourceRequest.getDescription())
                .priority(resourceRequest.getPriority())
                .build();
        // ResourceRole 추가는 서비스 계층에서 처리.
    }
}
