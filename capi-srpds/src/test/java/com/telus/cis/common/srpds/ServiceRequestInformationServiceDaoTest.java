package com.telus.cis.common.srpds;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.telus.cis.common.srpds.dao.ServiceRequestInformationServiceDao;
import com.telus.cis.common.srpds.domain.EquipmentChangeActivity;
import com.telus.cis.common.srpds.domain.EquipmentDetail;
import com.telus.cis.common.srpds.domain.FeatureInstanceChange;
import com.telus.cis.common.srpds.domain.OfferingInstanceChange;
import com.telus.cis.common.srpds.domain.OfferingInstanceChangeActivity;
import com.telus.cis.common.srpds.domain.ServiceRequestHeader;
import com.telus.cis.common.srpds.domain.SubscriberStatusChangeActivity;
import com.telus.cis.common.srpds.domain.TimePeriod;
import com.telus.cis.common.srpds.domain.TransactionEnums.EventDetailEnum;
import com.telus.cis.common.srpds.domain.TransactionEnums.OfferingTransactionEnum;
import com.telus.cis.common.srpds.domain.TransactionEnums.StatusChangeEnum;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Disabled
@SpringJUnitConfig( SrpdsConfig.class )
class ServiceRequestInformationServiceDaoTest {

	@Autowired
	private ServiceRequestInformationServiceDao serviceRequestInformationServiceDao;

	
	@Test
	void reportChangeEquipment() {

		int ban = 71041378;
		String phoneNumber = "4160791980";
		String dealerCode = "A001000001";
		String salesRepCode = "0000";

		ServiceRequestHeader serviceRequestHeader = ServiceRequestHeader.builder().applicationId(27)
				.applicationName("SMARTDESKTOP").languageCode("EN").build();

		EquipmentChangeActivity equipmentChangeActivity = EquipmentChangeActivity.builder()
				.eventDetail(EventDetailEnum.EQUIPMENT_CHANGE)
				.ban(ban)
				.phoneNumber(phoneNumber)
				.subscriberId(phoneNumber)
				.newEquipmentDetail(buildUsimData("8912239900006908810"))
				.oldEquipmentDetail(buildUsimData("8912239900006909911"))
				.swapType("REPLACEMENT")
				.repairId(" ")
				.dealerCode(dealerCode)
				.salesRepCode(salesRepCode)
				.userId("18654")
				.serviceRequestHeader(serviceRequestHeader)
				.build();

		try {
			serviceRequestInformationServiceDao.reportChangeEquipment(equipmentChangeActivity);
		} catch (Exception e) {
			log.error("ServiceRequestInformationServiceDao:: reportChangeEquipment error {} ",e.getLocalizedMessage());
			fail();
		}

	}
	
	private EquipmentDetail buildUsimData(String usimId) {
		EquipmentDetail equipDetail = new EquipmentDetail();
		equipDetail.setProductClassCode("USIM");
		equipDetail.setProductGroupTypeCode("1XFM");
		equipDetail.setProductCode("KTRISIM");
		equipDetail.setProductStatusCode("N");
		equipDetail.setProductTechnologyType("1RTT");
		equipDetail.setSerialNumber(usimId);
		return equipDetail;
	}
	
	@Test
	void verifyBeans()  {
		
		try {
			serviceRequestInformationServiceDao.reportChangeSubscriberStatus(getSubscriberStatusActivity());
		} catch (Exception e) {
			log.error("ServiceRequestInformationServiceDao error {} ",e.getLocalizedMessage());
			fail();
		}
	}

