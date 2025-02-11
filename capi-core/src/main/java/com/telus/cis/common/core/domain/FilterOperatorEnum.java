package com.telus.cis.common.core.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * This is the list of TMF630 supported operators and their query parameter suffix values.
 * @author x149682
 *
 */
public enum FilterOperatorEnum {

	EQ(".eq"),
    GT(".gt"),
    GE(".ge"),
    LT(".lt"),
    LE(".le"),
    NE(".ne"),
    REGEX(".regex"),
    NF("not found");

	private String value;

	FilterOperatorEnum(String value) {
		this.value = value;
	}

	public String value() {
		return StringUtils.isBlank(this.value) ? name() : this.value;
	}

}
