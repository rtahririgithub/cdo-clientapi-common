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

package com.telus.cis.common.core.domain;

import lombok.Data;

@Data
public class SecurityProperties {
	
    private String scopeId;
    private String resourceId;
    private String[] permitList = new String[] { "/*" };
    private String[] authenticateList = new String[] { "/**" };
    
}