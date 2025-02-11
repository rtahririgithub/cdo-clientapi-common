package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import com.telus.cis.common.srpds.domain.TransactionEnums.OfferingTransactionEnum;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FeatureInstanceChange implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	private String featureSpecId;
	private OfferingTransactionEnum transactionType;
	private String featureParam;
	private TimePeriod validity;
	
	public boolean isUpdate() {
		return transactionType.isUpdate();
	}
}
