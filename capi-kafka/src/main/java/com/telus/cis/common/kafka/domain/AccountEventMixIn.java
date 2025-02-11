package com.telus.cis.common.kafka.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.telus.cis.framework.kafka.account_v1.ActivityDetail;
import com.telus.cis.framework.kafka.account_v1.ChargeDetail;
import com.telus.cis.framework.kafka.account_v1.CreditCheckResult;
import com.telus.cis.framework.kafka.account_v1.CreditDetail;
import com.telus.cis.framework.kafka.account_v1.DebtSummary;
import com.telus.cis.framework.kafka.account_v1.Memo;
import com.telus.cis.framework.kafka.account_v1.PaymentArrangementInfo;
import com.telus.cis.framework.kafka.account_v1.PaymentDetail;
import com.telus.cis.framework.kafka.account_v1.PaymentMethod;
import com.telus.cis.framework.kafka.account_v1.PaymentNotificationInfo;
import com.telus.cis.framework.kafka.account_v1.Subscriber;
import com.telus.cis.framework.kafka.account_v1.SubscriberStatusChange;

public abstract class AccountEventMixIn {
	
	@JsonInclude(Include.NON_NULL)
	public PaymentMethod paymentMethod;
	@JsonInclude(Include.NON_NULL)
	public PaymentDetail paymentDetail;
	@JsonInclude(Include.NON_NULL)
	public DebtSummary debtSummary;	
	@JsonInclude(Include.NON_NULL)
	public CreditDetail creditDetail;
	@JsonInclude(Include.NON_NULL)
	public ChargeDetail originalChargeDetail;
	@JsonInclude(Include.NON_NULL)
	public Integer followUpId;
	@JsonInclude(Include.NON_NULL)
	public String followUpType;
	@JsonInclude(Include.NON_NULL)
	public String followUpReason;
	@JsonInclude(Include.NON_NULL)
	public Subscriber subscriber;
	@JsonInclude(Include.NON_NULL)
	public ActivityDetail activityDetail;
	@JsonInclude(Include.NON_EMPTY)
	public List<SubscriberStatusChange> subscriberStatusChangeList;
	@JsonInclude(Include.NON_NULL)
	public Memo memo;
	@JsonInclude(Include.NON_NULL)
	public PaymentArrangementInfo paymentArrangementInfo;
	@JsonInclude(Include.NON_NULL)
	public PaymentNotificationInfo paymentNotificationInfo;
	@JsonInclude(Include.NON_NULL)
	public CreditCheckResult creditCheckResult;
}
