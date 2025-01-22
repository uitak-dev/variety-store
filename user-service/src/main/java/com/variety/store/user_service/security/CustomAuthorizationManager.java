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
        return null;
    }

    @Override
    public Mono<AuthorizationResult> authorize(Mono<Authentication> authentication, AuthorizationContext context) {

        ServerWebExchange exchange = context.getExchange();

        return Flux.fromIterable(authorizationService.getMappings())
                .concatMap((mapping) -> mapping.getMatcher()
                        .matches(exchange)
                        .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                        .map(ServerWebExchangeMatcher.MatchResult::getVariables)
                        .flatMap((variables) -> {
                            log.debug("Checking authorization on '{}' using {}",
                                    exchange.getRequest().getPath().pathWithinApplication(),
                                    mapping.getEntry());

                            return mapping.getEntry()
                                    .authorize(authentication, new AuthorizationContext(exchange, variables))
                                    .filter(AuthorizationResult::isGranted);
                        })
                )
                .next()
                .switchIfEmpty(Mono.just(new AuthorizationDecision(false)));
    }
}
