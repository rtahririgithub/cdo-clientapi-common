package com.telus.cis.common.kafka.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;
//@PropertySource("classpath:/kafka-${spring.profiles.active}.properties"),

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka")
@PropertySource("classpath:/kafka.properties")
public class KafkaProperties { 
			
	private Map<String, String> partyCustomerBillingAccountWirelessUrl;
	
	private Map<String, String> partyEventCustomerOrderWirelessUrl;
	
	private Map<String, String> partyCustomerBillingAccountUrl;
	
	

	@Override
	public String toString() {
		return "KafkaProperties [partyCustomerBillingAccountWirelessUrl=" + partyCustomerBillingAccountWirelessUrl + ", partyEventCustomerOrderWirelessUrl=" + partyEventCustomerOrderWirelessUrl
				+ ", partyCustomerBillingAccountUrl=" + partyCustomerBillingAccountUrl ;
	}
	
	
}
