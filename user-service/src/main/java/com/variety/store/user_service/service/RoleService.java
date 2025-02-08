package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.RoleRequest;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * 권한 생성.
     */
    public RoleRequest createRole(RoleRequest roleRequest) {
        Role role = convertToEntity(roleRequest);
        roleRepository.save(role);

        return convertToDto(role);
    }

    /**
     * 모든 권한 조회.
     */
    public List<RoleRequest> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(this::convertToDto).toList();
    }

    public RoleRequest getRoleByName(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with roleName: " + roleName));

        return convertToDto(role);
    }

    public RoleRequest getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("role not found with roleId:" + roleId));

        return convertToDto(role);
    }

    /**
     * 권한 수정.
     */
    public RoleRequest updateRole(Long roleId, RoleRequest roleRequest) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("role not found with roleId:" + roleId));

        Role updatedRole = role.updateRole(roleRequest.getName(), roleRequest.getDescription());

        return convertToDto(updatedRole);
    }

    /**
     * 권한 삭제.
     */
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }

    public RoleRequest convertToDto(Role role) {
        RoleRequest roleRequest = RoleRequest.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        return roleRequest;
    }

    public Role convertToEntity(RoleRequest roleRequest) {
        Role role = Role.builder()
                .id(roleRequest.getId())
                .name(roleRequest.getName())
                .description(roleRequest.getDescription())
                .build();

        return role;
    }
}
