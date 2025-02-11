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

package com.telus.cis.common.webflux.oauth2;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.telus.cis.common.core.domain.SecurityProperties;
import com.telus.cis.common.webflux.oauth2.jwt.ConfigurableJwtIssuerReactiveAuthenticationManagerResolver;
import com.telus.cis.common.webflux.oauth2.jwt.JwtIssuerProperties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "cis.security.oauth2.resourceserver.jwt")
@EnableWebFluxSecurity
public class WebfluxResourceServerConfig {

    private SecurityProperties properties;
    private Map<String, JwtIssuerProperties> issuers;

    @Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

		http.csrf().disable()
			.cors().disable()
			.authorizeExchange()
					.pathMatchers(properties.getPermitList())
					.permitAll()
					.and()
			.authorizeExchange(exchange -> exchange.pathMatchers(properties.getAuthenticateList())
					.hasAuthority("SCOPE_" + properties.getScopeId())
					.anyExchange()
					.authenticated())
			.exceptionHandling()
					.authenticationEntryPoint((serverExchange, exception) -> Mono.error(unauthorizedError(exception)))
					.accessDeniedHandler((serverExchange, exception) -> Mono.error(unauthorizedError(exception)))
					.and()
			// note, we're using our multi-tenancy authentication resolver here to support multiple identity providers
			.oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(new ConfigurableJwtIssuerReactiveAuthenticationManagerResolver(issuers)));
		
		log.info("Security filter setup properties [{}].", properties);

		return http.build();
    }

    /**
     * Security authentication exception and authorization (access denied) exception are wrapped in CompletionException,
     * and handled by common-core::ApiErrorAttributes::getErrorAttributes.
     */
    private CompletionException unauthorizedError(Exception exception) {

		var message = Objects.nonNull(exception.getCause()) && Objects.nonNull(exception.getCause().getMessage()) 
				? exception.getCause().getMessage()
				: HttpStatus.UNAUTHORIZED.getReasonPhrase();

		return new CompletionException(message, exception);
    }

}