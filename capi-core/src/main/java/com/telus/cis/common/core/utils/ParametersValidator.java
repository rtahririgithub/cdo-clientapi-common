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

import static com.telus.cis.common.core.domain.ServiceStatusCode.ERROR_VALIDATE;

import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.telus.cis.common.core.domain.ServiceStatusCode;
import com.telus.cis.common.core.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParametersValidator {
	
	private final ValidatorFactory validationFactory;

	public static Locale prepareLanguage(String language) {
		
		Locale langLocale = new Locale(language == null ? "en" : language.trim().toLowerCase());
		if ((Locale.FRENCH.equals(langLocale)) || (Locale.ENGLISH.equals(langLocale))) {
			return langLocale;
		}
		throw new ApiException(ERROR_VALIDATE, "Invalid language: " + language);
	}

	public <T> boolean validateServiceParameters(T validateObject) {
		
		if (!validateObject.getClass().isAnnotationPresent(Validated.class)) {
			log.warn("Class {} is not validatable", validateObject.getClass().getSimpleName());
			return false;
		}
		Validator validator = validationFactory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(validateObject);
		violations.forEach(violation -> {
			log.warn("Validation for [{}] error: {}", validateObject.getClass().getSimpleName(), violation.getMessage());
			throw new ApiException(ERROR_VALIDATE, "Invalid error: " + violation.getMessage());
		});
		
		return true;
	}

	public <T> boolean validateServiceParameters(T validateObject, ServiceStatusCode code) {
		
		if (!validateObject.getClass().isAnnotationPresent(Validated.class)) {
			log.warn("Class {} is not validatable.", validateObject.getClass().getSimpleName());
			return false;
		}
		Validator validator = validationFactory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(validateObject);
		violations.forEach(violation -> {
			log.warn("Validation for [{}] error : {}", validateObject.getClass().getSimpleName(), violation.getMessage());
			throw new ApiException(code, "Invalid error: " + violation.getMessage());
		});
		
		return true;
	}

}