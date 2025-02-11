package com.telus.cis.common.srpds.service;

import org.springframework.stereotype.Service;

import com.telus.cis.common.core.aspectj.TrackTime;
import com.telus.cis.common.srpds.dao.ServiceRequestInformationServiceDao;
import com.telus.cis.common.srpds.domain.EquipmentChangeActivity;
import com.telus.cis.common.srpds.domain.OfferingInstanceChangeActivity;
import com.telus.cis.common.srpds.domain.SubscriberStatusChangeActivity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class ServiceRequestInformationService {
	
	
	private final ServiceRequestInformationServiceDao serviceRequestInformationServiceDao;

	@TrackTime
	public void reportChangeSubscriberStatus(final SubscriberStatusChangeActivity statusActivity) {
		serviceRequestInformationServiceDao.reportChangeSubscriberStatus(statusActivity);
	}
	
	@TrackTime
	public void reportChangeOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		serviceRequestInformationServiceDao.reportChangeOfferingInstance(offeringInstanceChangeActivity);
	}
	
	@TrackTime
	public void reportChangePriceplanOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		serviceRequestInformationServiceDao.reportChangePriceplanOfferingInstance(offeringInstanceChangeActivity);
	}
	
	@TrackTime
	public void reportChangeAddOnOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		serviceRequestInformationServiceDao.reportChangeAddOnOfferingInstance(offeringInstanceChangeActivity);
	}
	
	@TrackTime
	public void reportChangeFeatureOfferingInstance(final OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		serviceRequestInformationServiceDao.reportChangeFeatureOfferingInstance(offeringInstanceChangeActivity);
	}
	
	@TrackTime
	public void reportChangeEquipment(final EquipmentChangeActivity equipChangeActivity) {
		serviceRequestInformationServiceDao.reportChangeEquipment(equipChangeActivity);
	}

}
