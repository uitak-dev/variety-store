package com.variety.store.user_service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AuthorizationService {

    // 리소스와 권한 매핑 정보
    private final List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> mappings = new ArrayList<>();

    // 매핑(리소스, 권한) 정보 조회
    public List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> getMappings() {
        return mappings;
    }

    // 매핑(리소스, 권한) 추가
    public void addMapping(String pattern, Set<String> roles) {
        ReactiveAuthorizationManager<AuthorizationContext> authManager = createAuthorizationManager(roles);
        ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.pathMatchers(pattern);

        mappings.add(new ServerWebExchangeMatcherEntry<>(matcher, authManager));
        log.info("Added mapping: {} -> {}", pattern, roles);
    }

    // 매핑(리소스) 제거
    public void removeMapping(String pattern) {
        mappings.removeIf(entry -> entry.getMatcher().toString().equals(pattern));
        log.info("Removed mapping: {}", pattern);
    }

    // 매핑(리소스, 권한) 수정
    public void updateMapping(String pattern, Set<String> newRoles) {
        removeMapping(pattern);
        addMapping(pattern, newRoles);
        log.info("Updated mapping: {} -> {}", pattern, newRoles);
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
}
