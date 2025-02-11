package com.telus.cis.common.core.exception;

import com.telus.cis.common.core.domain.ServiceStatusCode;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		this(ServiceStatusCode.STATUS_NOT_FOUND.getCode(), message);
	}

	public ResourceNotFoundException(String code, String message) {
		super(ApiError.builder()
				.code(code)
				.reason(HttpStatus.NOT_FOUND.getReasonPhrase())
				.status(HttpStatus.NOT_FOUND.value())
				.message(message)
				.build());
	}

}