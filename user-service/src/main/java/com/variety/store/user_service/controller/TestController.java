package com.variety.store.user_service.controller;

import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.domain.dto.request.UserDto;
import com.variety.store.user_service.service.KeycloakService;
import com.variety.store.user_service.service.ResourceService;
import com.variety.store.user_service.service.RoleService;
import com.variety.store.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final UserService userService;
    private final RoleService roleService;
    private final ResourceService resourceService;

    private final KeycloakService keycloakService;

    /**
     * 사용자 생성.
     * 1. UserService 를 사용하여 DB에 사용자 저장.
     * 2. Keycloak Admin REST API 를 호출하여, 인가 서버에 사용자 저장.
     */
    @PostMapping("/users")
    public Mono<ResponseEntity<UserDto>> createUser(@RequestBody UserDto userDto) {

        log.info("run createUser: {}", userDto);

        return Mono.fromCallable(() -> userService.createUser(userDto))
                .flatMap(savedUser -> keycloakService.createUser(savedUser.getId(), userDto.getUsername(), userDto.getEmail(), userDto.getPassword())
                        .thenReturn(ResponseEntity.ok(savedUser))
                )
                .doOnError(e -> log.error("❌ 사용자 등록 실패: {}", e.getMessage()));
    }
}
    /*
    // 사용자 정보 조회.
    @GetMapping("/users/info")
    public String getUserInfo(Authentication authentication, @RequestParam Long id) {

        log.info("run getUserInfo");

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaim("preferred_username");

        // id로 사용자 조회, authentication 의 id 와 비교.

        return "Authenticated user: " + username;
    }

    // 권한 생성.
    @PostMapping("/role")
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {

        log.info("run createRole");

        RoleDto role = roleService.createRole(roleDto);
        return ResponseEntity.ok(role);
    }


    // 사용자 권한 추가.
    @PostMapping("")
    public String addRoleToUser() {

    }

}
     */
