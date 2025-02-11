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

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * Used when an API throws an error, typically with a HTTP error response-code (3xx, 4xx, 5xx).
 */
@Data
public class ApiError implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Application relevant detail, defined in the API or a common list.
	 **/
	@NotNull
	private String code;

	/**
	 * Explanation of the reason for the error which can be shown to a client user.
	 **/
	@NotNull
	private String reason;

	/**
	 * More details and corrective actions related to the error which can be shown to a client user.
	 **/
	@JsonInclude(Include.NON_NULL)
	private String message;

	/**
	 * HTTP Error code extension.
	 **/
	@JsonInclude(Include.NON_NULL)
	private Integer status;

	/**
	 * URI of documentation describing the error.
	 **/
	@JsonInclude(Include.NON_NULL)
	private String referenceError;

	@JsonProperty("@baseType")
	@JsonInclude(Include.NON_NULL)
	private String baseType;

	@JsonProperty("@schemaLocation")
	@JsonInclude(Include.NON_NULL)
	private String schemaLocation;

	@JsonProperty("@type")
	@JsonInclude(Include.NON_NULL)
	private String type;

	@JsonInclude(Include.NON_NULL)
	private String retry;

	@Builder
	private static ApiError of(String code, String reason, Integer status, String message) {

		ApiError error = new ApiError();
		error.setCode(code);
		error.setReason(reason);
		error.setStatus(status);
		error.setMessage(message);

		return error;
	}

}