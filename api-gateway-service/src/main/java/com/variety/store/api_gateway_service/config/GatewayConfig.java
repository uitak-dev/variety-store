package com.variety.store.api_gateway_service.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // User-Service 경로 설정
                .route("user-service-route", route -> route
                        .path("/user-service/**")
                        .filters(filter -> filter
                                .rewritePath("/user-service/(?<segment>.*)", "/${segment}")
                                .removeRequestHeader("Cookie")
                        )
                        .uri("lb://USER-SERVICE")
                )
                // Order-Service 경로 설정
                .route("order-service-route", route -> route
                        .path("/order-service/**")
                        .filters(filter -> filter
                                .rewritePath("/order-service/(?<segment>.*)", "/${segment}")
                                .removeRequestHeader("Cookie")
                        )
                        .uri("lb://ORDER-SERVICE")
                )
                .build();
    }
}
