package com.telus.cis.common.srpds.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.telus.cis.common.srpds.domain.TransactionEnums.EventDetailEnum;
import com.telus.cis.common.srpds.domain.TransactionEnums.StatusChangeEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@ToString(callSuper=true)
public class SubscriberStatusChangeActivity extends BaseActivity implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ban;
	private String phoneNumber;	
	private String subscriberId;
	private String reason;
	private OffsetDateTime activationDate;
	private OffsetDateTime deactivationDate;
	private StatusChangeEnum statusChangeEnum;
	
	@Override
	public EventDetailEnum getEventDetail() {
		return statusChangeEnum.getEvent();
	}
	
}
