package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.ResourceRequest;
import com.variety.store.user_service.domain.dto.response.ResourceResponse;
import com.variety.store.user_service.domain.entity.Resource;
import com.variety.store.user_service.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource resource;
    private ResourceRequest resourceRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        resource = Resource.builder()
                .id(1L)
                .name("Test Resource")
                .pattern("/api/test/**")
                .httpMethod("GET")
                .description("Test resource description")
                .priority(1L)
                .build();

        resourceRequest = ResourceRequest.builder()
                .name("Test Resource")
                .pattern("/api/test/**")
                .httpMethod("GET")
                .description("Test resource description")
                .priority(1L)
                .build();
    }

    @Test
    void testCreateResource() {
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        ResourceResponse savedResource = resourceService.createResource(resourceRequest);

        assertNotNull(savedResource);
        assertEquals("Test Resource", savedResource.getName());

        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    void testGetResourceById() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        ResourceResponse foundResource = resourceService.getResourceById(1L);
        assertEquals("Test Resource", foundResource.getName());

        verify(resourceRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteResource() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        doNothing().when(resourceRepository).delete(resource);

        resourceService.deleteResource(1L);

        verify(resourceRepository, times(1)).delete(resource);
    }
}