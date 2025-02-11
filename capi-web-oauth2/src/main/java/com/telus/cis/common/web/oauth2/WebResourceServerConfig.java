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

package com.telus.cis.common.web.oauth2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.cis.common.core.domain.SecurityProperties;
import com.telus.cis.common.core.exception.ApiException;
import com.telus.cis.common.core.exception.ForbiddenException;
import com.telus.cis.common.web.oauth2.jwt.ConfigurableJwtIssuerAuthenticationManagerResolver;
import com.telus.cis.common.web.oauth2.jwt.JwtIssuerProperties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "cis.security.oauth2.resourceserver.jwt")
@RequiredArgsConstructor
@EnableWebSecurity
public class WebResourceServerConfig {
    
	private static final String ACCESS_DENIED = "Access denied.";
	
    private final ObjectMapper objectMapper;
    
    private SecurityProperties properties;
    private Map<String, JwtIssuerProperties> issuers;    
    
    @Bean
    public SecurityFilterChain httpSecurityConfig(HttpSecurity http) throws Exception {
    	
		http.csrf().disable()
	        .cors().disable()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
            .authorizeHttpRequests()
				.antMatchers(properties.getPermitList())
				.permitAll()
				.and()
        	.authorizeHttpRequests(auth -> auth.antMatchers(properties.getAuthenticateList())
        			.hasAuthority("SCOPE_" + properties.getScopeId())
        			.anyRequest()
        			.authenticated())
        		.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint())
				.accessDeniedHandler(accessDeniedHandler())
        		.and()	
        	// note, we're using our multi-tenancy authentication resolver here to support multiple identity providers
       		.oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(new ConfigurableJwtIssuerAuthenticationManagerResolver(issuers)));
		
		log.info( "Security filter setup properties [{}].", properties.toString());
		
		return http.build();
    }
    
	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		
		return new AuthenticationEntryPoint() {
		
			@Override
			public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
				
				var errorMessage = Optional.ofNullable(authenticationException)
						.filter(OAuth2AuthenticationException.class::isInstance)
						.map(OAuth2AuthenticationException.class::cast)
						.map(OAuth2AuthenticationException::getError)
						.map(err -> String.format("OAuth2 error: %s", err.getDescription()))
						.orElseGet(() -> String.format("Authentication error: %s", authenticationException.getMessage()));
				log.error("AuthenticationEntryPoint error {}.", errorMessage);
				sendErrorResponse(response, new ForbiddenException(errorMessage));
			}
		};
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		
		return new AccessDeniedHandler() {
		
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
				
				var errorMessage = Optional.ofNullable(accessDeniedException)
						.map(ex -> String.format("AccessDeniedException: %s", ex.getMessage()))
						.orElse(ACCESS_DENIED);
				log.error("AccessDeniedHandler error {}.", errorMessage);
				sendErrorResponse(response, new ForbiddenException(errorMessage));
			}
		};
	}
    
	private void sendErrorResponse(HttpServletResponse response, ApiException error) throws IOException {
		
		var apiError = error.getApiError();
		
		response.setStatus(apiError.getStatus());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		
		OutputStream out = response.getOutputStream();
		objectMapper.writeValue(out, apiError);
		out.flush();
	}
    
}