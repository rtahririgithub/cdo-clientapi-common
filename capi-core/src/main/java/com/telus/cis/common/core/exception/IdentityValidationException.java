package com.telus.cis.common.core.exception;

import com.telus.cis.common.core.domain.ServiceStatusCode;

import org.springframework.http.HttpStatus;

public class IdentityValidationException extends ApiException {

	private static final long serialVersionUID = 1L;

	public IdentityValidationException(String message) {
		this(ServiceStatusCode.ERROR_IDENTITY.getCode(), message);
	}

	public IdentityValidationException(String code, String message) {
        super(ApiError.builder()
                .code(code)
                .reason(HttpStatus.FORBIDDEN.getReasonPhrase())
                .status(HttpStatus.FORBIDDEN.value())
                .message(message)
                .build());
    }

}