package com.variety.store.user_service.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakService {

    private final WebClient webClient;

    public KeycloakService(@Qualifier("keycloakWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // 사용자 생성
    public Mono<Void> createUser(Long id, String username, String email, String password) {

        UserRepresentation userRepresentation = new UserRepresentation(id.toString(), username, email, password);
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

    // 사용자 정보 수정
    public Mono<Void> updateUser(String userId, Map<String, Object> updatedFields) {
        if (updatedFields == null || updatedFields.isEmpty()) {
            return Mono.error(new IllegalArgumentException("수정할 필드가 제공되지 않았습니다."));
        }

        return webClient.put()
                .uri("/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedFields)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> log.info("사용자 정보가 업데이트되었습니다: {}", userId))
                .doOnError(e -> log.error("사용자 수정 실패: {}", e.getMessage()));
    }

    // 사용자 삭제
    public Mono<Void> deleteUser(String email) {

        return getUserIdByEmail(email)
                .flatMap(userId -> webClient.delete()
                        .uri("/users/{userId}", userId)
                        .retrieve()
                        .bodyToMono(Void.class)
                )
                .doOnSuccess(unused -> log.info("사용자 삭제 완료: " + email))
                .doOnError(error -> log.error("사용자 삭제 실패: " + error.getMessage()));
    }

    // 권한 생성
    public Mono<Void> createRole(Long id, String roleName, String description) {

        return webClient.post()
                .uri("/roles")
                .bodyValue(new RoleRepresentation(id.toString(), roleName, description))
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 권한 수정
    public Mono<Void> updateRole(String roleName, Map<String, Object> updatedFields) {
        if (updatedFields == null || updatedFields.isEmpty()) {
            return Mono.error(new IllegalArgumentException("수정할 필드가 제공되지 않았습니다."));
        }

        return webClient.put()
                .uri("/roles/{roleName}", roleName)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedFields)
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

    // 사용자 특정 권한(Role) 추가.
    public Mono<Void> addRoleToUser(String userId, String roleName) {
        return webClient.get()
                .uri("/roles/{roleName}", roleName)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(newRole -> webClient.get()
                        .uri("/users/{userId}/role-mappings/realm", userId)
                        .retrieve()
                        .bodyToMono(List.class)
                        .flatMap(currentRoles -> {
                            // 현재 권한 목록에서 중복 체크
                            boolean alreadyHasRole = ((List<Map<String, Object>>) currentRoles)
                                    .stream()
                                    .anyMatch(role -> role.get("id").equals(newRole.get("id")));

                            if (alreadyHasRole) {
                                return Mono.error(new IllegalArgumentException("사용자는 이미 해당 권한을 가지고 있습니다."));
                            }

                            // 기존 역할에 새로운 역할을 추가한 리스트
                            List<Map<String, Object>> updatedRoles = new ArrayList<>(currentRoles);
                            updatedRoles.add(Map.of(
                                    "id", newRole.get("id"),
                                    "name", newRole.get("name")
                            ));

                            // 업데이트된 역할을 Keycloak에 적용 (POST)
                            return webClient.post()
                                    .uri(uriBuilder -> uriBuilder
                                            .path("/users/{userId}/role-mappings/realm")
                                            .build(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(updatedRoles)
                                    .retrieve()
                                    .bodyToMono(Void.class);
                        })
                );
    }

    // 사용자 특정 권한(Role) 제거.
    public Mono<Void> removeRoleFromUser(String userId, String roleName) {
        return webClient.get()
                .uri("/roles/{roleName}", roleName)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(roleToRemove ->
                        webClient.get()
                                .uri("/users/{userId}/role-mappings/realm", userId)
                                .retrieve()
                                .bodyToMono(List.class)
                                .flatMap(currentRoles -> {
                                    // 현재 역할 목록에서 제거할 역할을 제외한 새로운 리스트 생성
                                    List<Map<String, Object>> updatedRoles = ((List<Map<String, Object>>) currentRoles)
                                            .stream()
                                            .filter(role -> !role.get("id").equals(roleToRemove.get("id")))
                                            .toList();

                                    if (updatedRoles.size() == currentRoles.size()) {
                                        return Mono.error(new IllegalArgumentException("지정된 권한이 사용자에게 할당되어 있지 않습니다."));
                                    }

                                    // 업데이트된 역할 목록을 POST 요청으로 Keycloak에 적용
                                    return webClient.post()
                                            .uri("/users/{userId}/role-mappings/realm", userId)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(updatedRoles)
                                            .retrieve()
                                            .bodyToMono(Void.class);
                                })
                );
    }

    // 사용자 권한 수정.
    public Mono<Void> updateUserRoles(String userId, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Mono.error(new IllegalArgumentException("권한 목록이 비어있습니다."));
        }

        return Flux.fromIterable(roleNames)
                .flatMap(roleName -> webClient.get()
                        .uri("/roles/{roleName}", roleName)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .onErrorResume(e -> {
                            log.error("권한 조회 실패: {}", roleName, e);
                            return Mono.error(new IllegalArgumentException("권한을 찾을 수 없습니다: " + roleName));
                        })
                )
                .collectList()
                .flatMap(roleList -> {
                    if (roleList.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("모든 역할이 유효하지 않습니다."));
                    }

                    // 사용자 역할 업데이트 요청 (기존 역할은 무시하고, 새로운 역할 목록 적용)
                    return webClient.post()
                            .uri("/users/{userId}/role-mappings/realm", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(roleList)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .doOnSuccess(unused -> log.info("사용자의 역할이 업데이트 되었습니다. 사용자 ID: {}", userId))
                            .doOnError(e -> log.error("사용자 역할 업데이트 실패. 사용자 ID: {}", userId, e));
                })
                .onErrorResume(e -> {
                    log.error("사용자 역할 업데이트 중 오류 발생: {}", e.getMessage());
                    return Mono.error(new RuntimeException("사용자 역할 업데이트 중 문제가 발생했습니다."));
                });
    }

    // 사용자 email로, keycloak에 등록된 사용자의 uuid 조회.
    public Mono<String> getUserIdByEmail(String email) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(Map[].class)
                .flatMap(users -> {
                    if (users.length > 0 && users[0].get("id") != null) {
                        return Mono.just(users[0].get("id").toString());
                    } else {
                        return Mono.error(new IllegalArgumentException("이메일을 찾을 수 없습니다: " + email));
                    }
                });
    }

    @Data
    public static class UserRepresentation {

        private final String id;
        private final String username;
        private final String email;
        private final boolean enabled = true;
        private final List<Credentials> credentials;

        public UserRepresentation(String id, String username, String email, String password) {
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
        private final String id;
        private final String name;
        private final String description;

        public RoleRepresentation(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}
