package com.telus.cis.common.srpds.dao;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.core.exception.DownstreamServiceException;
import com.telus.cis.common.core.utils.ApiUtils;
import com.telus.cis.common.srpds.domain.EquipmentChangeActivity;
import com.telus.cis.common.srpds.domain.OfferingInstanceChangeActivity;
import com.telus.cis.common.srpds.domain.SubscriberStatusChangeActivity;
import com.telus.cis.wsclient.ServiceRequestInfoServicePort;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.ServiceRequest;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.StoreServiceRequest;
import com.telus.tmi.xmlschema.srv.cmo.ordermgmt.servicerequestinforequestresponse_v5.StoreServiceRequestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ServiceRequestInformationServiceDao {
	
	private final ServiceRequestInfoServicePort serviceRequestInfoServicePort;
	

	public void reportChangeEquipment(EquipmentChangeActivity equipChangeActivity) {
		
		if(log.isDebugEnabled()){
			log.debug("ServiceRequestInformationServiceDao.reportChangeEquipment called with input :: {} ",equipChangeActivity);
		}
		
		ServiceRequest request = com.telus.cis.common.srpds.utils.ServiceRequestFactory.createEquipmentChangeServiceRequest(equipChangeActivity);
		reportServiceRequestActivity(request,equipChangeActivity.getBan(),equipChangeActivity.getPhoneNumber());
	}

	public void reportChangeSubscriberStatus(final SubscriberStatusChangeActivity statusActivity) {
		
		if(log.isDebugEnabled()){
			log.debug("ServiceRequestInformationServiceDao.subscriberStatusChangeActivity called with input :: {} ",statusActivity);
		}
		
		ServiceRequest request = com.telus.cis.common.srpds.utils.ServiceRequestFactory.createChangeSubscriberStatusServiceRequest(statusActivity);
		reportServiceRequestActivity(request,statusActivity.getBan(),statusActivity.getPhoneNumber());
	}
	
	public void reportChangeOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		
		//report priceplanOfferingInstance changes 
		reportChangePriceplanOfferingInstance(offeringInstanceChangeActivity);
		
		//report addOnOfferingInstance changes 
		reportChangeAddOnOfferingInstance(offeringInstanceChangeActivity);
		
		//report featureOfferingInstance changes 
		reportChangeFeatureOfferingInstance(offeringInstanceChangeActivity);

	}

	public void reportChangePriceplanOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		
		if(log.isDebugEnabled()){
			log.debug("ServiceRequestInformationServiceDao.reportChangePriceplanOfferingInstance called with input :: {} ",offeringInstanceChangeActivity);
		}
		
		// report priceplanOfferingInstance change
		ServiceRequest priceplanChangeRequest = com.telus.cis.common.srpds.utils.ServiceRequestFactory.createPriceplanfferingChangeServiceRequest(offeringInstanceChangeActivity);
		if (Objects.nonNull(priceplanChangeRequest)) {
			reportServiceRequestActivity(priceplanChangeRequest, offeringInstanceChangeActivity.getBan(),offeringInstanceChangeActivity.getPhoneNumber());
		}

	}

	public void reportChangeAddOnOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		
		if(log.isDebugEnabled()){
			log.debug("ServiceRequestInformationServiceDao.reportChangeAddOnOfferingInstance called with input :: {} ",offeringInstanceChangeActivity);
		}
		
		//report addOnOfferingInstance changes 
		ServiceRequest addOnChangeRequest = com.telus.cis.common.srpds.utils.ServiceRequestFactory.createAddOnOfferingChangeServiceRequest(offeringInstanceChangeActivity);
		if(Objects.nonNull(addOnChangeRequest)) {
			reportServiceRequestActivity(addOnChangeRequest,offeringInstanceChangeActivity.getBan(),offeringInstanceChangeActivity.getPhoneNumber());
		}
		
	}

	
	public void reportChangeFeatureOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		
		if(log.isDebugEnabled()){
			log.debug("ServiceRequestInformationServiceDao.reportChangeFeatureOfferingInstance called with input :: {} ",offeringInstanceChangeActivity);
		}
		
		//report addOnOfferingInstance changes 
		ServiceRequest featureUpdateRequest = com.telus.cis.common.srpds.utils.ServiceRequestFactory.createFeatureOfferingChangeServiceRequest(offeringInstanceChangeActivity);
		if(Objects.nonNull(featureUpdateRequest)) {
			reportServiceRequestActivity(featureUpdateRequest,offeringInstanceChangeActivity.getBan(),offeringInstanceChangeActivity.getPhoneNumber());
		}
		
	}

	private void reportServiceRequestActivity(final ServiceRequest request, int ban ,String phoneNumber) {

		Objects.requireNonNull(serviceRequestInfoServicePort, "serviceRequestInfoServicePort can't be null");

		log.debug("serviceRequestInfoServicePort::reportServiceRequestActivity  , serviceRequest {} ",ApiUtils.prettyWrite(new ObjectMapper(), request));
		
		try {
			StoreServiceRequest storeServiceRequest = new StoreServiceRequest();
			storeServiceRequest.setServiceRequest(request);
			storeServiceRequest.setFailOnPersistenceErrorInd(true);
			
			StoreServiceRequestResponse response =  serviceRequestInfoServicePort.storeServiceRequest(storeServiceRequest);
			log.info("ServiceRequestInformationServiceDao::storeServiceRequest  call is successful ,  ban:  {} , phoneNumber: {} , response keyId: {} ",ban,phoneNumber,response.getServiceRequestPk().getId());

		} catch (Exception e) {
			log.error("serviceRequestInfoServicePort::storeServiceRequest error request {} , error {}",ApiUtils.prettyWrite(new ObjectMapper(), request), e.getMessage());
			throw new DownstreamServiceException("serviceRequestInfoServicePort::storeServiceRequest", phoneNumber,e.getMessage());
		}
	}
	
}
