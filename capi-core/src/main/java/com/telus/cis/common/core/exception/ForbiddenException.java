package com.telus.cis.common.core.exception;

import com.telus.cis.common.core.domain.ServiceStatusCode;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ForbiddenException(String message) {
		this(ServiceStatusCode.STATUS_FORBIDDEN.getCode(), message);
	}

	public ForbiddenException(String code, String message) {
		super(ApiError.builder()
				.code(code)
				.reason(HttpStatus.FORBIDDEN.getReasonPhrase())
				.status(HttpStatus.FORBIDDEN.value())
				.message(message)
				.build());
	}

}