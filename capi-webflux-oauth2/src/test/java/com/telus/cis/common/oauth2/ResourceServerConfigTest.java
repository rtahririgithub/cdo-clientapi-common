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

package com.telus.cis.common.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec.Access;
import org.springframework.security.config.web.server.ServerHttpSecurity.CorsSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.ExceptionHandlingSpec;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.telus.cis.common.core.domain.SecurityProperties;
import com.telus.cis.common.webflux.oauth2.WebfluxResourceServerConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceServerConfigTest {

	private ServerHttpSecurity httpSecurity;
	private WebfluxResourceServerConfig resourceConfig;
	private SecurityProperties securityProperties;

	@BeforeEach
	void setUp() throws Exception {
		
		securityProperties = new SecurityProperties();
		resourceConfig = new WebfluxResourceServerConfig();
		httpSecurity = mock(ServerHttpSecurity.class);

		resourceConfig.setProperties(securityProperties);

		// setup csrf
		CsrfSpec csrf = mock(CsrfSpec.class);
		doReturn(httpSecurity).when(csrf).disable();
		doReturn(csrf).when(httpSecurity).csrf();

		// setup cors
		CorsSpec cors = mock(CorsSpec.class);
		doReturn(cors).when(httpSecurity).cors();
		doReturn(httpSecurity).when(cors).disable();

		AuthorizeExchangeSpec authExchange = mock(AuthorizeExchangeSpec.class);
		doReturn(authExchange).when(httpSecurity).authorizeExchange();

		Access permit = mock(Access.class);
		doReturn(permit).when(authExchange).pathMatchers(anyString());
		doReturn(authExchange).when(permit).permitAll();
		doReturn(httpSecurity).when(authExchange).and();

//        ServerHttpSecurity severSecurity = mock( ServerHttpSecurity.class );
		doReturn(httpSecurity).when(httpSecurity).authorizeExchange(any());

		ExceptionHandlingSpec expectSpec = mock(ExceptionHandlingSpec.class);
		doReturn(expectSpec).when(httpSecurity).exceptionHandling();
		doReturn(expectSpec).when(expectSpec).authenticationEntryPoint(any());

		doReturn(expectSpec).when(expectSpec).accessDeniedHandler(any());

		doReturn(httpSecurity).when(expectSpec).and();
		doReturn(httpSecurity).when(httpSecurity).oauth2ResourceServer(any());
	}

//    @Test
//    void test_springSecurityFilterChain()
//    {
//        SecurityWebFilterChain result = resourceConfig.springSecurityFilterChain( httpSecurity );
//        log.info( "result {}", result );
//        assertNull( result );
//    }

	@Test
	void test_unauthorizedError() {
		
		ClassNotFoundException cause = new ClassNotFoundException("testing"); // ReflectiveOperationException without cause

		CompletionException result = ReflectionTestUtils.invokeMethod(resourceConfig, "unauthorizedError", cause);
		log.info("result {} {}", result.getMessage(), result.getCause().getClass().getCanonicalName());
		assertEquals("Unauthorized", result.getMessage());
		log.info("isAssignableFrom {}", Exception.class.isAssignableFrom(result.getCause().getClass()));
		log.info("isAssignableFrom {}", result.getCause().getClass().isAssignableFrom(Exception.class));
		log.info("isInstances {}", Exception.class.isInstance(result.getCause()));
		assertTrue(ClassNotFoundException.class.isInstance(result.getCause()));
		assertEquals("testing", result.getCause().getMessage());

		AuthorizationServiceException acessEx = new AuthorizationServiceException("access", cause);
		result = ReflectionTestUtils.invokeMethod(resourceConfig, "unauthorizedError", acessEx);
		log.info("result {} {}", result.getMessage(), result.getCause().getClass().getCanonicalName());
		assertTrue(AccessDeniedException.class.isInstance(result.getCause()));
		assertEquals("testing", result.getMessage());

		AuthenticationServiceException authEx = new AuthenticationServiceException("authEx", cause);
		result = ReflectionTestUtils.invokeMethod(resourceConfig, "unauthorizedError", authEx);
		log.info("result {} {}", result.getMessage(), result.getCause().getClass().getCanonicalName());
		assertTrue(AuthenticationException.class.isInstance(result.getCause()));
		assertEquals("testing", result.getMessage());

		RuntimeException exception = new RuntimeException(cause); // inject a cause to runtimeException
		result = ReflectionTestUtils.invokeMethod(resourceConfig, "unauthorizedError", exception);
		log.info("result {}", result.getMessage());
		assertEquals("testing", result.getMessage());
	}

}