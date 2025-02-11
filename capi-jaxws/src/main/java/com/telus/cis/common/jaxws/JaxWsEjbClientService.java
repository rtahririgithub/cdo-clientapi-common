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

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JaxWsEjbClientService {
	
	private JaxWsEjbClientService() { }

	public static final String SECURITY_PRINCIPAL = "security-principal";
	public static final String SECURITY_CREDENTIALS = "security-credentials";

	private static final String INITIAL_CONTEXT_FACTORY = "initial-context-factory";
	private static final String PROVIDER_URL = "provider-url";
	private static final String JNDI_NAME = "jndi-name";
	private static final String BUSINESS_INTERFACE = "business-interface";
	private static final String CACHE_HOME = "cache-home";
	private static final String REFRESH_HOME = "refresh-home-on-connect-failure";
	private static final String LOOKUP_HOME = "lookup-home-on-startup";
	private static final String RESOURCE_REF = "resource-ref";
	private static final String EXPOSE_ACCESS_CONTEXT = "expose-access-context";

	public static SimpleRemoteStatelessSessionProxyFactoryBean getSimpleRemoteStatelessSessionProxyFactory(Map<String, String> ejbProperties)
			throws ClassNotFoundException, NamingException {

		Properties jndiEnvironment = new Properties();
		jndiEnvironment.setProperty(Context.INITIAL_CONTEXT_FACTORY, ejbProperties.get(INITIAL_CONTEXT_FACTORY));
		jndiEnvironment.setProperty(Context.PROVIDER_URL, ejbProperties.get(PROVIDER_URL));
		jndiEnvironment.setProperty(Context.SECURITY_PRINCIPAL, ejbProperties.get(SECURITY_PRINCIPAL));
		jndiEnvironment.setProperty(Context.SECURITY_CREDENTIALS, ejbProperties.get(SECURITY_CREDENTIALS));

		SimpleRemoteStatelessSessionProxyFactoryBean factory = new SimpleRemoteStatelessSessionProxyFactoryBean();
		factory.setJndiName(ejbProperties.get(JNDI_NAME));
		factory.setBusinessInterface(Class.forName(ejbProperties.get(BUSINESS_INTERFACE)));
		factory.setJndiEnvironment(jndiEnvironment);
		factory.setCacheHome(BooleanUtils.toBoolean(ejbProperties.get(CACHE_HOME)));
		factory.setRefreshHomeOnConnectFailure(BooleanUtils.toBoolean(ejbProperties.get(REFRESH_HOME)));
		factory.setLookupHomeOnStartup(BooleanUtils.toBoolean(ejbProperties.get(LOOKUP_HOME)));
		factory.setResourceRef(BooleanUtils.toBoolean(ejbProperties.get(RESOURCE_REF)));
		factory.setExposeAccessContext(BooleanUtils.toBoolean(EXPOSE_ACCESS_CONTEXT));
		// initialize the factory immediately, otherwise to support lazy initialization we will have to save this (configured) factory instance as a bean
		factory.afterPropertiesSet();

		log.info("EjbClientConfig.simpleRemoteStatelessSessionProxyFactory: created new SLSB [{}].", ejbProperties.get(JNDI_NAME));
		return factory;
	}

}