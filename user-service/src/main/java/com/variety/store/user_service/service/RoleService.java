package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.RoleDto;
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
    public RoleDto createRole(RoleDto roleDto) {
        Role role = convertToEntity(roleDto);
        roleRepository.save(role);

        return convertToDto(role);
    }

    /**
     * 모든 권한 조회.
     */
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(this::convertToDto).toList();
    }

    public RoleDto getRoleByName(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with roleName: " + roleName));

        return convertToDto(role);
    }

    public RoleDto getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("role not found with roleId:" + roleId));

        return convertToDto(role);
    }

    /**
     * 권한 수정.
     */
    public RoleDto updateRole(Long roleId, RoleDto roleDto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("role not found with roleId:" + roleId));

        Role updatedRole = role.updateRole(roleDto.getName(), roleDto.getDescription());

        return convertToDto(updatedRole);
    }

    /**
     * 권한 삭제.
     */
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }

    public RoleDto convertToDto(Role role) {
        RoleDto roleDto = RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        return roleDto;
    }

    public Role convertToEntity(RoleDto roleDto) {
        Role role = Role.builder()
                .id(roleDto.getId())
                .name(roleDto.getName())
                .description(roleDto.getDescription())
                .build();

        return role;
    }
}
