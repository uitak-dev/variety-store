package com.variety.store.user_service.repository;

import com.variety.store.user_service.domain.entity.Resource;
import com.variety.store.user_service.domain.entity.ResourceRole;
import com.variety.store.user_service.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceRoleRepository extends JpaRepository<ResourceRole, Long> {

    Optional<ResourceRole> findByRoleAndResource(Role role, Resource resource);
}
