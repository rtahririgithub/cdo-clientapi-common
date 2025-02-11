/*
 *  Copyright (c) 2022 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.exception;

import static com.telus.cis.common.core.exception.ApiErrorAttributes.ExceptionHttpStatusEnum.determineHttpStatus;
import static java.util.Objects.nonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.UnknownHttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * Component to customize WebFlux error handling
 *
 * @author T896143
 */
@Component
public class ApiErrorAttributes extends DefaultErrorAttributes {
	
	private static final String UNKNOWN_HTTP_STATUS = "UNKNOWN_HTTP_STATUS";

	public enum ExceptionHttpStatusEnum {
		
		UNSUPPORTED(UnsupportedOperationException.class, ex -> HttpStatus.NOT_IMPLEMENTED), 
		BAD_REQUEST(HttpMessageNotReadableException.class, ex -> HttpStatus.BAD_REQUEST),
		AUTHORIZATION(AccessDeniedException.class, ex -> HttpStatus.UNAUTHORIZED), 
		AUTHENTICATION(AuthenticationException.class, ex -> HttpStatus.UNAUTHORIZED),
		RESPONSE_STATUS(ResponseStatusException.class, ex -> ResponseStatusException.class.cast(ex).getStatus()),
		WEB_CLIENT(WebClientResponseException.class, ex -> WebClientResponseException.class.cast(ex).getStatusCode());

		private Class<? extends Throwable> clazz;
		private Function<Throwable, HttpStatus> statusFunction;

		ExceptionHttpStatusEnum(Class<? extends Throwable> clazz, Function<Throwable, HttpStatus> function) {
			this.clazz = clazz;
			this.statusFunction = function;
		}

		public static HttpStatus determineHttpStatus(Throwable error) {
			return Stream.of(ExceptionHttpStatusEnum.values()).filter(e -> e.clazz.isInstance(error)).map(e -> e.statusFunction.apply(error)).findFirst()
					.orElse(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		return getErrorAttributes(getError(request));
	}

	private Map<String, Object> getErrorAttributes(Throwable error) {

		// check the underlying cause to see if it's one of the following exception types and handle appropriately
		if (error instanceof CompletionException) {
			// as we're using Spring Reactor Core libraries, the true exception may be wrapped by a CompletionException
			return Optional.ofNullable(error.getCause()).map(this::getErrorAttributes).orElseGet(() -> getDefaultErrorAttributes(error));
		}
		if (error instanceof UnknownHttpStatusCodeException) {
			// handle any unchecked custom HTTP codes being returned by downstream services
			return getUnknownHttpErrorAttributes(UnknownHttpStatusCodeException.class.cast(error));
		}
		if (error instanceof ApiException) {
			// map errors from our internal ApiException types
			return getApiErrorAttributes(ApiException.class.cast(error));
		}

		return getDefaultErrorAttributes(error);
	}

	private Map<String, Object> getDefaultErrorAttributes(Throwable error) {

		var status = determineHttpStatus(error);

		var errorAttributes = new LinkedHashMap<String, Object>();
		errorAttributes.put("code", status.name());
		errorAttributes.put("reason", status.getReasonPhrase());
		errorAttributes.put("message", error.getMessage());
		errorAttributes.put("status", status.value());

		return errorAttributes;
	}

	private Map<String, Object> getApiErrorAttributes(ApiException error) {

		var apiError = error.getApiError();
		var errorMessage = nonNull(error.getCause()) ? StringUtils.defaultIfBlank(error.getCause().getMessage(), error.getMessage()) : error.getMessage();

		var errorAttributes = new LinkedHashMap<String, Object>();
		errorAttributes.put("code", apiError.getCode());
		errorAttributes.put("reason", apiError.getReason());
		errorAttributes.put("message", errorMessage);
		errorAttributes.put("status", apiError.getStatus());

		return errorAttributes;
	}
	
	private Map<String, Object> getUnknownHttpErrorAttributes(UnknownHttpStatusCodeException error) {
		
		var reason = StringUtils.defaultIfBlank(error.getStatusText(), error.getResponseBodyAsString());
		var errorMessage = StringUtils.defaultIfBlank(error.getMessage(), error.getResponseBodyAsString());

		var errorAttributes = new LinkedHashMap<String, Object>();
		errorAttributes.put("code", UNKNOWN_HTTP_STATUS);
		errorAttributes.put("reason", reason);
		errorAttributes.put("message", errorMessage);
		errorAttributes.put("status", error.getRawStatusCode());

		return errorAttributes;
	}

}