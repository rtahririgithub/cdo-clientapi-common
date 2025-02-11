package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuperBuilder
@Data
@NoArgsConstructor
public class ServiceRequestHeader implements Serializable {

	private static final long serialVersionUID = 1L;
	@Default
	private String languageCode = "EN";
	private int applicationId;
	private String referenceNumber;
	@Default
	private String applicationName = "N/A";
	private ServiceRequestParent parentRequest;
    private ServiceRequestNote note;
}
