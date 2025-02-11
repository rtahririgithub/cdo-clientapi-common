package com.telus.cis.common.kafka.domain;

public class KafkaDataEnum {
	public enum KafkaTopic {

		BILLING_ACCOUNT("customerBillingAccountWireless"),
		CUSTOMER_ORDER("partyEventCustomerOrderWireless");

		private String name;

		KafkaTopic(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum KafkaMetaData {
		EVENT_NAME("eventName"),
		ORIGINATING_APPLICATION_ID("originatingApplicationId"),
		ORIGINATOR_APPLICATION_ID("originatorApplicationId"), //wrong attribute used in early days.
		BRAND("brand"),
		ACCOUNT_TYPE("accountType"),
		ACCOUNT_SUB_TYPE("accountSubType"),
		COMBINED_ACCOUNT_TYPE_CD("combinedAccountTypeCd"),
		TRIGGERED_BY_KBID("eventTriggeredByKBID"),
		TRIGGERED_BY_KB_APP_ID("eventTriggeredByKBAppId"),
		NOTIFICATION_SUPPRESSION_IND("notificationSuppressionInd"),
		BAN("billingAccountNumber"),
		STACK_SOURCE("stackSource"), //to find out from which stack is this event published for troubleshooting purpose
		VERSION("version");

		private String name;

		KafkaMetaData(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum KafkaEvent {
		CREDIT_CHECK_CHANGE("CREDIT_CHECK_CHANGE"),
		SUBSCRIBER_CREATED ("SUBSCRIBER_CREATED"),
		ACCOUNT_STATUS_CHANGE("ACCOUNT_STATUS_CHANGE"),
		SUBSCRIBER_STATUS_CHANGE("SUBSCRIBER_STATUS_CHANGE");
		
		private String name;

		KafkaEvent(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}