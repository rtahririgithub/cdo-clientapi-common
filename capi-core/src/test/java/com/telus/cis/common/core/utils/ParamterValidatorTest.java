/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.utils;

import static com.telus.cis.common.core.utils.ParametersValidator.prepareLanguage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.core.domain.ServiceStatusCode;
import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;
import com.telus.cis.common.core.module.EntityModule;
import com.telus.cis.common.core.test.JunitTestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ParamterValidatorTest {
	
	private ValidatorFactory validationFactory;
	private ParametersValidator validator;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() throws Exception {		
		objectMapper = JunitTestUtils.getObjectMapper();
		validationFactory = Validation.buildDefaultValidatorFactory();
		validator = new ParametersValidator(validationFactory);
	}

	@Test
	void validatorStaticTest() {
		
		Locale lang = prepareLanguage(null);
		assertThat(lang, equalTo(Locale.ENGLISH));
		lang = prepareLanguage("EN");
		assertThat(lang, equalTo(Locale.ENGLISH));
		lang = prepareLanguage("FR");
		assertThat(lang, equalTo(Locale.FRENCH));

		try {
			lang = prepareLanguage("XX");
		} catch (ApiException ex) {
			log.info("api error {}", ApiUtils.prettyWrite(objectMapper, ex.getApiError()));
			assertThat(ex.getApiError().getCode(), equalTo("ERR_VALIDATE_001"));
		}
	}

	@Test
	void validationTest() {
		
		EntityModule entity = new EntityModule();
		entity.setId("1");
		entity.setNumberVal1(1L);
		boolean result = validator.validateServiceParameters(entity);
		assertTrue(result);
	}

	@Test
	void validationTest_withValidationError() {
		
		EntityModule entity = new EntityModule();
		try {
			validator.validateServiceParameters(entity);
		} catch (ApiException ex) {
			log.info("api error {}", ApiUtils.prettyWrite(objectMapper, ex.getApiError()));
		}
	}

	@Test
	void validationTest_negativeNumver() {
		
		EntityModule entity = new EntityModule();
		entity.setId("123");
		entity.setNumberVal1(-1);
		try {
			validator.validateServiceParameters(entity);
		} catch (ApiException ex) {
			log.info("api error {}", ApiUtils.prettyWrite(objectMapper, ex.getApiError()));
		}
	}

	@Test
	void validationTest_nullNumver() {
		
		EntityModule entity = new EntityModule();
		entity.setId("123");
		try {
			validator.validateServiceParameters(entity, ServiceStatusCode.STATUS_BAD_REQUEST);
		} catch (ApiException ex) {
			log.info("api error {}", ApiUtils.prettyWrite(objectMapper, ex.getApiError()));
		}
	}

	@Test
	void validationTest_zeroNumver() {
		
		EntityModule entity = new EntityModule();
		entity.setId("123");
		entity.setNumberVal1(0L);
		try {
			validator.validateServiceParameters(entity);
		} catch (ApiException ex) {
			log.info("api error {}", ApiUtils.prettyWrite(objectMapper, ex.getApiError()));
		}
	}

	@Test
	void validationTest_withNotValidable() {
		ApiError apiError = ApiError.builder().build();
		boolean result = validator.validateServiceParameters(apiError);
		assertFalse(result);
	}

}