package com.telus.cis.common.web.oauth2.jwt;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.util.Assert;

public class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

	private final JwtClaimValidator<Object> validator;
	
	/**
	 * Constructs a {@link JwtAudienceValidator} using the provided parameters.
	 * 
	 * @param audience - The audience that each {@link Jwt} should have.
	 */
	public JwtAudienceValidator(String audience) {		
		Assert.notNull(audience, "audience cannot be null");
		Predicate<Object> testClaimValue = (claimValue) -> claimValue != null && List.class.cast(claimValue).contains(audience);
		this.validator = new JwtClaimValidator<>(JwtClaimNames.AUD, testClaimValue);
	}
	
	@Override
	public OAuth2TokenValidatorResult validate(Jwt token) {
		Assert.notNull(token, "token cannot be null");
		return this.validator.validate(token);
	}	
	
}