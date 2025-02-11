package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class ServiceRequestParent implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private long parentId;
	private Timestamp timestamp;
	private int relationshipTypeId;
}
