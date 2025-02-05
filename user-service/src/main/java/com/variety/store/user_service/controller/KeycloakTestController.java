package com.variety.store.user_service.controller;

import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/keycloak")
@RequiredArgsConstructor
public class KeycloakTestController {

    private final KeycloakService keycloakService;

    /**
     * ✅ 사용자 생성
     */
    @PostMapping("/users")
    public Mono<ResponseEntity<Void>> createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        return keycloakService.createUser(username, email, password)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    /**
     * ✅ 사용자의 정보 조회
     */
    @GetMapping("/users/{username}")
    public Mono<ResponseEntity<KeycloakService.UserRepresentation>> getUserInfo(@PathVariable String username) {
        return keycloakService.getUserInfoByUsername(username)
                .map(optionalUser -> optionalUser.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build()));
    }

    /**
     * ✅ 사용자 삭제
     */
    @DeleteMapping("/users/{username}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String username) {
        return keycloakService.deleteUser(username)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    /**
     * ✅ 사용자 정보 수정
     */
    @PutMapping("/users/{username}")
    public Mono<ResponseEntity<Void>> updateUser(
            @PathVariable String username,
            @RequestBody KeycloakService.UserRepresentation userRepresentation) {
        return keycloakService.updateUser(username, userRepresentation)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    /**
     * ✅ 모든 역할 조회
     */
    @GetMapping("/roles")
    public Mono<ResponseEntity<List<KeycloakService.RoleRepresentation>>> getAllRoles() {
        return keycloakService.getAllRolesByNames()
                .map(ResponseEntity::ok);
    }

    /**
     * ✅ 역할 생성
     */
    @PostMapping("/roles")
    public Mono<ResponseEntity<Void>> createRole(@RequestBody KeycloakService.RoleRepresentation roleRepresentation) {

        log.info("run createRole(): roleName - {}, description - {}", roleRepresentation.getName(), roleRepresentation.getDescription());

        return keycloakService.createRole(roleRepresentation.getName(), roleRepresentation.getDescription())
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    /**
     * ✅ 역할 수정
     */
    @PutMapping("/roles/{roleName}")
    public Mono<ResponseEntity<Void>> updateRole(
            @PathVariable String roleName,
            @RequestBody KeycloakService.RoleRepresentation roleRepresentation) {
        return keycloakService.updateRole(roleName, roleRepresentation)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    /**
     * ✅ 역할 삭제
     */
    @DeleteMapping("/roles/{roleName}")
    public Mono<ResponseEntity<Void>> deleteRole(@PathVariable String roleName) {
        return keycloakService.deleteRole(roleName)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    /**
     * ✅ 사용자의 역할 수정
     */
    @PutMapping("/users/{username}/roles")
    public Mono<ResponseEntity<Void>> updateUserRoles(
            @PathVariable String username,
            @RequestBody List<RoleDto> roles) {

        List<String> roleNameList = roles.stream().map(RoleDto::getName).toList();

        return keycloakService.updateUserRoles(username, roleNameList)
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}
