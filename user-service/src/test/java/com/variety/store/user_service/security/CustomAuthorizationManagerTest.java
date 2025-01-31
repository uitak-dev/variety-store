package com.variety.store.user_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
@Import(SecurityConfig.class)
class CustomAuthorizationManagerTest {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private CustomAuthorizationManager customAuthorizationManager;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // 권한 매핑 추가 (순서가 중요한 경우 priority 값을 설정)
        authorizationService.addMapping(1L, "/user/**", Set.of("ROLE_USER"));
        authorizationService.addMapping(3L, "/admin/**", Set.of("ROLE_ADMIN"));
    }

    @Test
    void testAuthorizeGranted() {
        // ROLE_USER가 /user/profile 요청할 때 접근 가능해야 함
        Authentication authentication = mockAuthenticationWithRole("ROLE_USER");
        AuthorizationContext context = new AuthorizationContext(mockServerWebExchange("/user/profile"), Map.of());

        Mono<Boolean> authorizationResult = customAuthorizationManager
                .authorize(Mono.just(authentication), context)
                .map(result -> result.isGranted());

        assertThat(authorizationResult.block()).isTrue();
    }

    @Test
    void testAuthorizeDenied() {
        // ROLE_GUEST가 /admin/dashboard 요청할 때 접근 불가능해야 함
        Authentication authentication = mockAuthenticationWithRole("ROLE_GUEST");
        AuthorizationContext context = new AuthorizationContext(mockServerWebExchange("/admin/dashboard"), Map.of());

        Mono<Boolean> authorizationResult = customAuthorizationManager
                .authorize(Mono.just(authentication), context)
                .map(result -> result.isGranted());

        assertThat(authorizationResult.block()).isFalse();
    }

    // Mock Authentication 객체 생성
    private Authentication mockAuthenticationWithRole(String role) {
        return new TestingAuthenticationToken("testUser", "testPassword", role);
    }

    // Mock ServerWebExchange 생성
    private ServerWebExchange mockServerWebExchange(String path) {
        // MockServerHttpRequest를 사용하여 요청 생성.
        MockServerHttpRequest request = MockServerHttpRequest
                .get(path)
                .build();

        MockServerHttpResponse response = new MockServerHttpResponse();
        WebSessionManager sessionManager = new DefaultWebSessionManager();
        ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();

        AcceptHeaderLocaleContextResolver localeContextResolver = new AcceptHeaderLocaleContextResolver();
        localeContextResolver.setDefaultLocale(Locale.KOREAN);

        // DefaultServerWebExchange를 사용하여 Mock ServerWebExchange 생성.
        return new DefaultServerWebExchange(request, response, sessionManager, codecConfigurer, localeContextResolver);
    }
}