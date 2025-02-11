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

package com.telus.cis.common.core.exception;

import java.util.Objects;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

import com.telus.cis.common.core.domain.ServiceStatusCode;

import lombok.Getter;

@Getter
public class ApiException extends NestedRuntimeException {

	private static final long serialVersionUID = 1L;

	private final ApiError apiError;

	public ApiException() {
		super(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		apiError = new ApiError();
	}

	public ApiException(ApiError apiError, Throwable cause) {
		super(cause.getLocalizedMessage(), cause);
		this.apiError = apiError;
	}

	public ApiException(ApiError apiError) {
		super(apiError.getMessage());
		this.apiError = apiError;
	}

	public ApiException(ServiceStatusCode statusCode, String message) {
		super(message);
		this.apiError = buildApiError(statusCode, null, message);
	}

	public ApiException(ServiceStatusCode statusCode, String code, String message) {
		super(message);
		this.apiError = buildApiError(statusCode, code, message);

	}

	private ApiError buildApiError(ServiceStatusCode statusCode, String code, String message) {
		return ApiError.builder()
				.message(message)
				.code(Objects.nonNull(code) ? code : statusCode.getCode())
				.reason(statusCode.getStatus().getReasonPhrase())
				.status(statusCode.getStatus().value())
				.build();
	}

}