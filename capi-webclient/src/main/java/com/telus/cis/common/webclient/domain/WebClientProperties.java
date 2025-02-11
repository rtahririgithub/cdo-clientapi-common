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
package com.telus.cis.common.webclient.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("cis.webclient")
public class WebClientProperties {
	
	private String env;
	private String baseUri;
	private String registrationId;
	private Long maxIdleTime = 30000L;
	private Long maxLifeTime = 60000L;
	private Long responseTimeout = 60000L;
	private Long evictInBackground = 120000L;
	private Integer maxConnections = 64;
	private Integer connectionTimeout = 60000;
	private Boolean wiretap = false;
	
}