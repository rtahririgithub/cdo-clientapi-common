package com.telus.cis.common.core.exception;

import com.telus.cis.common.core.domain.ServiceStatusCode;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		this(ServiceStatusCode.STATUS_BAD_REQUEST.getCode(), message);
	}

	public ValidationException(String code, String message) {
		super(ApiError.builder()
				.code(code)
				.reason(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.status(HttpStatus.BAD_REQUEST.value())
				.message(message)
				.build());
	}

}