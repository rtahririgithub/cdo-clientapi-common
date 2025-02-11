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

package com.telus.cis.common.webflux.oauth2.jwt;

import lombok.Data;

@Data
public class JwtIssuerProperties {
	
	private String name;
    private String issuerUri;
    private String jwkSetUriAccessToken;
    private String jwkSetUriIdToken;
    private String audience;
    
}