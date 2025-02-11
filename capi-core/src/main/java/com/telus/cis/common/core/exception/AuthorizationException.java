package com.telus.cis.common.core.exception;

import org.springframework.http.HttpStatus;

import com.telus.cis.common.core.domain.ServiceStatusCode;

public class AuthorizationException extends ApiException {

	private static final long serialVersionUID = 1L;

	public AuthorizationException(String message) {
		this(ServiceStatusCode.STATUS_UNAUTHORIZED.getCode(), message);
	}

	public AuthorizationException(String code, String message) {
        super(ApiError.builder()
                .code(code)
                .reason(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .build());
    }

}