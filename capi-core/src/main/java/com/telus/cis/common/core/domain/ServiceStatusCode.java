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

package com.telus.cis.common.core.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ServiceStatusCode {
	
	STATUS_SUCCESS(HttpStatus.OK, null, "status.success"), 
	STATUS_CREATED(HttpStatus.CREATED, "ERR_CREATE_001", "status.created"),
	STATUS_BAD_REQUEST(HttpStatus.BAD_REQUEST, "ERR_REQUEST_001", "status.bad_request"), 
	STATUS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ERR_UNAUTHORIZED_001", "status.unauthorized"),
	STATUS_FORBIDDEN(HttpStatus.FORBIDDEN, "ERR_FORBIDDEN_001", "status.forbidden"), 
	STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_NOT_FOUND_001", "status.not_found"),
	STATUS_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "ERR_METHOD_001", "status.method_not_found"), 
	STATUS_CONFLICT(HttpStatus.CONFLICT, "ERR_CONFLICT_001", "status.conflict"),
	ERROR_SYSTEM(HttpStatus.INTERNAL_SERVER_ERROR, null, "error.system"), 
	ERROR_NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "ERR_NOT_IMPLEMENTED_001", "error.not_implemented"),
	ERROR_SQL(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_SQL_001", "error.sql_exception"), 
	ERROR_DOWNSTREAM(HttpStatus.FAILED_DEPENDENCY, "ERR_DOWNSTREAM_001", "error.failed_dependency"),
	ERROR_VALIDATE(HttpStatus.BAD_REQUEST, "ERR_VALIDATE_001", "error.validate"), 
	ERROR_NOT_FOUND(HttpStatus.NOT_FOUND, "ERR_NOT_FOUND_001", "error.empty_response"),
	ERROR_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_RESPONSE_001", "error.response"), 
	ERROR_IDENTITY(HttpStatus.FORBIDDEN, "ERR_IDENTITY_001", "error.invalid_identity"),
	UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", "error.unknown");

	private String code;
	private String message;
	private HttpStatus status;

	private ServiceStatusCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.message = message;
		this.code = (StringUtils.isBlank(code)) ? status.toString() : code;
	}

}