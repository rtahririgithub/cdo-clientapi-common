package com.telus.cis.common.kafka.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.telus.cis.framework.kafka.subscriber_v2.ActivityDetail;
import com.telus.cis.framework.kafka.subscriber_v2.ConsumerName;
import com.telus.cis.framework.kafka.subscriber_v2.Equipment;
import com.telus.cis.framework.kafka.subscriber_v2.ManagerInfo;
import com.telus.cis.framework.kafka.subscriber_v2.Memo;
import com.telus.cis.framework.kafka.subscriber_v2.PortActivityDetail;
import com.telus.cis.framework.kafka.subscriber_v2.ServiceAgreement;
import com.telus.cis.framework.kafka.subscriber_v2.Subscriber;
import com.telus.cis.framework.kafka.subscriber_v2.SubscriberMoveDetail;
import com.telus.cis.framework.kafka.subscriber_v2.SubscriberStatusChange;

public abstract class SubscriberEventMixIn {
	
	@JsonInclude(Include.NON_NULL)
	public Subscriber subscriber;
	@JsonInclude(Include.NON_NULL)
	public ServiceAgreement serviceAgreement;
	@JsonInclude(Include.NON_NULL)
	public Equipment equipment;
	@JsonInclude(Include.NON_NULL)
	public Equipment associatedEquipment;
	@JsonInclude(Include.NON_NULL)
	public SubscriberMoveDetail subscriberMoveDetail;
	@JsonInclude(Include.NON_EMPTY)
	public SubscriberStatusChange subscriberStatusChangeList;
	@JsonInclude(Include.NON_NULL)
	public Memo memo;
	@JsonInclude(Include.NON_NULL)
	public ActivityDetail activityDetail;
	@JsonInclude(Include.NON_NULL)
	public ConsumerName oldName;
	@JsonInclude(Include.NON_NULL)
	public ManagerInfo managerInfo;
	@JsonInclude(Include.NON_NULL)
	public PortActivityDetail portActivityDetail;
}
