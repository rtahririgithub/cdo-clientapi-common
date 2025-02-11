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

package com.telus.cis.common.webflux.webclient.domain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class ServiceProperties {
	
	private String baseUri;
	private String path;
	private List<String> resources;

	public String getUriPathResource(int resource) {
		return StringUtils.trimToEmpty(baseUri) + StringUtils.trimToEmpty(path) + StringUtils.trimToEmpty(resources.get(resource));
	}
	
}