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

package com.telus.cis.common.jaxws;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.telus.cis.common.core.exception.DownstreamServiceException;

import lombok.extern.slf4j.Slf4j;

public interface ResponseExceptionHandler {
	
	@Slf4j
	final class LogHolder { }

	/**
	 * Validate response message error messages.
	 * 
	 * Required: 
	 * 	- Supplier<String> for error code.
	 * 	- Supplier<String> for context data.
	 * 	- Supplier<List<Message>> for response message list - a Function<Message, String> for SOAP Message to convert message to String.
	 */
	public default <M> void validateResponseValue(String description, String param, Supplier<String> errorCodeSupplier, Supplier<String> contextDataSupplier,
			Supplier<List<M>> errorMessageListSupplier, Function<M, String> typeConvertor) {
		
		String extraErrorString = null;
		if (Objects.nonNull(errorMessageListSupplier)) {
			extraErrorString = errorMessageListSupplier.get().stream().map(m -> Objects.nonNull(typeConvertor) ? typeConvertor.apply(m) : ((Object) m).toString())
					.collect(Collectors.joining(" "));
		}
		validateResponseValue(description, param, nonNull(errorCodeSupplier) ? errorCodeSupplier.get() : null, nonNull(contextDataSupplier) ? contextDataSupplier.get() : null,
				extraErrorString);
	}

	public default void validateResponseValue(String description, String param, String errorCode, String contextData, String extraErrorMessage) {
		
		if (isNotBlank(errorCode)) {
			String errorMessage = StringUtils.isEmpty(contextData) ? extraErrorMessage : contextData;
			LogHolder.log.warn("{} error code ;[{}] error message : [{}]", description, errorCode, errorMessage);
			String errorResponseMessage = formatMessage(description, param, errorCode, errorMessage);
			throw new DownstreamServiceException(errorResponseMessage);
		}
		if (isNotBlank(contextData)) {
			LogHolder.log.warn("{} error in context data : [{}]", description, contextData);
			throw new DownstreamServiceException(description, param, contextData);
		}
	}

	public default <T, R> R getResponseValue(String description, String param, T response, Function<T, R> operation) {
		
		if (isNull(response)) {
			throw new DownstreamServiceException(description, null, "Empty response");
		}
		return operation.apply(response);
	}

	public default <T> T getResponseValue(String description, String param, T response, Supplier<T> operation) {
		
		if (isNull(response)) {
			throw new DownstreamServiceException(description, null, "Empty response");
		}
		return operation.get();
	}

	public default <T, M> T getResponseValue(String description, String param, Supplier<String> errorCodeSupplier, Supplier<String> contextDataSupplier,
			Supplier<List<M>> errorMessageListSupplier, Function<M, String> typeConvertor, Supplier<T> operation) {
		validateResponseValue(description, param, errorCodeSupplier, contextDataSupplier, errorMessageListSupplier, typeConvertor);
		return nonNull(operation) ? operation.get() : null;
	}

	public default String formatMessage(String description, String parameter, String errorCode, String errorMessage) {
		return isNull(parameter) ? String.format("Call for %s failed error code [%s] : [%s].", description, errorCode, errorMessage)
				: String.format("Call for %s failed for parameter(s) : [%s] : errorCode : [%s] error message : [%s].", description, parameter, errorCode, errorMessage);
	}

	public default void throwDownStreamException(String description, String parameter, String exceptionMessage) {
		throw new DownstreamServiceException(description, parameter, exceptionMessage);
	}

	public default <T extends Exception> void throwDownStreamException(String description, String parameter, T exception) {
		throw new DownstreamServiceException(description, parameter, exception.getMessage());
	}

}