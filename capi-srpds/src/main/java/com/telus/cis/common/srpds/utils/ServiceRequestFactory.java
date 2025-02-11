package com.telus.cis.common.srpds.utils;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.telus.cis.common.core.domain.CommonConstantEnum;
import com.telus.cis.common.srpds.domain.BaseActivity;
import com.telus.cis.common.srpds.domain.EquipmentChangeActivity;
import com.telus.cis.common.srpds.domain.EquipmentDetail;
import com.telus.cis.common.srpds.domain.FeatureInstanceChange;
import com.telus.cis.common.srpds.domain.OfferingInstanceChange;
import com.telus.cis.common.srpds.domain.OfferingInstanceChangeActivity;
import com.telus.cis.common.srpds.domain.ServiceRequestConstants;
import com.telus.cis.common.srpds.domain.ServiceRequestNote;
import com.telus.cis.common.srpds.domain.SubscriberStatusChangeActivity;
import com.telus.cis.common.srpds.domain.TimePeriod;
import com.telus.cis.common.srpds.domain.TransactionEnums.EventDetailEnum;
import com.telus.cis.common.srpds.domain.TransactionEnums.OfferingTransactionEnum;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.Actor;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.Note;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ParentServiceRequestPk;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServiceFeatureItem;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServicePackageItem;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServiceRequest;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServiceRequestHeader;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServiceRequestItem;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServiceRequestItemCollection;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.Status;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.SubscriberEquipmentItem;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.SubscriptionStatusItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceRequestFactory {

	private ServiceRequestFactory() {
		throw new IllegalStateException("factory classes should not have constructors.");
	}

	
	public static ServiceRequest createEquipmentChangeServiceRequest(EquipmentChangeActivity equipChangeActivity) {
		//Create baseServiceRequest
		ServiceRequest target =  createBaseServiceRequest(equipChangeActivity.getBan(),equipChangeActivity.getPhoneNumber(),equipChangeActivity);

		// Create the subscriberEquipmentItem for removal
		SubscriberEquipmentItem subscriberEquipmentItemRemoval = prepareEquipmentChangeItem(equipChangeActivity,equipChangeActivity.getOldEquipmentDetail(),
				ServiceRequestConstants.ACTION_TYPE_REMOVE.getValue(), ServiceRequestConstants.EQUIPMENT_REMOVAL_SEQ_NUMBER.getValue(), target );

		//Create the subscriberEquipmentItem for create
		SubscriberEquipmentItem subscriberEquipmentItemCreate = prepareEquipmentChangeItem(equipChangeActivity,equipChangeActivity.getNewEquipmentDetail(),
				ServiceRequestConstants.ACTION_TYPE_CREATE.getValue(), ServiceRequestConstants.EQUIPMENT_CREATE_SEQ_NUMBER.getValue(), target);

		// Prepare items list
		ServiceRequestItemCollection itemCollection = new ServiceRequestItemCollection();
		itemCollection.getSubscriberEquipmentItemList().add(subscriberEquipmentItemRemoval);
		itemCollection.getSubscriberEquipmentItemList().add(subscriberEquipmentItemCreate);
		target.setServiceRequestItemCollection(itemCollection);

		return target;
	}
	
	
	public static ServiceRequest createChangeSubscriberStatusServiceRequest(SubscriberStatusChangeActivity statusActivity) {
		//Create baseServiceRequest
		ServiceRequest target =  createBaseServiceRequest(statusActivity.getBan(),statusActivity.getPhoneNumber(),statusActivity);

		// Create the subscriptionStatusItem
		SubscriptionStatusItem subscriptionStatusItem = prepareSubscriptionStatusItem(statusActivity, target.getServiceRequestHeader());

		// Prepare items list
		ServiceRequestItemCollection itemCollection = new ServiceRequestItemCollection();
		itemCollection.getSubscriptionStatusItemList().add(subscriptionStatusItem);
		target.setServiceRequestItemCollection(itemCollection);

		return target;
	}
	
	public static ServiceRequest createPriceplanfferingChangeServiceRequest(OfferingInstanceChangeActivity instanceChangeActivity) {

		//Create baseServiceRequest
		ServiceRequest target =  createBaseServiceRequest(instanceChangeActivity.getBan(),instanceChangeActivity.getPhoneNumber(),instanceChangeActivity);
		

		OfferingInstanceChange oldPriceplan = instanceChangeActivity.getOldPriceplan();
		OfferingInstanceChange newPriceplan = instanceChangeActivity.getNewPriceplan();

		
		if(Objects.isNull(oldPriceplan) || Objects.isNull(newPriceplan) || StringUtils.equals(oldPriceplan.getOfferingRefId(),newPriceplan.getOfferingRefId())) {
			log.info("ServiceRequestFactory:: createPriceplanfferingChangeServiceRequest , priceplan change is not valid to report srpds , ban : [{}] ,subscriber: [{}]", instanceChangeActivity.getBan(),instanceChangeActivity.getPhoneNumber());
			return null;
		}
		
		ServiceRequestItemCollection itemCollection = new ServiceRequestItemCollection();
		
		//Prepare oldPriceplan servicePackage item
		ServicePackageItem oldPriceplanItem = new ServicePackageItem();
		setBaseItemRequestData(oldPriceplanItem, ServiceRequestConstants.ACTION_TYPE_CREATE.getValue(),
				instanceChangeActivity.getEventDetail().getEventName() + " "+ CommonConstantEnum.DASH_DELIMITER.value() + " NEW",
				instanceChangeActivity.getEventDetail().getEventDesc() + " "+ CommonConstantEnum.DASH_DELIMITER.value() + " NEW",
				ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), 1,
				target.getServiceRequestHeader());
		setOfferingInstanceChangeData(oldPriceplanItem, oldPriceplan);

		//Prepare newPriceplan servicePackage item
		ServicePackageItem newPriceplanItem = new ServicePackageItem();
		setBaseItemRequestData(newPriceplanItem, ServiceRequestConstants.ACTION_TYPE_REMOVE.getValue(),
				instanceChangeActivity.getEventDetail().getEventName() + " "+ CommonConstantEnum.DASH_DELIMITER.value() + " OLD",
				instanceChangeActivity.getEventDetail().getEventDesc() + " "+ CommonConstantEnum.DASH_DELIMITER.value() + " OLD",
				ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), 1,
				target.getServiceRequestHeader());
		setOfferingInstanceChangeData(newPriceplanItem, oldPriceplan);
		
		
		//Add to the item collection list
		itemCollection.getServicePackageItemList().add(oldPriceplanItem);
		itemCollection.getServicePackageItemList().add(newPriceplanItem);
		target.setServiceRequestItemCollection(itemCollection);
		
		return target;
	}
	
	public static ServiceRequest createAddOnOfferingChangeServiceRequest(OfferingInstanceChangeActivity instanceChangeActivity) {

		//Create baseServiceRequest
		ServiceRequest target =  createBaseServiceRequest(instanceChangeActivity.getBan(),instanceChangeActivity.getPhoneNumber(),instanceChangeActivity);
		

		List<OfferingInstanceChange> addOnChanges = instanceChangeActivity.getAddOnChanges();
		
		if(CollectionUtils.isEmpty(addOnChanges)) {
			log.info("ServiceRequestFactory :: createAddOnOfferingChangeServiceRequest , addOnChanges are empty/null , ban : [{}] ,subscriber: [{}]", instanceChangeActivity.getBan(),instanceChangeActivity.getPhoneNumber());
			return null;
		}
		
		ServiceRequestItemCollection itemCollection = new ServiceRequestItemCollection();
		int sequenceNumber = 0;

		//Create addOn offeringInstance change "servicePackage" item
		for (OfferingInstanceChange addOn : addOnChanges) {

			ServicePackageItem item = new ServicePackageItem();
			OfferingTransactionEnum offeringTransactionEnum = addOn.getTransactionType();
			switch (offeringTransactionEnum) {

			case ADD:
				setBaseItemRequestData(item, ServiceRequestConstants.ACTION_TYPE_CREATE.getValue(),
						instanceChangeActivity.getEventDetail().getEventName() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " " + offeringTransactionEnum.getValue(),
						instanceChangeActivity.getEventDetail().getEventDesc() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " " + offeringTransactionEnum.getValue(),
						ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), ++sequenceNumber,
						target.getServiceRequestHeader());
				setOfferingInstanceChangeData(item, addOn);
				itemCollection.getServicePackageItemList().add(item);
				break;

			case MODIFY:
				setBaseItemRequestData(item, ServiceRequestConstants.ACTION_TYPE_UPDATE.getValue(),
						instanceChangeActivity.getEventDetail().getEventName() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " " + offeringTransactionEnum.getValue(),
						instanceChangeActivity.getEventDetail().getEventDesc() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " " + offeringTransactionEnum.getValue(),
						ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), ++sequenceNumber,
						target.getServiceRequestHeader());
				setOfferingInstanceChangeData(item, addOn);
				itemCollection.getServicePackageItemList().add(item);
				break;

			case REMOVE:
				setBaseItemRequestData(item, ServiceRequestConstants.ACTION_TYPE_REMOVE.getValue(),
						instanceChangeActivity.getEventDetail().getEventName() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " " + offeringTransactionEnum.getValue(),
						instanceChangeActivity.getEventDetail().getEventDesc() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " " + offeringTransactionEnum.getValue(),
						ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), ++sequenceNumber,
						target.getServiceRequestHeader());
				setOfferingInstanceChangeData(item, addOn);
				itemCollection.getServicePackageItemList().add(item);
				break;

			default:
				log.info("Invalid transaction type [{}] , ban : [{}] ,subscriber: [{}]", addOn.getTransactionType(),
						instanceChangeActivity.getBan(), instanceChangeActivity.getPhoneNumber());
				break;
			}
		}
		target.setServiceRequestItemCollection(itemCollection);
		return target;
	}
	
	
	public static ServiceRequest createFeatureOfferingChangeServiceRequest(OfferingInstanceChangeActivity instanceChangeActivity) {

		//Create baseServiceRequest
		ServiceRequest target =  createBaseServiceRequest(instanceChangeActivity.getBan(),instanceChangeActivity.getPhoneNumber(),instanceChangeActivity);
		
		//Populate addOn feature changes ( Although we can modify end date and dont see that mapping for srpds event call ,so ignored dates update from mapping )
		List<FeatureInstanceChange> featureInstanceChanges = Optional.ofNullable(instanceChangeActivity.getAddOnChanges())
				.orElseGet(Collections::emptyList)
				.stream()
				.filter(Objects::nonNull)
				.filter(OfferingInstanceChange::isUpdate)
				.map(OfferingInstanceChange::getFeatureInstance)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.filter(FeatureInstanceChange::isUpdate)
			//	.filter(featureChange -> StringUtils.isNotBlank(featureChange.getFeatureParam()))
				.collect(Collectors.toList());
		
		//add the feature changes passed from request activity
		featureInstanceChanges.addAll(instanceChangeActivity.getFeatureUpdates());
	
		//will skip the srpds flow if featureInstanceChanges are empty
		if(CollectionUtils.isEmpty(featureInstanceChanges)) {
			log.info("ServiceRequestFactory :: createFeatureOfferingChangeServiceRequest , featureInstanceChanges are empty/null , ban : [{}] ,subscriber: [{}]", instanceChangeActivity.getBan(),instanceChangeActivity.getPhoneNumber());
			return null;
		}

		ServiceRequestItemCollection itemCollection = new ServiceRequestItemCollection();
		
		int sequenceNumber = 0;
		// Create addOn offeringInstance change "servicePackage" item
		for (FeatureInstanceChange featureInstanceChange : featureInstanceChanges) {
			ServiceFeatureItem featureItem = new ServiceFeatureItem();
			setBaseItemRequestData(featureItem, ServiceRequestConstants.ACTION_TYPE_UPDATE.getValue(),
					instanceChangeActivity.getEventDetail().getEventName() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " UPDATE",
					instanceChangeActivity.getEventDetail().getEventDesc() + " " + CommonConstantEnum.DASH_DELIMITER.value() + " UPDATE",
					ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), ++sequenceNumber,
					target.getServiceRequestHeader());
			setFeatureInstanceData(featureItem, featureInstanceChange);
			itemCollection.getServiceFeatureItemList().add(featureItem);
		}
		target.setServiceRequestItemCollection(itemCollection);
		return target;
	}
	
	
	private static ServiceRequest createBaseServiceRequest(int ban ,String subscriberId,BaseActivity baseActivity) {

		ServiceRequest target = new ServiceRequest();

		// Construct actors for subscriber status change
		List<Actor> actors = constructBaseActorListForActivity(ban, subscriberId, baseActivity.getDealerCode(),baseActivity.getSalesRepCode(), baseActivity.getUserId());
		
		// Create service request header for subscriber status change
		ServiceRequestHeader requestHeader = createServiceRequestHeader(baseActivity, actors, baseActivity.getEventDetail());
		target.setServiceRequestHeader(requestHeader);

		return target;
	}
	

	private static void setOfferingInstanceChangeData(ServicePackageItem item, OfferingInstanceChange offering) {
		item.setItemEffectiveDate(getStartDate(offering.getValidity()));
		item.setExpiryDate(getExpiryDate(offering.getValidity()));
		item.setSupportedInd(true);
		item.setKbSocTypeCode(offering.getOfferingType());
		item.setKbSoc(offering.getOfferingRefId());
		item.setObjectTypeId(ServiceRequestConstants.OBJECT_TYPE_SERVREQ_SERVICE_PACKAGE.getValue());
		item.setObjectVersionId(ServiceRequestConstants.OBJECT_VERSION_ID.getValue());
	}
	
	private static void setFeatureInstanceData(ServiceFeatureItem featureItem, FeatureInstanceChange featureInstanceChange) {
		featureItem.setObjectVersionId(ServiceRequestConstants.OBJECT_VERSION_ID.getValue());
		featureItem.setObjectTypeId(ServiceRequestConstants.OBJECT_TYPE_SERVREQ_SERVICE_FEATURE.getValue());
		featureItem.setKbFeatureCode(featureInstanceChange.getFeatureSpecId());
		String parameter = featureInstanceChange.getFeatureParam();
		final int parameterMaxSize = 40;
		Optional.ofNullable(parameter).ifPresent(param -> featureItem.setParameterValue(param.length() <= parameterMaxSize ? param : param.substring(0, parameterMaxSize)));
	}
	
	private static Timestamp getStartDate(TimePeriod validity) {
		if(Objects.nonNull(validity) && Objects.nonNull(validity.getStartDateTime())) {
			return getSqlTimeStamp(validity.getStartDateTime());
		}
		return null;
	}
	
	private static Timestamp getExpiryDate(TimePeriod validity) {
		if(Objects.nonNull(validity) && Objects.nonNull(validity.getEndDateTime())) {
			return getSqlTimeStamp(validity.getEndDateTime());
		}
		return null;
	}
	
	
	private static SubscriberEquipmentItem prepareEquipmentChangeItem(EquipmentChangeActivity equipChangeActivity,EquipmentDetail equipment, int actionTypeId, int sequenceNum, ServiceRequest target) {

		SubscriberEquipmentItem subEquipmentItem = new SubscriberEquipmentItem();
		
		boolean isCreate = ServiceRequestConstants.ACTION_TYPE_CREATE.getValue() == actionTypeId ; 
		String eventNameSuffix = isCreate ? " - NEW - PRIMARY" : "- OLD - PRIMARY";
		String eventDescSuffix = isCreate ? " - New - Primary" : "- Old - Primary";

		// Set header and other event detail
		setBaseItemRequestData(subEquipmentItem, actionTypeId, equipChangeActivity.getEventDetail().getEventName() + eventNameSuffix,equipChangeActivity.getEventDetail().getEventDesc() +eventDescSuffix,
				ServiceRequestConstants.PROVISIONING_TYPE_PROVISIONABLE.getValue(), sequenceNum, target.getServiceRequestHeader());


		// Set old subscriber equipment change event details
		subEquipmentItem.setObjectTypeId(ServiceRequestConstants.OBJECT_TYPE_SERVREQ_SUBSCRIBER_EQUIP.getValue());
		subEquipmentItem.setSwapType(equipChangeActivity.getSwapType());
		subEquipmentItem.setRepairId(equipChangeActivity.getRepairId());
		subEquipmentItem.setSmNum(equipment.getSerialNumber());
		subEquipmentItem.setTechnologyType(equipment.getProductTechnologyType());
		subEquipmentItem.setKbProductCode(equipment.getProductCode());
		subEquipmentItem.setProductStatusCode(equipment.getProductStatusCode());
		subEquipmentItem.setProductClassCode(equipment.getProductClassCode());
		subEquipmentItem.setProductGroupTypeCode(equipment.getProductGroupTypeCode());
		return subEquipmentItem;

	}


	private static SubscriptionStatusItem prepareSubscriptionStatusItem(SubscriberStatusChangeActivity statusActivity, ServiceRequestHeader requestHeader) {

		SubscriptionStatusItem item = new SubscriptionStatusItem();

		// Set header and other event detail
		setBaseItemRequestData(item, statusActivity.getStatusChangeEnum().getActionTypeId(), statusActivity.getStatusChangeEnum().getEvent().getEventName(),statusActivity.getStatusChangeEnum().getEvent().getEventDesc(),
				ServiceRequestConstants.PROVISIONING_TYPE_NON_PROVISIONABLE.getValue(), ServiceRequestConstants.SUBSCRIBER_STATUS_CHANGE_SERVICE_REQUEST_SEQ_NUMBER.getValue(),
				requestHeader);

		// Set subscriber specific event details ( knowbility)
		item.setKbSubscriberId(statusActivity.getSubscriberId());
		item.setCellPhoneNum(statusActivity.getPhoneNumber());
		item.setKbBan(Long.valueOf(statusActivity.getBan()));
		item.setSubscriberStatusCode(statusActivity.getStatusChangeEnum().getNewStatus().getValue());
		item.setActivationDate(getSqlTimeStamp(statusActivity.getActivationDate()));
		item.setDeactivationDate(getSqlTimeStamp(statusActivity.getDeactivationDate()));
		item.setKbActivityReasonCode(statusActivity.getReason().trim());
		item.setObjectTypeId(ServiceRequestConstants.OBJECT_TYPE_SERVREQ_SUBSCRIPTION_STATUS.getValue());

		return item;
	}

	private static ServiceRequestItem setBaseItemRequestData(ServiceRequestItem item, int actionTypeId,
			String eventName, String eventDesc, int provisioningTypeId, int sequenceNumber,ServiceRequestHeader header) {

		item.setActionTypeId(actionTypeId);
		item.setApplicationTypeId(header.getApplicationTypeId());
		item.setItemName(eventName);
		item.setDescription(eventDesc);
		item.setItemEffectiveDate(Objects.isNull(header.getEffectiveDate()) ? null : new java.sql.Timestamp(header.getEffectiveDate().getTime()));
		item.setStatus(header.getStatus());
		item.setProvisioningTypeId(provisioningTypeId);
		item.setBusinessAreaTypeId(ServiceRequestConstants.BUSINESS_AREA_TYPE_SUBSCRIBER.getValue());
		item.setSequenceNum(sequenceNumber);

		return item;
	}

	private static List<Actor> constructBaseActorListForActivity(int banId, String subscriberId, String dealerCode, String salesRepCode, String userId) {

		List<Actor> actorList = new ArrayList<>();

		// Create ban actor
		actorList.add(createActor(ServiceRequestConstants.ACTOR_TYPE_KB_ACCOUNT.getValue(), ServiceRequestConstants.ACTOR_ROLE_TARGET.getValue(), String.valueOf(banId)));

		// Create subscriber actor if provided
		if (StringUtils.isNotBlank(subscriberId)) {
			actorList.add(createActor(ServiceRequestConstants.ACTOR_TYPE_KB_SUBSCRIBER.getValue(), ServiceRequestConstants.ACTOR_ROLE_TARGET.getValue(), subscriberId));
		}

		// Create dealer actor
		actorList.add(createActor(ServiceRequestConstants.ACTOR_TYPE_KB_DEALER_CD.getValue(), ServiceRequestConstants.ACTOR_ROLE_OF_RECORD.getValue(), dealerCode));

		// Create sales rep actor
		actorList.add(createActor(ServiceRequestConstants.ACTOR_TYPE_KB_SALES_REP_PIN.getValue(), ServiceRequestConstants.ACTOR_ROLE_OF_RECORD.getValue(), salesRepCode));

		// Create user actor
		actorList.add(createActor(ServiceRequestConstants.ACTOR_TYPE_KB_OPERATOR_ID.getValue(), ServiceRequestConstants.ACTOR_ROLE_OF_RECORD.getValue(), userId));

		return actorList;
	}

	private static ServiceRequestHeader createServiceRequestHeader(BaseActivity source, List<Actor> actors, EventDetailEnum eventDetailEnum) {

		Objects.requireNonNull(source.getServiceRequestHeader(), "ServiceRequestHeader can't be null");

		// Create service request header
		ServiceRequestHeader requestHeader = new ServiceRequestHeader();

		// Set event details
		requestHeader.setEventTypeId(eventDetailEnum.getEventId());
		requestHeader.setName(eventDetailEnum.getEventName());
		requestHeader.setDescription(eventDetailEnum.getEventDesc());
		requestHeader.setEffectiveDate(getSqlTimeStamp(source.getDate()));
		requestHeader.setPriorityTypeId(ServiceRequestConstants.PRIORITY_MEDIUM.getValue());

		// Set status
		Status status = createStatus(source);
		requestHeader.setStatus(status);

		// Set the required actors based on activity
		requestHeader.getActorList().addAll(actors);

		// Set app details
		com.telus.cis.common.srpds.domain.ServiceRequestHeader sourceHeader = source.getServiceRequestHeader();
		requestHeader.setApplicationTypeId(sourceHeader.getApplicationId());
		requestHeader.setLanguageCode(sourceHeader.getLanguageCode());

		// Set reference number
		if (StringUtils.isNotBlank(sourceHeader.getReferenceNumber())) {
			requestHeader.setReferenceNum(sourceHeader.getReferenceNumber());
		}

		// Set parent request
		if (!Objects.isNull(sourceHeader.getParentRequest())) {
			ParentServiceRequestPk parentServiceRequestPk = new ParentServiceRequestPk();
			parentServiceRequestPk.setId(sourceHeader.getParentRequest().getParentId());
			parentServiceRequestPk.setTimestamp(sourceHeader.getParentRequest().getTimestamp());
			parentServiceRequestPk.setRelationshipTypeId(sourceHeader.getParentRequest().getRelationshipTypeId());
			requestHeader.setParentServiceRequestPk(parentServiceRequestPk);
		}

		return requestHeader;
	}

	private static Status createStatus(BaseActivity activity) {

		// Create status
		Status status = new Status();
		Actor userActor = createActor(ServiceRequestConstants.ACTOR_TYPE_KB_OPERATOR_ID.getValue(), ServiceRequestConstants.ACTOR_ROLE_PROCESSOR.getValue(), activity.getUserId());
		status.getActorList().add(userActor);
		status.setStatusTypeId(ServiceRequestConstants.STATUS_COMPLETED.getValue());
		status.setBusinessDate(getSqlTimeStamp(activity.getDate()));
		status.setApplicationTypeId(activity.getServiceRequestHeader().getApplicationId());

		// Set the service request note
		ServiceRequestNote serviceRequestNote = activity.getServiceRequestHeader().getNote();
		if (!Objects.isNull(serviceRequestNote)) {
			Note note = new Note();
			note.setNoteTypeId(serviceRequestNote.getNoteTypeId());
			note.setText(serviceRequestNote.getNoteText());
			note.setActor(userActor);
			status.setNote(note);
		}

		return status;
	}

	private static Actor createActor(int actorType, int actorRole, String source) {

		Actor actor = new Actor();
		actor.setActorTypeId(actorType);
		actor.setActorRoleTypeId(actorRole);
		actor.setSourceActorCode(source);

		return actor;
	}

	private static Timestamp getSqlTimeStamp(OffsetDateTime date) {
		return Optional.ofNullable(date).map(OffsetDateTime::toInstant).map(Timestamp::from).orElse(null);
	}

}
