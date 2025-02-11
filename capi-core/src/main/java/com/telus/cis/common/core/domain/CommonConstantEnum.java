/*
 *  Copyright (c) 2018 TELUS Communications Inc.,
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

public enum CommonConstantEnum {

	// header names and values
	ENV_STRING("env"),
	REFERENCE_HOST("x-forwarded-host"),
	REFERENCE_PORT("x-forwarded-port"),
	X_RUNTIME_ENV("X-Runtime-Env"),
	X_TOTAL_COUNT("X-Total-Count"),
	X_RESULT_COUNT("X-Result-Count"),
	CACHE_CONTROL("Cache-Control"),
	NO_CACHE("no-cache"),
	ORIGINATING_CLIENT("originator-client-id"),
	USER_ID_TOKEN_HEADER("x-id-token"),
	AUTHORIZATION_HEADER("Authorization"),

	// query parameters (mostly TMF 630)
    PARAM_DATE_GREATER_THAN("date.gt"),
    PARAM_DATE_GREATER_THAN_OR_EQUAL_TO("date.gte"),
    PARAM_DATE_LESS_THAN("date.lt"),
    PARAM_DATE_LESS_THAN_OR_EQUAL_TO("date.lte"),
    PARAM_DATE_EQUAL_TO("date.eq"),
    PARAM_OFFSET("offset"),
    PARAM_LIMIT("limit"),
    PARAM_FIELDS("fields"),
    PARAM_ASYNC("async"),

    // regular expressions
    MEMOID_PATTERN("\\d+-\\d+"),
    VARIABLE_PATTERN("^\\$\\{.*\\}$"),
    PRODUCTID_PATTERN("\\d+-[a-zA-Z]\\w\\d{9}"),
    PHONE_NUMBER_PATTERN_1("\\d{3}-\\d{3}-\\d{4}"),
    PHONE_NUMBER_PATTERN_2("\\(\\d{3}\\)\\s\\d{3}-\\d{4}"),
    PROVINCE_PATTERN("^(N[BFSTU]|[AM]B|BC|ON|P[EQ]|SK|YT)$"),
    ISO8601_PATTERN("2[0-9]{3}-[0-1][0-9]-[0-9]{2}T([0-9]{2}):([0-9]{2}).*"),    

    // date and time
    CANADA_PACIFIC_TIME_ZONE("Canada/Pacific"),
    CANADA_MOUNTAIN_TIME_ZONE("Canada/Mountain"),
    CANADA_SASKATCHEWAN_TIME_ZONE("Canada/Saskatchewan"),
    CANADA_CENTRAL_TIME_ZONE("Canada/Central"),
    CANADA_EASTERN_TIME_ZONE("Canada/Eastern"),
    CANADA_ATLANTIC_TIME_ZONE("Canada/Atlantic"),
    CANADA_NEWFOUNDLAND_TIME_ZONE("Canada/Newfoundland"),
    UTC("UTC"),

    // miscellaneous
    COMMA_DELIMITER(","),
    COMMA_DELIMITER_PLUS(", "),
    DASH_DELIMITER("-"),
	PIPE_DELIMITER("|"),
    SLASH_DELIMITER("/"),
    LEFT_BRACE("["),
    RIGHT_BRACE("]"),
    DOT_DELIMITER("."),
	SPACE_DELIMITER(" "),

	END("END");

	private String value;

	CommonConstantEnum(String value) {
		this.value = value;
	}

	public String value() {
		return StringUtils.isEmpty(this.value) ? name() : this.value;
	}

}