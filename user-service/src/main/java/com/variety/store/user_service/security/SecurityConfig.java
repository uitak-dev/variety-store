package com.variety.store.user_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;

import java.util.EnumSet;
import java.util.stream.Stream;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ReactiveAuthorizationManager<AuthorizationContext> customAuthorizationManager;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(makeStaticPaths()).permitAll()
                        .anyExchange().access(customAuthorizationManager)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    private String[] makeStaticPaths() {
        EnumSet<StaticResourceLocation> resourceLocations = EnumSet.allOf(StaticResourceLocation.class);

        Stream<String> allPatterns = resourceLocations.stream()
                .flatMap(StaticResourceLocation::getPatterns);

        return allPatterns.toArray(String[]::new);
    }
}
