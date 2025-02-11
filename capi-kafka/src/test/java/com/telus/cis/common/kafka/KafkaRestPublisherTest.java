/*
 *  Copyright (c) 2023 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.kafka;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.core.test.JunitTestUtils;
import com.telus.cis.common.core.utils.ApiUtils;
import com.telus.cis.common.kafka.domain.KafkaDataEnum.KafkaMetaData;
import com.telus.cis.common.kafka.rest.KafkaEventRestPublisher;
import com.telus.cis.common.kafka.service.AccountEventPublisher;
import com.telus.cis.common.kafka.service.XmlEventPublisher;
import com.telus.cis.common.webclient.WebClientConfig;
import com.telus.cis.common.webclient.domain.WebClientProperties;
import com.telus.cis.framework.kafka.account_v1.Account;
import com.telus.cis.framework.kafka.account_v1.AccountEvent;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Disabled
class KafkaRestPublisherTest
{

    private WebClient webClient;
    private WebClient xmlWebClient;
    private ObjectMapper objectMapper;
    private WebClientConfig webClientConfig;
    private WebClientProperties clientProperties;
    private KafkaEventRestPublisher kafkaPublisher;
    private KafkaEventRestPublisher kafkaXmlPublisher;
    private AccountEventPublisher accountEventPublisher;
    private XmlEventPublisher xmlEventPublisher;
    
    @BeforeEach
    void setUp() throws Exception
    {
        String url = "https://enterprisemessagewebk-is02.tsl.telus.com:443/publisher/publish/PT168.Party.Customer.BillingAccount.Wireless";
        objectMapper = JunitTestUtils.getObjectMapper();

        clientProperties = new WebClientProperties();
        // clientProperties.setEnv("it01");
        // clientProperties.setBaseUri("https://enterprisemessagewebk-is01.tsl.telus.com:443/publisher/publish/PT168.Party.Customer.BillingAccount.Wireless");
        clientProperties.setBaseUri(url);

        webClientConfig = new WebClientConfig(objectMapper, clientProperties, null );

        webClient = webClientConfig.webClient("kafka", "5284", "CAPI", "https://enterprisemessagewebk-is02.tsl.telus.com:443/publisher/publish/PT168.Party.Customer.BillingAccount.Wireless", MediaType.APPLICATION_JSON_VALUE);
        kafkaPublisher = new KafkaEventRestPublisher(webClient, "5284");



        accountEventPublisher = new AccountEventPublisher(kafkaPublisher);
        accountEventPublisher.accountEventObjMapper();

        xmlWebClient = webClientConfig.webClient("kafka","5284", "CAPI", "https://enterprisemessagewebk-is02.tsl.telus.com:443/publisher/publish/PT168.Party.Customer.BillingAccount", MediaType.APPLICATION_XML_VALUE);
        kafkaXmlPublisher = new KafkaEventRestPublisher(xmlWebClient, "5284");
        xmlEventPublisher = new XmlEventPublisher(kafkaXmlPublisher);
    }


    @Test
    void test_printProperties()
    {
        assertNotNull(webClient);
        String propertyString = clientProperties.toString();
        log.info( "{}", ApiUtils.prettyWrite( objectMapper, clientProperties ) );
        assertFalse(propertyString.contains("test_password"));
    }
    
    
    @Test
    void testPublish() throws Throwable {
        Account account = new Account();
        account.setBan(12345889);
        AccountEvent accountEvent = new AccountEvent();
        
        accountEvent.setAccount(account);
        
        Map<String, String> metaData = new HashMap<>();
        metaData.put(KafkaMetaData.ORIGINATING_APPLICATION_ID.getName(), "12345");
        assertNotNull( metaData );
        
        accountEventPublisher.publishEvent(accountEvent, metaData);
    }
    
    
    @Test
    void testPublishXml() throws Throwable {
        com.telus.evnthdl.publisher.domain.Event event = new com.telus.evnthdl.publisher.domain.Event ();
        event.setName("123name");
        Map<String, String> metaData = new HashMap<String,String>();
        metaData.put(KafkaMetaData.ORIGINATING_APPLICATION_ID.getName(), "5284");
        metaData.put(KafkaMetaData.EVENT_NAME.getName(), "BILLINGACCOUNT_CREATED");
        metaData.put(KafkaMetaData.TRIGGERED_BY_KB_APP_ID.getName(), "TEST");
        metaData.put(KafkaMetaData.ACCOUNT_TYPE.getName(), "I");
        metaData.put(KafkaMetaData.ACCOUNT_SUB_TYPE.getName(), "3");
        metaData.put(KafkaMetaData.BAN.getName(), "1234");
        
        assertNotNull( metaData );
        xmlEventPublisher.publishEvent(event, metaData);
        
        /*
         * eventName=BILLINGACCOUNT_CREATED, originatorApplicationId=5284, eventTriggeredByKBID=ENTRSALESSVC, accountType=I, brand=3, billingAccountNumber=70946813
         */
        /*
<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
<ns2:Event source=\"5284\" timestamp=\"2020-09-22T05:07:06.232-04:00\" specName=\"Party.Customer.BillingAccount\" xmlns:ns2=\"com.telus.evnthdl.publisher.domain\" xmlns=\"http://xmlschema.tmi.telus.com/xsd/common/utils/PubSubQueueObject_v1\">
    <Object>
        <Name>BILLING_ACCOUNT_ALL</Name>
        <Characteristic name=\"status\">
            <Value>T</Value>
        </Characteristic>
        <Characteristic name=\"statusDate\">
            <Value>null</Value>
        </Characteristic>
        <Characteristic name=\"billCycleCloseDay\">
            <Value>0</Value>
        </Characteristic>
        <Characteristic name=\"customerId\">
            <Value>0</Value>
        </Characteristic>
        <Characteristic name=\"createDate\">
            <Value>Tue Sep 22 05:07:04 EDT 2020</Value>
        </Characteristic>
        <Characteristic name=\"billingAccountNumber\">
            <Value>70946813</Value>
        </Characteristic>
        <Characteristic name=\"billingMasterSourceId\">
            <Value>130</Value>
        </Characteristic>
        <Characteristic name=\"billCycle\">
            <Value>30</Value>
        </Characteristic>
        <Characteristic name=\"nextBillCycle\">
            <Value>0</Value>
        </Characteristic>
        <Characteristic name=\"accountType\">
            <Value>I</Value>
        </Characteristic>
        <Characteristic name=\"accountSubTypeCode\">
            <Value>R</Value>
        </Characteristic>
        <Characteristic name=\"startServiceDate\">
            <Value>null</Value>
        </Characteristic>
        <Characteristic name=\"email\">
            <Value>Pavan.V@telus.com</Value>
        </Characteristic>
        <Characteristic name=\"language\">
            <Value>EN</Value>
        </Characteristic>
        <Characteristic name=\"brandId\">
            <Value>3</Value>
        </Characteristic>
        <Characteristic name=\"homePhone\">
            <Value>4373532244</Value>
        </Characteristic>
        <Characteristic name=\"businessPhone\"/>
        <Characteristic name=\"businessPhoneExtension\"/>
        <Characteristic name=\"contactPhone\">
            <Value>4373532244</Value>
        </Characteristic>
        <Characteristic name=\"contactPhoneExtension\"/>
        <Characteristic name=\"contactFax\"/>
        <Characteristic name=\"otherPhoneType\"/>
        <Characteristic name=\"otherPhone\"/>
        <Characteristic name=\"otherPhoneExtension\"/>
        <Object specName=\"contactName\">
            <Characteristic name=\"title\"/>
            <Characteristic name=\"firstName\">
                <Value>KARATE</Value>
            </Characteristic>
            <Characteristic name=\"middleInitial\"/>
            <Characteristic name=\"lastName\">
                <Value>KGASILQCJFEK</Value>
            </Characteristic>
            <Characteristic name=\"generation\">
                <Value/>
            </Characteristic>
            <Characteristic name=\"additionalLine\">
                <Value/>
            </Characteristic>
            <Characteristic name=\"nameFormat\">
                <Value>P</Value>
            </Characteristic>
        </Object>
        <Object specName=\"address\">
            <Characteristic name=\"addressTypeCode\">
                <Value>C</Value>
            </Characteristic>
            <Characteristic name=\"additionalAddressInfo\">
                <Value/>
            </Characteristic>
            <Characteristic name=\"ruralRouteTypeCode\"/>
            <Characteristic name=\"careOf\"/>
            <Characteristic name=\"countryCode\">
                <Value>CAN</Value>
            </Characteristic>
            <Characteristic name=\"municipalityName\">
                <Value>LETHBRIDGE</Value>
            </Characteristic>
            <Characteristic name=\"postOfficeBoxNumber\"/>
            <Characteristic name=\"postalZipCode\">
                <Value>T1K5H5</Value>
            </Characteristic>
            <Characteristic name=\"province\">
                <Value>AB</Value>
            </Characteristic>
            <Characteristic name=\"streetDirectionCode\"/>
            <Characteristic name=\"streetName\">
                <Value>CAYUGA CRES W</Value>
            </Characteristic>
            <Characteristic name=\"streetTypeCode\"/>
            <Characteristic name=\"unitNumber\"/>
            <Characteristic name=\"unitTypeCode\"/>
            <Characteristic name=\"civicNumber\">
                <Value>106</Value>
            </Characteristic>
            <Characteristic name=\"civicNumberSuffix\"/>
            <Characteristic name=\"ruralRouteNumber\"/>
            <Characteristic name=\"stationName\"/>
            <Characteristic name=\"stationQualifier\"/>
            <Characteristic name=\"stationTypeCode\"/>
        </Object>
        <Object specName=\"billingName\" specType=\"postpaid\">
            <Characteristic name=\"title\"/>
            <Characteristic name=\"firstName\">
                <Value>KARATE</Value>
            </Characteristic>
            <Characteristic name=\"middleInitial\"/>
            <Characteristic name=\"lastName\">
                <Value>KGASILQCJFEK</Value>
            </Characteristic>
            <Characteristic name=\"generation\">
                <Value/>
            </Characteristic>
            <Characteristic name=\"additionalLine\">
                <Value/>
            </Characteristic>
            <Characteristic name=\"nameFormat\">
                <Value>P</Value>
            </Characteristic>
        </Object>
    </Object>
</ns2:Event>
         String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:Event source=\"5284\" timestamp=\"2020-09-22T05:07:06.232-04:00\" specName=\"Party.Customer.BillingAccount\" xmlns:ns2=\"com.telus.evnthdl.publisher.domain\" xmlns=\"http://xmlschema.tmi.telus.com/xsd/common/utils/PubSubQueueObject_v1\"><Object><Name>BILLING_ACCOUNT_ALL</Name><Characteristic name=\"status\"><Value>T</Value></Characteristic><Characteristic name=\"statusDate\"><Value>null</Value></Characteristic><Characteristic name=\"billCycleCloseDay\"><Value>0</Value></Characteristic><Characteristic name=\"customerId\"><Value>0</Value></Characteristic><Characteristic name=\"createDate\"><Value>Tue Sep 22 05:07:04 EDT 2020</Value></Characteristic><Characteristic name=\"billingAccountNumber\"><Value>70946813</Value></Characteristic><Characteristic name=\"billingMasterSourceId\"><Value>130</Value></Characteristic><Characteristic name=\"billCycle\"><Value>30</Value></Characteristic><Characteristic name=\"nextBillCycle\"><Value>0</Value></Characteristic><Characteristic name=\"accountType\"><Value>I</Value></Characteristic><Characteristic name=\"accountSubTypeCode\"><Value>R</Value></Characteristic><Characteristic name=\"startServiceDate\"><Value>null</Value></Characteristic><Characteristic name=\"email\"><Value>Pavan.V@telus.com</Value></Characteristic><Characteristic name=\"language\"><Value>EN</Value></Characteristic><Characteristic name=\"brandId\"><Value>3</Value></Characteristic><Characteristic name=\"homePhone\"><Value>4373532244</Value></Characteristic><Characteristic name=\"businessPhone\"/><Characteristic name=\"businessPhoneExtension\"/><Characteristic name=\"contactPhone\"><Value>4373532244</Value></Characteristic><Characteristic name=\"contactPhoneExtension\"/><Characteristic name=\"contactFax\"/><Characteristic name=\"otherPhoneType\"/><Characteristic name=\"otherPhone\"/><Characteristic name=\"otherPhoneExtension\"/><Object specName=\"contactName\"><Characteristic name=\"title\"/><Characteristic name=\"firstName\"><Value>KARATE</Value></Characteristic><Characteristic name=\"middleInitial\"/><Characteristic name=\"lastName\"><Value>KGASILQCJFEK</Value></Characteristic><Characteristic name=\"generation\"><Value></Value></Characteristic><Characteristic name=\"additionalLine\"><Value></Value></Characteristic><Characteristic name=\"nameFormat\"><Value>P</Value></Characteristic></Object><Object specName=\"address\"><Characteristic name=\"addressTypeCode\"><Value>C</Value></Characteristic><Characteristic name=\"additionalAddressInfo\"><Value></Value></Characteristic><Characteristic name=\"ruralRouteTypeCode\"/><Characteristic name=\"careOf\"/><Characteristic name=\"countryCode\"><Value>CAN</Value></Characteristic><Characteristic name=\"municipalityName\"><Value>LETHBRIDGE</Value></Characteristic><Characteristic name=\"postOfficeBoxNumber\"/><Characteristic name=\"postalZipCode\"><Value>T1K5H5</Value></Characteristic><Characteristic name=\"province\"><Value>AB</Value></Characteristic><Characteristic name=\"streetDirectionCode\"/><Characteristic name=\"streetName\"><Value>CAYUGA CRES W</Value></Characteristic><Characteristic name=\"streetTypeCode\"/><Characteristic name=\"unitNumber\"/><Characteristic name=\"unitTypeCode\"/><Characteristic name=\"civicNumber\"><Value>106</Value></Characteristic><Characteristic name=\"civicNumberSuffix\"/><Characteristic name=\"ruralRouteNumber\"/><Characteristic name=\"stationName\"/><Characteristic name=\"stationQualifier\"/><Characteristic name=\"stationTypeCode\"/></Object><Object specName=\"billingName\" specType=\"postpaid\"><Characteristic name=\"title\"/><Characteristic name=\"firstName\"><Value>KARATE</Value></Characteristic><Characteristic name=\"middleInitial\"/><Characteristic name=\"lastName\"><Value>KGASILQCJFEK</Value></Characteristic><Characteristic name=\"generation\"><Value></Value></Characteristic><Characteristic name=\"additionalLine\"><Value></Value></Characteristic><Characteristic name=\"nameFormat\"><Value>P</Value></Characteristic></Object></Object></ns2:Event>";
*/
      //      kafkaXmlPublisher.publish(xmlStr, metaData);
    }

}