	private SubscriberStatusChangeActivity getSubscriberStatusActivity() {
		
		int ban = 	71041378;
		String phoneNumber = "4160791980";
		String dealerCode = "A001000001";
		String salesRepCode = "0000";
		
		ServiceRequestHeader serviceRequestHeader = ServiceRequestHeader.builder().applicationId(27)
				.applicationName("SMARTDESKTOP").languageCode("EN").build();
		
		return SubscriberStatusChangeActivity.builder()
				 .ban(ban).phoneNumber(phoneNumber)
				 .subscriberId(phoneNumber).reason("CR")
				 .activationDate(OffsetDateTime.now())
				 .deactivationDate(OffsetDateTime.now())
				 .dealerCode(dealerCode)
				 .salesRepCode(salesRepCode)
				 .userId("18654")
				 .statusChangeEnum(StatusChangeEnum.CANCEL).serviceRequestHeader(serviceRequestHeader).build();
	}

	@Test
	void reportChangeOfferingInstance_AddOn() {

		int ban = 71041378;
		String phoneNumber = "4160791980";
		String dealerCode = "A001000001";
		String salesRepCode = "0000";

		ServiceRequestHeader serviceRequestHeader = ServiceRequestHeader.builder().applicationId(27)
				.applicationName("SMARTDESKTOP").languageCode("EN").build();

		OfferingInstanceChangeActivity offeringInstanceChangeActivity = OfferingInstanceChangeActivity.builder()
				.eventDetail(EventDetailEnum.ADD_ON_SERVICE_CHANGE)
				.ban(ban)
				.phoneNumber(phoneNumber)
				.subscriberId(phoneNumber)
				.addOnChanges(buildAddOns())
				.dealerCode(dealerCode)
				.salesRepCode(salesRepCode)
				.userId("18654")
				.serviceRequestHeader(serviceRequestHeader)
				.build();

		try {
			serviceRequestInformationServiceDao.reportChangeAddOnOfferingInstance(offeringInstanceChangeActivity);
		} catch (Exception e) {
			log.error("ServiceRequestInformationServiceDao:: reportChangeOfferingInstance error {} ",
					e.getLocalizedMessage());
			fail();
		}

	}
	
	@Test
	void reportChangeOfferingInstance_PP() {

		int ban = 71041378;
		String phoneNumber = "4160791980";
		String dealerCode = "A001000001";
		String salesRepCode = "0000";

		ServiceRequestHeader serviceRequestHeader = ServiceRequestHeader.builder().applicationId(27)
				.applicationName("SMARTDESKTOP").languageCode("EN").build();

		OfferingInstanceChangeActivity offeringInstanceChangeActivity = OfferingInstanceChangeActivity.builder()
				.eventDetail(EventDetailEnum.PRICEPLAN_CHANGE)
				.ban(ban)
				.phoneNumber(phoneNumber)
				.subscriberId(phoneNumber)
				.dealerCode(dealerCode)
				.salesRepCode(salesRepCode)
				.userId("18654")
				.serviceRequestHeader(serviceRequestHeader)
				.build();
		setOldAndNewPriceplan(offeringInstanceChangeActivity);

		try {
			serviceRequestInformationServiceDao.reportChangePriceplanOfferingInstance(offeringInstanceChangeActivity);
		} catch (Exception e) {
			log.error("ServiceRequestInformationServiceDao:: reportChangeOfferingInstance error {} ",
					e.getLocalizedMessage());
			fail();
		}

	}
	

	@Test
	void reportChangeOfferingInstance_Feature() {

		int ban = 71041378;
		String phoneNumber = "4160791980";
		String dealerCode = "A001000001";
		String salesRepCode = "0000";

		ServiceRequestHeader serviceRequestHeader = ServiceRequestHeader.builder().applicationId(27)
				.applicationName("SMARTDESKTOP").languageCode("EN").build();

		OfferingInstanceChangeActivity offeringInstanceChangeActivity = OfferingInstanceChangeActivity.builder()
				.eventDetail(EventDetailEnum.SERVICE_FEATURE_CHANGE)
				.ban(ban)
				.phoneNumber(phoneNumber)
				.subscriberId(phoneNumber)
				.addOnChanges(buildAddOnsWithFeatureUpdate())
				.dealerCode(dealerCode)
				.salesRepCode(salesRepCode)
				.userId("18654")
				.serviceRequestHeader(serviceRequestHeader)
				.build();

		try {
			serviceRequestInformationServiceDao.reportChangeFeatureOfferingInstance(offeringInstanceChangeActivity);
		} catch (Exception e) {
			log.error("ServiceRequestInformationServiceDao:: reportChangeOfferingInstance error {} ",
					e.getLocalizedMessage());
			fail();
		}

	}
	
