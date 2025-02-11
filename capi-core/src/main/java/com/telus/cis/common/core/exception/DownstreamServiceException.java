
package com.telus.cis.common.core.exception;

import static java.util.Objects.isNull;

import org.springframework.http.HttpStatus;

import com.telus.cis.common.core.domain.ServiceStatusCode;

public class DownstreamServiceException extends ApiException {
	
	private static final long serialVersionUID = 1L;

	public DownstreamServiceException(String message) {
		super(ApiError.builder()
				.code(ServiceStatusCode.ERROR_DOWNSTREAM.getCode())
				.reason(HttpStatus.FAILED_DEPENDENCY.getReasonPhrase())
				.status(HttpStatus.FAILED_DEPENDENCY.value())
				.message(message)
				.build());
	}

	public DownstreamServiceException(String description, String parameter, String errorMessage) {
		super(ApiError.builder()
				.code(ServiceStatusCode.ERROR_DOWNSTREAM.getCode())
				.reason(HttpStatus.FAILED_DEPENDENCY.getReasonPhrase())
				.status(HttpStatus.FAILED_DEPENDENCY.value())
				.message(isNull(parameter) ? String.format("Call for %s failed : [%s].", description, errorMessage)
						: String.format("Call for %s failed for parameter(s) : [%s] : error message : [%s].", description, parameter, errorMessage))
				.build());
	}

}