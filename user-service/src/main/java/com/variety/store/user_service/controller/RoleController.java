package com.variety.store.user_service.controller;

import com.variety.store.user_service.domain.dto.request.RoleRequest;
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
    public Mono<ResponseEntity<RoleRequest>> createRole(@RequestBody RoleRequest roleRequest) {

        log.info("run createRole(): {}", roleRequest);

        return Mono.fromCallable(() -> roleService.createRole(roleRequest))
                .flatMap(savedRole -> keycloakService.createRole(savedRole.getName(), savedRole.getDescription())
                        .thenReturn(ResponseEntity.ok(savedRole))
                )
                .doOnError(e -> log.error("권한(역할) 등록 실패: {}", e.getMessage()));
    }

    // 권한 목록 조회.
    // 페이징 및 검색 조건 추가 필요.
    @GetMapping("/roles")
    public ResponseEntity<List<RoleRequest>> getAllRoles() {
        List<RoleRequest> result = roleService.getAllRoles();
        return ResponseEntity.ok(result);
    }

    // 권한 상세 조회.
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<RoleRequest> getRoleInfo(@PathVariable Long roleId) {

        RoleRequest result = roleService.getRoleById(roleId);
        return ResponseEntity.ok(result);
    }

    // 권한 수정.
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<RoleRequest> updateRole(@PathVariable Long roleId, @RequestBody RoleRequest roleRequest) {

        RoleRequest updatedRole = roleService.updateRole(roleId, roleRequest);
        return ResponseEntity.ok(updatedRole);
    }


}
