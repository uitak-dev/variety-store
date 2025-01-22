package com.variety.store.user_service.security;

import com.variety.store.user_service.repository.RoleRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final WebClient keycloakWebClient;

    // 사용자 생성
    public Mono<Void> createUser(String id, String username, String email, String password) {

        return keycloakWebClient.post()
                .uri("/users")
                .bodyValue(new UserRepresentation(id, username, email, password))
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 사용자 정보 수정
    public Mono<Void> updateUser(String userId, Map<String, Object> updatedFields) {
        if (updatedFields == null || updatedFields.isEmpty()) {
            return Mono.error(new IllegalArgumentException("수정할 필드가 제공되지 않았습니다."));
        }

        return keycloakWebClient.put()
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
                .flatMap(userId -> keycloakWebClient.delete()
                        .uri("/users/{userId}", userId)
                        .retrieve()
                        .bodyToMono(Void.class)
                )
                .doOnSuccess(unused -> log.info("사용자 삭제 완료: " + email))
                .doOnError(error -> log.error("사용자 삭제 실패: " + error.getMessage()));
    }

    // 권한 생성
    public Mono<Void> createRole(String id, String roleName, String description) {

        return keycloakWebClient.post()
                .uri("/roles")
                .bodyValue(new RoleRepresentation(id, roleName, description))
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 권한 수정
    public Mono<Void> updateRole(String roleName, Map<String, Object> updatedFields) {
        if (updatedFields == null || updatedFields.isEmpty()) {
            return Mono.error(new IllegalArgumentException("수정할 필드가 제공되지 않았습니다."));
        }

        return keycloakWebClient.put()
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
        return keycloakWebClient.delete()
                .uri("/roles/{roleName}", roleName)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 사용자 특정 권한(Role) 추가.
    public Mono<Void> addRoleToUser(String userId, String roleName) {
        return keycloakWebClient.get()
                .uri("/roles/{roleName}", roleName)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(newRole -> keycloakWebClient.get()
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
                            return keycloakWebClient.post()
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
        return keycloakWebClient.get()
                .uri("/roles/{roleName}", roleName)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(roleToRemove ->
                        keycloakWebClient.get()
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
                                    return keycloakWebClient.post()
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
                .flatMap(roleName -> keycloakWebClient.get()
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
                    return keycloakWebClient.post()
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
        return keycloakWebClient.get()
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

    static class UserRepresentation {

        private final String id;
        private final String username;
        private final String email;
        private final boolean enabled = true;
        private final Credentials credentials;

        public UserRepresentation(String id, String username, String email, String password) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.credentials = new Credentials(password);
        }

        public static class Credentials {
            private final String type = "password";
            private final String value;
            private final boolean temporary = false;

            public Credentials(String value) {
                this.value = value;
            }
        }
    }

    static class RoleRepresentation {
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
