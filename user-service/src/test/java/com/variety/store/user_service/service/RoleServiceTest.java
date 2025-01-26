package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.RoleDto;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    private List<RoleDto> roleDtoList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();  // 테스트 간 데이터 초기화

        String[] roleNameList = {"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"};
        String[] roleDescriptionList = {"관리자 권한", "매니저 권한", "일반 사용자 권한"};

        if (roleNameList.length != roleDescriptionList.length) {
            throw new IllegalArgumentException("테스트 데이터 초기화 오류");
        }

        for (int i = 0; i < roleNameList.length; i++) {
            roleDtoList.add(RoleDto.builder()
                    .name(roleNameList[i])
                    .description(roleDescriptionList[i])
                    .build()
            );
        }
    }

    /**
     * 권한 생성 테스트
     */
    @Test
    void testCreateRole() {

        RoleDto roleDto = roleDtoList.get(0);
        RoleDto savedRole = roleService.createRole(roleDto);

        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole)
                .extracting(RoleDto::getName, RoleDto::getDescription)
                .containsExactly(roleDto.getName(), roleDto.getDescription());

        // 데이터베이스 확인
        Role foundRole = roleRepository.findById(savedRole.getId()).orElseThrow();
        assertThat(foundRole.getName()).isEqualTo(savedRole.getName());
    }

    /**
     * 모든 권한 조회 테스트
     */
    @Test
    void testGetAllRoles() {
        roleDtoList.forEach(roleDto -> roleService.createRole(roleDto));
        List<RoleDto> roles = roleService.getAllRoles();

        assertThat(roles).isNotEmpty();
        assertThat(roles).hasSize(3);

        assertThat(roles.stream().map(RoleDto::getName).toList())
                .contains(roleDtoList.get(0).getName(), roleDtoList.get(1).getName(), roleDtoList.get(2).getName());
    }

    /**
     * 권한 이름으로 조회 테스트
     */
    @Test
    void testGetRoleByName() {
        RoleDto roleDto = roleDtoList.get(0);
        roleService.createRole(roleDto);
        RoleDto foundRole = roleService.getRoleByName("ROLE_ADMIN");

        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getName()).isEqualTo("ROLE_ADMIN");
    }

    /**
     * 존재하지 않는 권한 조회 시 예외 발생 테스트
     */
    @Test
    void testGetRoleByNameNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            roleService.getRoleByName("ROLE_USER");
        });
    }

    /**
     * 권한 ID로 조회 테스트
     */
    @Test
    void testGetRoleById() {
        RoleDto roleDto = roleDtoList.get(0);
        RoleDto savedRole = roleService.createRole(roleDto);
        RoleDto foundRole = roleService.getRoleById(savedRole.getId());

        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getId()).isEqualTo(savedRole.getId());
    }

    /**
     * 권한 수정 테스트
     */
    @Test
    void testUpdateRole() {
        RoleDto roleDto = roleDtoList.get(0);
        RoleDto savedRole = roleService.createRole(roleDto);

        RoleDto updateRoleDto = RoleDto.builder()
                .id(savedRole.getId())
                .name("ROLE_UPDATE")
                .description("updated role")
                .build();

        RoleDto updatedRole = roleService.updateRole(savedRole.getId(), updateRoleDto);

        assertThat(updatedRole).isNotNull();
        assertThat(updatedRole.getName()).isEqualTo("ROLE_UPDATE");
        assertThat(updatedRole.getDescription()).isEqualTo("updated role");
    }

    /**
     * 존재하지 않는 권한 수정 시 예외 테스트
     */
    @Test
    void testUpdateRoleNotFound() {
        RoleDto updateDto = RoleDto.builder()
                .id(99L)
                .name("ROLE_USER")
                .description("User role")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            roleService.updateRole(99L, updateDto);
        });
    }

    /**
     * 권한 삭제 테스트
     */
    @Test
    void testDeleteRole() {
        RoleDto roleDto = roleDtoList.get(0);
        RoleDto savedRole = roleService.createRole(roleDto);
        roleService.deleteRole(savedRole.getId());

        assertThat(roleRepository.existsById(savedRole.getId())).isFalse();
    }
}