	private void setOldAndNewPriceplan(OfferingInstanceChangeActivity offeringInstanceChangeActivity) {
		
		OfferingInstanceChange oldPriceplan =  new OfferingInstanceChange();
		oldPriceplan.setOfferingRefId("3VM5RAOL");
		oldPriceplan.setOfferingType("P");
		oldPriceplan.setValidity(TimePeriod.builder().startDateTime(OffsetDateTime.now()).endDateTime(OffsetDateTime.now().plusMonths(1)).build());

		
		OfferingInstanceChange newPriceplan =  new OfferingInstanceChange();
		newPriceplan.setOfferingRefId("3SV5RAN");
		newPriceplan.setOfferingType("P");
		oldPriceplan.setValidity(TimePeriod.builder().startDateTime(OffsetDateTime.now()).endDateTime(OffsetDateTime.now().plusMonths(1)).build());
		
		offeringInstanceChangeActivity.setOldPriceplan(oldPriceplan);
		offeringInstanceChangeActivity.setNewPriceplan(newPriceplan);
		
		
	}

	
	private List<OfferingInstanceChange> buildAddOns() {
		
		OfferingInstanceChange change_add =  new OfferingInstanceChange();
		change_add.setOfferingRefId("3SVVM5RAD");
		change_add.setOfferingType("R");
		change_add.setTransactionType(OfferingTransactionEnum.getTransactionEnumByName("ADD"));
		change_add.setValidity(TimePeriod.builder().startDateTime(OffsetDateTime.now()).endDateTime(OffsetDateTime.now().plusMonths(4)).build());
		
		OfferingInstanceChange change_modify =  new OfferingInstanceChange();
		change_modify.setOfferingRefId("3SVVM5MO");
		change_modify.setOfferingType("R");
		change_add.setTransactionType(OfferingTransactionEnum.getTransactionEnumByName("MODIFY"));

		change_modify.setValidity(TimePeriod.builder().startDateTime(OffsetDateTime.now()).endDateTime(OffsetDateTime.now().plusMonths(4)).build());
		
		OfferingInstanceChange change_remove =  new OfferingInstanceChange();
		change_remove.setOfferingRefId("3SVVM5RE");
		change_remove.setOfferingType("R");
		change_add.setTransactionType(OfferingTransactionEnum.getTransactionEnumByName("REMOVE"));
		change_remove.setValidity(TimePeriod.builder().startDateTime(OffsetDateTime.now()).endDateTime(OffsetDateTime.now().plusMonths(4)).build());
		
		return Arrays.asList(change_add);
		
		
	}
	
	private List<OfferingInstanceChange> buildAddOnsWithFeatureUpdate() {
		
		OfferingInstanceChange change_add =  new OfferingInstanceChange();
		change_add.setOfferingRefId("3SVVM5RAD");
		change_add.setOfferingType("R");
		change_add.setTransactionType(OfferingTransactionEnum.getTransactionEnumByName("MODIFY"));
		change_add.setValidity(TimePeriod.builder().startDateTime(OffsetDateTime.now()).endDateTime(OffsetDateTime.now().plusMonths(4)).build());
		
		
		FeatureInstanceChange feature_modify =  new FeatureInstanceChange();
		feature_modify.setFeatureSpecId("FVTTM");
		feature_modify.setFeatureParam("TEST_PARAM");
		feature_modify.setTransactionType(OfferingTransactionEnum.getTransactionEnumByName("MODIFY"));
		
		change_add.getFeatureInstance().add(feature_modify);
	
		return Arrays.asList(change_add);
		
		
	}


}