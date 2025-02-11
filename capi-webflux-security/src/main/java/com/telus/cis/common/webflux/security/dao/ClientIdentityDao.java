package com.telus.cis.common.webflux.security.dao;

import java.util.Optional;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import com.telus.cis.common.core.domain.ClientIdentity;
import com.telus.cis.common.core.exception.IdentityValidationException;
import com.telus.cis.common.core.utils.EncrypterDecrypter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClientIdentityDao {

	private final EncrypterDecrypter encrypterDecrypter;
    private final LdapTemplate ldapTemplate;

	@Cacheable("clientIdentityCache")
	public ClientIdentity retrieveClientIdentity(String applicationCode, String knowbilityUserId, String knowbilityCredential) 
			throws IllegalBlockSizeException, BadPaddingException {

		log.info("ClientIdentityDao.retrieveClientIdentity(): clientIdentityCache not used...");

		if (Stream.of(applicationCode, knowbilityUserId, knowbilityCredential).anyMatch(StringUtils::isBlank)) {
			throw new IdentityValidationException("retrieveClientIdentity: invalid parameters.");
		}
		ClientIdentity identity = new ClientIdentity();
		identity.setApplicationCode(applicationCode);
		identity.setKnowbilityUserId(knowbilityUserId);
		identity.setKnowbilityCredential(encrypterDecrypter.decrypt(knowbilityCredential));
		return identity;
	}
	
		
	@Cacheable("clientIdentityCache")
	public ClientIdentity retrieveClientIdentity(String userId, String applicationCode) throws IllegalBlockSizeException, BadPaddingException {

		log.info("ClientIdentityDao.retrieveClientIdentity(): clientIdentityCache not used for userId {}...", userId);
		
		LdapQuery query = LdapQueryBuilder.query().where("uid").is(userId);		
		return Optional.ofNullable(ldapTemplate)
		        .map(ldap -> ldap.search(query, (Attributes attrs) -> {
		            ClientIdentity identity = new ClientIdentity();
		            String knowbilityUserId = Optional.ofNullable(getAttributeValue(attrs, "telusKnowbilityUserName"))
		                    .orElseThrow(() -> new IdentityValidationException("Invalid Knowbility credentials."));
		            identity.setKnowbilityCredential(getAttributeValue(attrs, "telusKnowbilityPassword"));
		            identity.setKnowbilityUserId(knowbilityUserId);
		            identity.setApplicationCode(applicationCode);
		            return identity;
		        }))
		        .filter(CollectionUtils::isNotEmpty)
		        .map(list -> list.get(0))
		        .orElseThrow(() -> new IdentityValidationException("Could not determine Knowbility credentials."));
	}
	

	private String getAttributeValue(Attributes attributes, String name) {
		return Optional.ofNullable(attributes)
				.map(attrs -> attrs.get(name))
				.map(a -> { 
					try { 
						return a.get();
					} catch (NamingException e) {
						return null;
					}
				})
				.map(String.class::cast)				
				.orElse(null);
	}
}