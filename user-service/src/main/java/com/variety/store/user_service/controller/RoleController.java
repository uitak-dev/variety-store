package com.variety.store.user_service.controller;

import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.service.KeycloakService;
import com.variety.store.user_service.service.ResourceService;
import com.variety.store.user_service.service.RoleService;
import com.variety.store.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final UserService userService;
    private final RoleService roleService;
    private final ResourceService resourceService;

    private final KeycloakService keycloakService;

    /**
     * 권한 생성.
     * 1. RoleService를 사용하여 DB에 권한(역할) 저장.
     * 2. Keycloak Admin REST API 를 호출하여, 인가 서버에 권한(역할) 생성.
     */
    @PostMapping("/roles")
    public Mono<ResponseEntity<RoleDto>> createRole(@RequestBody RoleDto roleDto) {

        log.info("run createRole(): {}", roleDto);

        return Mono.fromCallable(() -> roleService.createRole(roleDto))
                .flatMap(savedRole -> keycloakService.createRole(savedRole.getName(), savedRole.getDescription())
                        .thenReturn(ResponseEntity.ok(savedRole))
                )
                .doOnError(e -> log.error("권한(역할) 등록 실패: {}", e.getMessage()));
    }

    // 권한 목록 조회.
    // 페이징 및 검색 조건 추가 필요.
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> result = roleService.getAllRoles();
        return ResponseEntity.ok(result);
    }

    // 권한 상세 조회.
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<RoleDto> getRoleInfo(@PathVariable Long roleId) {

        RoleDto result = roleService.getRoleById(roleId);
        return ResponseEntity.ok(result);
    }

    // 권한 수정.
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long roleId, @RequestBody RoleDto roleDto) {

        RoleDto updatedRole = roleService.updateRole(roleId, roleDto);
        return ResponseEntity.ok(updatedRole);
    }


}
