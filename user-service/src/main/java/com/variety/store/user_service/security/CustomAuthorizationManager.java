package com.variety.store.user_service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final AuthorizationService authorizationService;

    @Deprecated
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        return this.authorize(authentication, object)
                .map(result -> new AuthorizationDecision(result.isGranted()));
    }

    @Override
    public Mono<AuthorizationResult> authorize(Mono<Authentication> authentication, AuthorizationContext context) {

        ServerWebExchange exchange = context.getExchange();

        return Flux.fromIterable(authorizationService.getMappings())
                .concatMap(mapping -> mapping.getMatcher()
                        .matches(exchange)
                        .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                        .flatMap(matchResult -> {
                            log.debug("Matched path: '{}', checking authorization", exchange.getRequest().getPath());
                            return mapping.getAuthorizationManager()
                                    .authorize(authentication, new AuthorizationContext(exchange, matchResult.getVariables()))
                                    .doOnNext(result -> log.debug("Authorization result for '{}': {}", exchange.getRequest().getPath(), result.isGranted()));
                        })
                )
                .next()
                .map(result -> {
                    if (!result.isGranted()) {
                        log.warn("Access denied for '{}'", exchange.getRequest().getPath());
                    }
                    return result;
                })
                // 요청에 대해 매칭되는 자원 경로가 존재하지 않은 경우 허용.( 보안이 중요한 경우, false 로 수정. )
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No matching path found for '{}', allowing access", exchange.getRequest().getPath());
                    return Mono.just(new AuthorizationDecision(true));
                }))
                .onErrorResume(error -> {
                    log.error("Authorization error: {}", error.getMessage());
                    return Mono.just(new AuthorizationDecision(false));
                });
    }
}
