package com.variety.store.user_service.service;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KeycloakService {

    private final WebClient webClient;

    public KeycloakService(@Qualifier("keycloakWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // 사용자 생성
    public Mono<Void> createUser(String username, String email, String password) {

        UserRepresentation userRepresentation = new UserRepresentation(null, username, email, password);
        log.info("Keycloak 사용자 등록 요청 JSON: {}", userRepresentation);

        return webClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Keycloak 사용자 등록 실패: {} - {}", response.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("Keycloak 사용자 등록 실패: " + errorBody));
                                })
                )
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Keycloak 사용자 등록 성공: {}", email))
                .doOnError(e -> log.error("Keycloak 사용자 등록 중 오류 발생: {}", e.getMessage()));
    }

    // 사용자 아이디로, keycloak에 등록된 사용자 정보 조회.
    public Mono<Optional<UserRepresentation>> getUserInfoByUsername(String username) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("username", username)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(UserRepresentation[].class)
                .map(users -> {
                    if (users.length > 0 && users[0].getId() != null) {
                        return Optional.of(users[0]);
                    }
                    return Optional.empty();
                });
    }

    // 사용자 정보 수정
    public Mono<Void> updateUser(String username, UserRepresentation userRepresentation) {
        if (userRepresentation == null) {
            return Mono.error(new IllegalArgumentException("수정할 필드가 제공되지 않았습니다."));
        }

        return webClient.put()
                .uri("/users/{username}", username)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> log.info("사용자 정보가 업데이트되었습니다: {}", username))
                .doOnError(e -> log.error("사용자 수정 실패: {}", e.getMessage()));
    }

    // 사용자 삭제
    public Mono<Void> deleteUser(String username) {

        return getUserInfoByUsername(username)
                .flatMap(userRepresentation -> {
                    if (userRepresentation.isPresent()) {
                        String userId = userRepresentation.get().getId();

                        return webClient.delete()
                                .uri("/users/{userId}", userId)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .retrieve()
                                .toBodilessEntity()
                                .doOnSuccess(response -> log.info("Successfully deleted user: {}", userId))
                                .doOnError(error -> log.error("Failed to delete user: {}", userId, error))
                                .then();
                    }
                    else {
                        log.warn("User not found: {}", username);
                        return Mono.empty();
                    }
                });
    }

    // 권한 생성
    public Mono<Void> createRole(String roleName, String description) {

        log.info("roleRepresentation: {}", new RoleRepresentation(null, roleName, description));

        return webClient.post()
                .uri("/roles")
                .bodyValue(new RoleRepresentation(null, roleName, description))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> log.info("역할 정보가 생성되었습니다: {}", roleName))
                .doOnError(e -> log.error("역할 생성 실패: {}", e.getMessage()));
    }

    // Realm 내 등록된 모든 역할 목록 조회.
    public Mono<List<RoleRepresentation>> getAllRolesByNames() {
        return webClient.get()
                .uri("/roles")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RoleRepresentation>>() {});
    }

    // 권한 수정
    public Mono<Void> updateRole(String roleName, RoleRepresentation roleRepresentation) {
        if (roleRepresentation == null || roleRepresentation.getName().isEmpty()) {
            return Mono.error(new IllegalArgumentException("수정할 필드가 제공되지 않았습니다."));
        }

        return webClient.put()
                .uri("/roles/{roleName}", roleName)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleRepresentation)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> log.info("역할 정보가 업데이트되었습니다: {}", roleName))
                .doOnError(e -> log.error("역할 수정 실패: {}", e.getMessage()));
    }

    // 권한 삭제
    public Mono<Void> deleteRole(String roleName) {
        return webClient.delete()
                .uri("/roles/{roleName}", roleName)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 사용자 역할 수정.
    public Mono<Void> updateUserRoles(String username, List<String> roleNames) {
        return getUserInfoByUsername(username)
                .flatMap(userRepresentation -> {
                    if (userRepresentation.isPresent()) {
                        String userId = userRepresentation.get().getId();

                        // 현재 사용자에게 할당된 역할 목록 조회.
                        return getUserRoles(userId)
                                .zipWith(getAllRolesByNames())
                                .flatMap(tuple -> {
                                    List<RoleRepresentation> currentRoles = tuple.getT1();
                                    List<RoleRepresentation> newRoles = tuple.getT2().stream()
                                            .filter(role -> roleNames.contains(role.getName()))
                                            .toList();

                                    // 제거 되어야 할 역할 목록.
                                    List<RoleRepresentation> rolesToRemove = currentRoles.stream()
                                            .filter(role ->
                                                    newRoles.stream().noneMatch(newRole ->
                                                            newRole.getId().equals(role.getId())))
                                            .toList();

                                    // 할당 되어야 할 역할 목록.
                                    List<RoleRepresentation> rolesToAdd = newRoles.stream()
                                            .filter(newRole ->
                                                    currentRoles.stream().noneMatch(existingRole ->
                                                            existingRole.getId().equals(newRole.getId())))
                                            .toList();

                                    log.info("Updating roles for user '{}': Removing {} | Adding {}",
                                            username, rolesToRemove, rolesToAdd);

                                    return removeRolesFromUser(userId, rolesToRemove)
                                            .then(addRolesToUserByUserId(userId, rolesToAdd));
                                });
                    }
                    else {
                        log.warn("User not found: {}", username);
                        return Mono.empty();
                    }
                });
    }

    // 사용자 특정 권한(역할) 제거.
    private Mono<Void> removeRolesFromUser(String userId, List<RoleRepresentation> roles) {
        return webClient.method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{userId}/role-mappings/realm")
                        .build(userId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(roles)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Successfully removed all roles from user: {}", userId))
                .doOnError(error -> log.error("Failed to remove roles from user: {}", userId, error))
                .then();
    }

    // 사용자 권한(역할) 추가.
    private Mono<Void> addRolesToUserByUserId(String userId, List<RoleRepresentation> roles) {

        // 추가할 역할이 없으면 요청 생략
        if (roles.isEmpty()) return Mono.empty();

        return webClient.post()
                .uri("/users/{userId}/role-mappings/realm", userId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(roles)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Successfully assigned roles {} to user: {}", roles, userId))
                .doOnError(error -> log.error("Failed to assign roles {} to user: {}", roles, userId, error))
                .then();
    }

    // keycloak에 등록된 사용자 UUID로, 해당 사용자에게 할당된 역할 목록 조회.
    public Mono<List<RoleRepresentation>> getUserRoles(String userId) {
        return webClient.get()
                .uri("/users/{userId}/role-mappings/realm", userId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RoleRepresentation>>() {});
    }


    @Data
    public static class UserRepresentation {

        private String id;
        private String username;
        private String email;
        private boolean enabled = true;
        private List<Credentials> credentials;

        public UserRepresentation(@Nullable String id, String username, String email, String password) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.credentials = Collections.singletonList(new Credentials(password));
        }

        @Data
        public static class Credentials {
            private final String type = "password";
            private final String value;
            private final boolean temporary = false;

            public Credentials(String value) {
                this.value = value;
            }
        }
    }

    @Data
    public static class RoleRepresentation {
        private String id;
        private String name;
        private String description;

        public RoleRepresentation(@Nullable String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}
