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

package com.telus.cis.common.jaxws;

import lombok.Data;

@Data
public class JaxWsClientProperties {

	private String wsdlResource;
	private String serviceInterface;
	private String endpointAddress;
	private String username;
	private String password;
	private String serviceName;
	private String portName;
	private String namespaceUri;

}