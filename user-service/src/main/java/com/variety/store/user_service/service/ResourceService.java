package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.ResourceDto;
import com.variety.store.user_service.domain.entity.Resource;
import com.variety.store.user_service.domain.entity.ResourceRole;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.repository.ResourceRepository;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.utility.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final RoleRepository roleRepository;

    /**
     * 새로운 리소스 생성.
     */
    public ResourceDto createResource(ResourceDto resourceDto) {
        Resource resource = convertToEntity(resourceDto);
        Resource savedResource = resourceRepository.save(resource);
        return convertToDto(savedResource);
    }

    /**
     * 특정 리소스 조회.
     */
    @Transactional(readOnly = true)
    public ResourceDto getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with ID: " + id));
        return convertToDto(resource);
    }

    /**
     * 모든 리소스 조회.
     */
    @Transactional(readOnly = true)
    public Set<ResourceDto> getAllResource() {
        return resourceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet());
    }

    /**
     * 기존 리소스 수정.
     */
    public ResourceDto updateResource(Long resourceId, ResourceDto resourceDto) {

        Resource existingResource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with ID: " + resourceId));

        // 기본 필드 업데이트.
        existingResource.updateInfo(
                resourceDto.getName(),
                resourceDto.getPattern(),
                resourceDto.getHttpMethod(),
                resourceDto.getDescription(),
                resourceDto.getOrder()
        );

        // 업데이트할 권한 목록.
        Set<Role> updatedRoles = resourceDto.getRoles().stream()
                .map(roleDto -> roleRepository.findById(roleDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleDto.getId()))
                )
                .collect(Collectors.toSet());

        // 기존 resourceRoles 중에서 제거할 권한 필터링.
        existingResource.getResourceRoles().removeIf(resourceRole ->
                updatedRoles.stream().noneMatch(role -> role.getId().equals(resourceRole.getRole().getId()))
        );

        // 새로운 권한 추가.
        updatedRoles.forEach(role -> {
            boolean hasRole = existingResource.getResourceRoles().stream()
                    .anyMatch(resourceRole -> resourceRole.getRole().getId().equals(role.getId()));

            if (!hasRole) {
                existingResource.addResourceRole(ResourceRole.createResourceRole(role));
            }
        });

        resourceRepository.save(existingResource);
        return convertToDto(existingResource);
    }

    /**
     * 기존 리소스 삭제.
     */
    public void deleteResource(Long resourceId) {
        Resource existingResource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with ID: " + resourceId));

        // 관련된 ResourceRole 은 Cascade 옵션을 통해 삭제.
        resourceRepository.delete(existingResource);
        log.info("Resource with ID {} deleted successfully.", resourceId);
    }

    public Resource convertToEntity(ResourceDto resourceDto) {

        Set<ResourceRole> resourceRoles = resourceDto.getRoles().stream()
                .map(roleDto -> {
                    Role role = roleRepository.findById(roleDto.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleDto.getId()));
                    return ResourceRole.createResourceRole(role);
                })
                .collect(Collectors.toSet());

        Resource resource = Resource.builder()
                .name(resourceDto.getName())
                .pattern(resourceDto.getPattern())
                .httpMethod(resourceDto.getHttpMethod())
                .description(resourceDto.getDescription())
                .order(resourceDto.getOrder())
                .isActive(resourceDto.isActive())
                .resourceRoles(resourceRoles)
                .build();

        // ResourceRole 객체에 리소스를 설정
        resourceRoles.forEach(resourceRole -> resourceRole.setResource(resource));
        return resource;

    }

    public ResourceDto convertToDto(Resource resource) {

        return ResourceDto.builder()
                .id(resource.getId())
                .name(resource.getName())
                .pattern(resource.getPattern())
                .httpMethod(resource.getHttpMethod())
                .description(resource.getDescription())
                .order(resource.getOrder())
                .isActive(resource.isActive())
                .roles(resource.getResourceRoles().stream()
                        .map(resourceRole ->
                                RoleMapper.convertToDto(resourceRole.getRole())
                        )
                        .collect(Collectors.toSet()))
                .build();
    }
}
