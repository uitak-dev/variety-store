package com.variety.store.user_service.controller;

import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.domain.dto.request.UserDto;
import com.variety.store.user_service.service.KeycloakService;
import com.variety.store.user_service.service.ResourceService;
import com.variety.store.user_service.service.RoleService;
import com.variety.store.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final ResourceService resourceService;

    private final KeycloakService keycloakService;

    /**
     * 사용자 정보 조회.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication, @PathVariable Long userId) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaim("preferred_username");

        log.info("Authenticated user: {}, {}", jwt.getClaim("sub"), username);

        if (Long.parseLong(jwt.getClaim("sub")) != userId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        UserDto result = userService.getUserBasicInfo(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 생성.
     * 1. UserService 를 사용하여 DB에 사용자 저장.
     * 2. Keycloak Admin REST API 를 호출하여, 인가 서버에 사용자 저장.
     */
    @PostMapping("/users")
    public Mono<ResponseEntity<UserDto>> createUser(@RequestBody UserDto userDto) {

        log.info("run createUser: {}", userDto);

        return Mono.fromCallable(() -> userService.createUser(userDto))
                .flatMap(savedUser -> keycloakService.createUser(userDto.getUsername(), userDto.getEmail(), userDto.getPassword())
                        .thenReturn(ResponseEntity.ok(savedUser))
                )
                .doOnError(e -> log.error("사용자 등록 실패: {}", e.getMessage()));
    }


}
