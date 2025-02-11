package com.telus.cis.common.core.exception;

import com.telus.cis.common.core.domain.ServiceStatusCode;

import org.springframework.http.HttpStatus;

public class MethodNotAllowedException extends ApiException {

	private static final long serialVersionUID = 1L;

	public MethodNotAllowedException(String message) {
		this(ServiceStatusCode.STATUS_METHOD_NOT_ALLOWED.getCode(), message);
	}

	public MethodNotAllowedException(String code, String message) {
		super(ApiError.builder()
				.code(code)
				.reason(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
				.status(HttpStatus.METHOD_NOT_ALLOWED.value())
				.message(message)
				.build());
	}

}