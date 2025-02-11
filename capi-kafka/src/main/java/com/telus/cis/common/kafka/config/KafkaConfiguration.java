package com.telus.cis.common.kafka.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import com.telus.cis.common.core.exception.ValidationException;
import com.telus.cis.common.core.utils.ApiUtils;
import com.telus.cis.common.kafka.rest.KafkaEventRestPublisher;
import com.telus.cis.common.webclient.WebClientConfig;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {
    
	private static final String CLIENT_PROVIDER_SUSBCRIBER = "susbcriber";

    private static final String CLIENT_PROVIDER_ACCOUNT = "account";

    private static final String CLIENT_PROVIDER_ACCOUNT_XML = "accountXml";

    private static final Set<String> ACCEPTED_PROFILES = new HashSet<>(Arrays.asList("dv", "it01", "it02", "it03", "it04", "local", "pr"));
	
	private final Environment environment;
	
	private final KafkaProperties properties;
	
	private final KafkaCredentials credentials;

	private final WebClientConfig webClientConfig;

	@Bean
	public String activeProfile() {
		String[] activeProfiles = environment.getActiveProfiles();
		for (String profile : activeProfiles) {
			String profileName = profile.trim().toLowerCase();
			if (ACCEPTED_PROFILES.contains(profileName)) {
				return profileName;
			}
		}
		return "";
	}
	
	
	private String getCredential(Map<String, String> credentialMap, String key)
	{
	    String result = Optional.ofNullable( credentialMap )
	                        .map( m -> m.get( key ) )
	                        .filter( StringUtils::isNotBlank )
	                        .orElseThrow( () -> new ValidationException( "creatial is undefined for key [" + key + "]") );
	    return ApiUtils.validateSecretVariable( result );
	}
	
	
	public String userName() {
	    return getCredential( credentials.getUsername(), "cmb" );
	}
	
	
	public String credential() {
	    return getCredential( credentials.getCredential(), "cmb" );
	}
	
	
	@Bean
	@Lazy
	public KafkaEventRestPublisher accountJsonKafkaPublisher() {
		WebClient webClient = webClientConfig.webClient(CLIENT_PROVIDER_ACCOUNT, userName(), credential(), properties.getPartyCustomerBillingAccountWirelessUrl().get(activeProfile()));
		return new KafkaEventRestPublisher(webClient, userName());
	}
	
	
	@Bean
	@Lazy
	public KafkaEventRestPublisher subscriberJsonKafkaPublisher() {
		WebClient webClient = webClientConfig.webClient(CLIENT_PROVIDER_SUSBCRIBER, userName(), credential(), properties.getPartyEventCustomerOrderWirelessUrl().get(activeProfile()));
		return new KafkaEventRestPublisher(webClient, userName());
	}
	
	
	@Bean
	@Lazy
	public KafkaEventRestPublisher accountXmlKafkaPublisher() {
		WebClient webClient = webClientConfig.webClient(CLIENT_PROVIDER_ACCOUNT_XML, userName(), credential(), properties.getPartyCustomerBillingAccountUrl().get(activeProfile()));
		return new KafkaEventRestPublisher(webClient, userName());

	}
}
