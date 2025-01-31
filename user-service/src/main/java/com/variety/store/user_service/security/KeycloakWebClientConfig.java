package com.variety.store.user_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakWebClientConfig {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public WebClient keycloakWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2.setDefaultClientRegistrationId("keycloak-admin-api-client");

        return WebClient.builder()
                .filter(oauth2)
                .baseUrl("http://localhost:8080/admin/realms/variety-store")
                .build();
    }
}
