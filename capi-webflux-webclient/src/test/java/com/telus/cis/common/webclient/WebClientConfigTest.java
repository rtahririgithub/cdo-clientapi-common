/*
 *  Copyright (c) 2023 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.webclient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.core.test.JunitTestUtils;
import com.telus.cis.common.webflux.webclient.WebClientConfig;
import com.telus.cis.common.webflux.webclient.domain.WebClientProperties;

class WebClientConfigTest {

	private WebClient webClient;
	private WebClientConfig webClientConfig;
	private WebClientProperties clientProperties;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() throws Exception {
		objectMapper = JunitTestUtils.getObjectMapper();
		clientProperties = new WebClientProperties();
		clientProperties.setEnv("it02");
		clientProperties.setBaseUri("https://enterprisemessagewebk-is02.tsl.telus.com:443");

		webClientConfig = new WebClientConfig(objectMapper, clientProperties);
		webClient = webClientConfig.webClient("test", "test_user", "test_password", null);
	}

	@Test
	void test() {
		assertNotNull(webClient);
		String propertyString = clientProperties.toString();
		assertFalse(propertyString.contains("test_password"));
	}

}