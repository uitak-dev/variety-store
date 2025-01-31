package com.variety.store.user_service.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Slf4j
@Service
public class AuthorizationService {

    // 매핑(리소스, 권한) 정보 조회
    // 리소스와 권한 매핑 정보
    private final List<AuthorizationMapping> mappings = new ArrayList<>();

    // 매핑(리소스, 권한) 추가
    public void addMapping(Long priority, String pattern, Set<String> roles) {
        ReactiveAuthorizationManager<AuthorizationContext> authManager = createAuthorizationManager(roles);
        ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.pathMatchers(pattern);

        mappings.add(new AuthorizationMapping(priority, matcher, authManager));

        // 우선순위(priority) 기준으로 오름차순 정렬.
        mappings.sort(Comparator.comparingLong(AuthorizationMapping::getPriority));

        log.info("Added mapping: priority={}, pattern={}, roles={}", priority, pattern, roles);
    }

    // 매핑(리소스) 제거
    public void removeMapping(String pattern) {
        mappings.removeIf(entry -> entry.getMatcher().toString().equals(pattern));
        log.info("Removed mapping: {}", pattern);
    }

    // 매핑(리소스, 권한) 수정
    public void updateMapping(Long priority, String pattern, Set<String> roles) {
        removeMapping(pattern);
        addMapping(priority, pattern, roles);

        // 우선순위(priority) 기준으로 오름차순 정렬.
        mappings.sort(Comparator.comparingLong(AuthorizationMapping::getPriority));

        log.info("Updated mapping: priority={}, pattern={}, roles={}", priority, pattern, roles);
    }

    // 역할 기반 ReactiveAuthorizationManager 생성
    private ReactiveAuthorizationManager<AuthorizationContext> createAuthorizationManager(Set<String> roles) {
        return (authentication, context) -> authentication.map(auth -> {
            Collection<? extends GrantedAuthority> userRoles = auth.getAuthorities();
            boolean hasAccess = userRoles.stream()
                    .anyMatch(grantedAuthority -> roles.contains(grantedAuthority.getAuthority()));
            return new AuthorizationDecision(hasAccess);
        });
    }

    @AllArgsConstructor
    @Getter
    public static class AuthorizationMapping {

        private final Long priority;
        private final ServerWebExchangeMatcher matcher;
        private final ReactiveAuthorizationManager<AuthorizationContext> authorizationManager;
    }
}
