package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@NoArgsConstructor
public class EquipmentDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	private String serialNumber;	
	private String productTechnologyType;
	private String productCode;	
	private String productStatusCode;	
	private String productClassCode;	
	private String productGroupTypeCode;	
}