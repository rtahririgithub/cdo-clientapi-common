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

import static com.telus.cis.common.core.utils.ApiUtils.setIfNullWithSecretValidation;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.remoting.jaxws.JaxWsPortProxyFactoryBean;

import com.telus.cis.common.core.exception.ValidationException;

public class JaxWsClientServicePort {
	
	private JaxWsClientServicePort() { }

	private static JaxWsPortProxyFactoryBean getJaxWsPortProxyFactory(JaxWsClientProperties clientProperties) throws IOException, ClassNotFoundException {
		
		JaxWsPortProxyFactoryBean factory = new JaxWsPortProxyFactoryBean();

		Resource wsdlResource = new ClassPathResource(clientProperties.getWsdlResource());
		factory.setWsdlDocumentResource(wsdlResource);
		factory.setServiceInterface(Class.forName(clientProperties.getServiceInterface()));
		factory.setEndpointAddress(clientProperties.getEndpointAddress());
		factory.setUsername(clientProperties.getUsername());
		factory.setPassword(clientProperties.getPassword());
		factory.setServiceName(clientProperties.getServiceName());
		factory.setPortName(clientProperties.getPortName());
		factory.setNamespaceUri(clientProperties.getNamespaceUri());
		// initialize the factory immediately, otherwise to support lazy initialization we will have to save this (configured) factory instance as a bean
		factory.afterPropertiesSet();

		return factory;
	}

	@SuppressWarnings("unchecked")
	public static <T> T createJaxWsClientServicePort(JaxWsClientProperties clientProperties, String username, String password) throws ClassNotFoundException, IOException {
		
		setIfNullWithSecretValidation(clientProperties::getUsername, clientProperties::setUsername, username);
		setIfNullWithSecretValidation(clientProperties::getPassword, clientProperties::setPassword, password);
		if (StringUtils.isAnyBlank(clientProperties.getUsername(), clientProperties.getPassword())) {
			throw new ValidationException("invalid username: [" + clientProperties.getUsername() + "]/password");
		}
		
		return (T) getJaxWsPortProxyFactory(clientProperties).getObject();
	}

	public static <T> T createJaxWsClientServicePort(JaxWsClientProperties clientProperties) throws ClassNotFoundException, IOException {
		return createJaxWsClientServicePort(clientProperties, null, null);
	}

}