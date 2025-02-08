package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.ResourceRequest;
import com.variety.store.user_service.domain.dto.response.ResourceResponse;
import com.variety.store.user_service.domain.entity.Resource;
import com.variety.store.user_service.domain.entity.ResourceRole;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.repository.ResourceRepository;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.utility.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
    public ResourceResponse createResource(ResourceRequest resourceRequest) {
        Resource resource = ResourceMapper.convertToEntity(resourceRequest);
        Resource savedResource = resourceRepository.save(resource);

        return ResourceMapper.convertToResponse(savedResource);
    }

    /**
     * 특정 리소스 조회.
     */
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with ID: " + id));

        return ResourceMapper.convertToResponse(resource);
    }

    /**
     * 모든 리소스 조회.
     */
    @Transactional(readOnly = true)
    public Set<ResourceResponse> getAllResource() {
        return resourceRepository.findAll().stream()
                .map(ResourceMapper::convertToResponse)
                .collect(Collectors.toSet());
    }

    /**
     * 기존 리소스 수정.
     */
    public ResourceResponse updateResource(Long resourceId, ResourceRequest resourceRequest) {

        Resource existingResource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with ID: " + resourceId));

        Set<Role> updateRoles = new HashSet<>(roleRepository.findAllById(resourceRequest.getRoles()));

        existingResource.update(
                resourceRequest.getName(),
                resourceRequest.getPattern(),
                resourceRequest.getHttpMethod(),
                resourceRequest.getDescription(),
                resourceRequest.getPriority(),
                updateRoles
        );

//        resourceRepository.save(existingResource);
        return ResourceMapper.convertToResponse(existingResource);
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
}
