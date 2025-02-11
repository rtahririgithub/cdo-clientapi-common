package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.telus.cis.common.srpds.domain.TransactionEnums.OfferingTransactionEnum;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OfferingInstanceChange implements Serializable {

	private static final long serialVersionUID = 1L;

	private String offeringRefId;
	private OfferingTransactionEnum transactionType;
	private String offeringType;
	private TimePeriod validity;
	private List<FeatureInstanceChange> featureInstance = new ArrayList<>();
	
	public boolean isUpdate() {
		return transactionType.isUpdate();
	}
}
