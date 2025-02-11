package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class ServiceRequestNote implements Serializable {

	private static final long serialVersionUID = 1L;
	private int noteTypeId;
	private String noteText;
}
