package com.variety.store.api_gateway_service.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class UserTokenRelayGlobalFilter implements GlobalFilter {

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return exchange.getPrincipal().flatMap(principal -> {
            if (principal instanceof OAuth2AuthenticationToken authenticationToken) {
                // Load Authorized Client
                return authorizedClientRepository.loadAuthorizedClient(
                                authenticationToken.getAuthorizedClientRegistrationId(),
                                authenticationToken,
                                exchange
                        )
                        .map(OAuth2AuthorizedClient::getAccessToken)
                        .map(accessToken -> {
                            // 요청 헤더에 Bearer Token 추가
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                    .request(exchange.getRequest()
                                            .mutate()
                                            .header(HttpHeaders.AUTHORIZATION,
                                                    "Bearer " + accessToken.getTokenValue())
                                            .build()
                                    )
                                    .build();
                            return mutatedExchange;
                        })
                        .flatMap(chain::filter);
            }
            // Continue if principal is not OAuth2AuthenticationToken
            return chain.filter(exchange);
        }).switchIfEmpty(chain.filter(exchange));
    }
}
