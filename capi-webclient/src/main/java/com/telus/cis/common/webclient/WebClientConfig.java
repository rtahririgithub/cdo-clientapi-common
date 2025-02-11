/*
 *  Copyright (c) 2022 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */
package com.telus.cis.common.webclient;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.cis.common.webclient.domain.WebClientProperties;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
	
    private static final String PROVIDER_TOKEN = "token";
    
    private final ObjectMapper objectMapper;
    private final WebClientProperties properties;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
	public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		return webClient(authorizedClientManager, properties.getRegistrationId());
	}

	public WebClient authorizedWebClient(String registrationId) {
		
		var clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
		var reactiveAuthorizedClientManager = authorizedClientManager(clientRegistration);
		
		return webClient(reactiveAuthorizedClientManager, registrationId);
	}

	public WebClient webClient(ReactiveOAuth2AuthorizedClientManager reactiveAuthorizedClientManager, String registrationId) {
		
		try {
			var oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(reactiveAuthorizedClientManager);
			oauth2Client.setDefaultClientRegistrationId(registrationId);
			log.info("Webclient setup properties clientId {} [{}].", registrationId, properties);
			
			return webClientBuilder(registrationId, properties.getBaseUri(), MediaType.APPLICATION_JSON_VALUE)
					.filter(oauth2Client)
					.codecs(configurer -> {
						configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
						configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
					}).build();
			
		} catch (Exception e) {
			log.error("Error building webClient:", e);
			throw e;
		}
	}

    /**
     * https://github.com/bingqiao/spring-webflux-security-demo :: WebClientConfig.java
     * authorizedClientService is not available either when TOMCAT is used
     */
    @Bean
	public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository) {
		return authorizedClientManager(clientRegistrationRepository.findByRegistrationId(properties.getRegistrationId()));
	}

	private ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ClientRegistration clientRegistration) {
		
		var clientRegistrationRepository = new InMemoryReactiveClientRegistrationRepository(clientRegistration);
		var authorizedClientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
		var authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
		
		return configureHttpProxy(authorizedClientManager);
	}

	private AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager configureHttpProxy(AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		
		try {
			var tokenResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
			tokenResponseClient.setWebClient(WebClient.builder()
					.clientConnector(reactorClientConnector(PROVIDER_TOKEN))
					.build());
			var authorizedClientProvider = new ClientCredentialsReactiveOAuth2AuthorizedClientProvider();
			authorizedClientProvider.setAccessTokenResponseClient(tokenResponseClient);
			authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

			return authorizedClientManager;

		} catch (Exception e) {
			log.error("Error configuring HttpProxy:", e);
			throw e;
		}
	}

	private ReactorClientHttpConnector reactorClientConnector(String name) {
		
		var providerBuilder = ConnectionProvider.builder(name)
				.maxConnections(properties.getMaxConnections());
		setProvider(properties.getMaxIdleTime(), providerBuilder::maxIdleTime);
		setProvider(properties.getMaxLifeTime(), providerBuilder::maxLifeTime);
		setProvider(properties.getEvictInBackground(), providerBuilder::evictInBackground);
		setProvider(properties.getResponseTimeout(), providerBuilder::pendingAcquireTimeout);

		var httpClient = HttpClient.create(providerBuilder.build())
				.responseTimeout(Duration.ofSeconds(properties.getResponseTimeout()))
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectionTimeout())
				.doOnConnected(conn -> conn.addHandlerFirst(new ReadTimeoutHandler(properties.getConnectionTimeout(), TimeUnit.MILLISECONDS))
						.addHandlerFirst(new WriteTimeoutHandler(properties.getConnectionTimeout(), TimeUnit.MICROSECONDS)))
				.proxyWithSystemProperties()
				.wiretap(properties.getWiretap());

		return new ReactorClientHttpConnector(httpClient);
	}
    
	void setProvider(Long value, Consumer<Duration> setter) {
		if (Objects.nonNull(value) && (value > 0L)) {
			setter.accept(Duration.ofMillis(value));
		}
	}

	public WebClient webClient(String name, String username, String password, String baseUrl) {
		log.info("WebClientConfig.webClient(): Basic Auth configuration {} {}", username, baseUrl);
		return webClient(name, username, password, baseUrl, MediaType.APPLICATION_JSON_VALUE);
	}

	public WebClient webClient(String name, String username, String password, String baseUrl, String mediaType) {
		log.info("WebClientConfig.webClient(): Basic Auth configuration {} {} {}", username, baseUrl, mediaType);
		return webClientBuilder(name, baseUrl, mediaType)
				.defaultHeaders(headers -> headers.setBasicAuth(username, password))
				.build();
	}

	private WebClient.Builder webClientBuilder(String name, String baseUrl, String mediaType) {
		
		log.info("WebClientConfig.webClient(): default configuration");
		return WebClient.builder()
				.baseUrl(StringUtils.isNotBlank(baseUrl) ? baseUrl : properties.getBaseUri())
				.clientConnector(reactorClientConnector(name))
				.defaultHeader(HttpHeaders.CONTENT_TYPE, mediaType)
				.defaultHeader(HttpHeaders.ACCEPT, mediaType)
				.defaultHeader("env", properties.getEnv());
	}


}