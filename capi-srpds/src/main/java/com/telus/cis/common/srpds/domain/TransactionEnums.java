package com.telus.cis.common.srpds.domain;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public class TransactionEnums {

	public enum OfferingTransactionEnum {

		ADD("ADD"), MODIFY("UPDATE"), REMOVE("REMOVE"), NO_CHANGE("NO_CHANGE");

		@Getter
		private String value;

		OfferingTransactionEnum(String value) {
			this.value = value;
		}

		public static OfferingTransactionEnum getTransactionEnumByName(String name) {
			return Arrays.stream(OfferingTransactionEnum.values()).filter(v -> v.name().equalsIgnoreCase(name))
					.findFirst().orElse(OfferingTransactionEnum.NO_CHANGE);
		}

		public boolean isUpdate() {
			return StringUtils.equals(MODIFY.getValue(), getValue());
		}
	}
	
	public enum SubscriberStatusEnum {
		
		RESERVE("R"), ACTIVE("A"), SUSPEND("S"), CANCEL("C");
		
		@Getter
		private String value;

		SubscriberStatusEnum(String value) {
			this.value = value;
		}

	}
	public enum EventDetailEnum {

		CANCEL(18, "CANCEL", "Telus API - Cancel"), SUSPEND(34, "SUSPEND", "Telus API - Suspend"),
		ACTIVATE_FROM_RESERVE(15, "ACTIVATE FROM RESERVE","Telus API - Activate From Reserve"),
		RESTORE_FROM_CANCEL(19, "RESTORE FROM CANCEL","Telus API - Restore From Cancel"),
		RESTORE_FROM_SUSPENDED(35, "RESTORE FROM SUSPEND", "Telus API - Restore From Suspend"),
		PRICEPLAN_CHANGE(3, "PRICE PLAN CHANGE", "Telus API - Price Plan Change"),
		ADD_ON_SERVICE_CHANGE(3, "SERVICE CHANGE", "Telus API - Service Change"),
		SERVICE_FEATURE_CHANGE(22, "FEATURE CHANGE ", "Telus API - Feature Change "),
		EQUIPMENT_CHANGE(3, "EQUIPMENT CHANGE", "Telus API - Equipment Change");


		@Getter
		private int eventId;
		@Getter
		private String eventName;
		@Getter
		private String eventDesc;
		
		EventDetailEnum(int eventId, String eventName, String eventDesc) {
			this.eventId = eventId;
			this.eventName = eventName;
			this.eventDesc = eventDesc;

		}
	}
	
	public enum StatusChangeEnum {

		CANCEL(EventDetailEnum.CANCEL, ServiceRequestConstants.ACTION_TYPE_REMOVE.getValue(),
				SubscriberStatusEnum.CANCEL),
		SUSPEND(EventDetailEnum.SUSPEND, ServiceRequestConstants.ACTION_TYPE_REMOVE.getValue(),
				SubscriberStatusEnum.SUSPEND),
		ACTIVATE_FROM_RESERVE(EventDetailEnum.ACTIVATE_FROM_RESERVE,
				ServiceRequestConstants.ACTION_TYPE_CREATE.getValue(), SubscriberStatusEnum.ACTIVE),
		RESTORE_FROM_CANCEL(EventDetailEnum.RESTORE_FROM_CANCEL, ServiceRequestConstants.ACTION_TYPE_CREATE.getValue(),
				SubscriberStatusEnum.ACTIVE),
		RESTORE_FROM_SUSPENDED(EventDetailEnum.RESTORE_FROM_SUSPENDED,
				ServiceRequestConstants.ACTION_TYPE_CREATE.getValue(), SubscriberStatusEnum.ACTIVE);

		@Getter
		private EventDetailEnum event;
		
		@Getter
		private int actionTypeId;
		
		@Getter
		private SubscriberStatusEnum newStatus;
		

		StatusChangeEnum(EventDetailEnum event,int actionTypeId,SubscriberStatusEnum newStatus) {
			this.event = event;
			this.actionTypeId = actionTypeId;
			this.newStatus = newStatus;
		}
	}
	
}
