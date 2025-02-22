package com.telus.cis.common.srpds.domain;

import lombok.Getter;

public enum ServiceRequestConstants {
	
	// Actor types
	ACTOR_TYPE_KB_OPERATOR_ID(4),
	ACTOR_TYPE_KB_SUBSCRIBER(8),
	ACTOR_TYPE_KB_ACCOUNT(9),
	ACTOR_TYPE_KB_SALES_REP_PIN(14),
	ACTOR_TYPE_KB_DEALER_CD(15),
	
	// Actor roles
	ACTOR_ROLE_PROCESSOR(2),
	ACTOR_ROLE_OF_RECORD(10),
	ACTOR_ROLE_TARGET(8),
	ACTOR_ROLE_NEW_TARGET(12),
	
	// Action type Id's
	ACTION_TYPE_CREATE(1),
	ACTION_TYPE_REMOVE(2),
	ACTION_TYPE_UPDATE(3),
	
	//Transaction specific sequence numbers
	PHONE_NUMBER_REMOVAL_RESOURCE_SEQ_NUMBER(1),
	PHONE_NUMBER_CREATE_RESOURCE_SEQ_NUMBER(2),
	SUBSCRIBER_STATUS_CHANGE_SERVICE_REQUEST_SEQ_NUMBER(1),
	EQUIPMENT_REMOVAL_SEQ_NUMBER(1),
	EQUIPMENT_CREATE_SEQ_NUMBER(2),
	
	// ServiceRequest Object Type Id's
	OBJECT_TYPE_SERVREQ_SERVICE_PACKAGE(5),
	OBJECT_TYPE_SERVREQ_SERVICE_FEATURE(6),
	OBJECT_TYPE_SERVREQ_RESOURCE(18),
	OBJECT_TYPE_SERVREQ_SUBSCRIPTION_STATUS(19),
	OBJECT_TYPE_SERVREQ_SUBSCRIBER_EQUIP(9),

	
	//other srpds constants 
	PRIORITY_MEDIUM(2),
	STATUS_COMPLETED(10),
	BUSINESS_AREA_TYPE_ACCOUNT(1),
	BUSINESS_AREA_TYPE_SUBSCRIBER(2),
	PROVISIONING_TYPE_PROVISIONABLE(1),
	PROVISIONING_TYPE_NON_PROVISIONABLE(2),
	OBJECT_VERSION_ID(1);
	


	@Getter
	private int value;

	ServiceRequestConstants(int value) {
		this.value = value;
	}

}
