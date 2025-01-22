package com.variety.store.user_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class CustomAuthorizationManagerTest {

    private CustomAuthorizationManager customAuthorizationManager;
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        // AuthorizationService의 실제 구현체 생성
        authorizationService = new AuthorizationService();
        customAuthorizationManager = new CustomAuthorizationManager(authorizationService);

        // 권한 매핑 추가
        authorizationService.addMapping("/user/**", Set.of("ROLE_USER"));
        authorizationService.addMapping("/admin/**", Set.of("ROLE_ADMIN"));
    }

    @Test
    void testAuthorizeGranted() {
        // 인증 및 매핑 설정
        Mono<Authentication> authentication = Mono.just(mockAuthenticationWithRole("ROLE_USER"));
        AuthorizationContext context = new AuthorizationContext(mockServerWebExchange("/user/profile"), Map.of());

        // 권한 승인 검증
        StepVerifier.create(customAuthorizationManager.authorize(authentication, context))
                .expectNextMatches(AuthorizationResult::isGranted)
                .verifyComplete();
    }

    @Test
    void testAuthorizeDenied() {
        // 인증 및 매핑 설정
        Mono<Authentication> authentication = Mono.just(mockAuthenticationWithRole("ROLE_GUEST"));
        AuthorizationContext context = new AuthorizationContext(mockServerWebExchange("/admin/dashboard"), Map.of());

        // 권한 거부 검증
        StepVerifier.create(customAuthorizationManager.authorize(authentication, context))
                .expectNextMatches(result -> !result.isGranted())
                .verifyComplete();
    }

    // Mock Authentication 객체 생성
    private Authentication mockAuthenticationWithRole(String role) {
        return new TestingAuthenticationToken("testUser", "testPassword", role);
    }

    // Mock ServerWebExchange 생성
    private ServerWebExchange mockServerWebExchange(String path) {

        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, URI.create(path))
                .build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        WebSessionManager sessionManager = new DefaultWebSessionManager();
        ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();

        AcceptHeaderLocaleContextResolver localeContextResolver = new AcceptHeaderLocaleContextResolver();
        localeContextResolver.setDefaultLocale(Locale.KOREAN); // 기본 Locale 설정

        return new DefaultServerWebExchange(request, response, sessionManager, codecConfigurer, localeContextResolver);
    }
}