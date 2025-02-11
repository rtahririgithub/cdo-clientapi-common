package com.telus.cis.common.core.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class is used to encapsulate the information used to generically apply attribute filters when finding TMF resources.
 * @author x149682
 */
@Getter
@Setter
@ToString(includeFieldNames = true)
public class AttributeFilter {

	// The name of the attribute without the operator (e.g. ?relatedParty.fullName=John -> attributeName is relatedParty.fullName)
	private String attributeName;

	// The value of the filter to be applied (e.g. ?relatedParty.fullName=John -> filterValue is John)
	private String filterValue;

	// The operator that's extracted from the query parameter (e.g. ?relatedParty.fullName.regex=.*John.* -> operator is .regex)
	private FilterOperatorEnum operator;

}
