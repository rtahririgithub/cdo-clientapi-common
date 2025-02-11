/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 */
package com.telus.cis.common.amdocs;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBObject;

import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;

import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;

import amdocs.APILink.accesscontrol.APIAccessInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmdocsTransaction
{
    
	private APIAccessInfo accessInfo;
	private List<EJBObject> beans = new ArrayList<>();

	public AmdocsTransaction(APIAccessInfo accessInfo) {
		this.accessInfo = accessInfo;
	}

	public void close() {
		log.debug("Closing amdocs transaction [" + this + "]");
		try {
			for (EJBObject bean : beans) {
				close(bean);
			}
			accessInfo.getContext().close();
		} catch (Exception e) {
			log.error("Error closing amdocs transaction: {}", e.getMessage(), e);
		}
	}

	private void close(EJBObject bean) {
		try {
			bean.remove();
		} catch (Exception e) {
			log.warn("Error removing amdocs bean [{}]", bean);
			log.trace("exception", e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T createBean(Class<T> beanType) {
		log.debug("Creating a new amdocs bean instance of type [" + beanType.getName() + "]");

		Object beanHome = getBeanHome(beanType);
		EJBObject bean = (EJBObject) ReflectionUtils
				.invokeMethod(ReflectionUtils.findMethod(beanHome.getClass(), "create"), beanHome);
		beans.add(bean);

		log.debug("Created amdocs stateful bean [" + bean + "]");

		return (T) bean;
	}

	
	@SuppressWarnings("unchecked")
	private <T> T getBeanHome(Class<?> beanClass) {

		String beanJndiName = "amdocsBeans." + beanClass.getSimpleName() + "Home";
		log.debug("Looking up for amdocs bean [{}]...", beanJndiName);

		try {
			return (T) accessInfo.getContext().lookup(beanJndiName);
		} catch (Exception e) {

			ApiError apiError = ApiError.builder().code("UNKNOWN_ERROR")
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.reason("UNKNOWN_ERROR").message(e.getLocalizedMessage()).build();

			throw new ApiException(apiError, e);
		}
	}

}