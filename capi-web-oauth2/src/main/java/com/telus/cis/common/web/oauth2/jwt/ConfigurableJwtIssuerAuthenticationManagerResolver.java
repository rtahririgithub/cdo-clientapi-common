package com.telus.cis.common.web.oauth2.jwt;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.util.Assert;

import com.nimbusds.jwt.JWTParser;

import lombok.extern.slf4j.Slf4j;

public class ConfigurableJwtIssuerAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

	private final AuthenticationManager authenticationManager;
	
	/**
	 * Construct a {@link ConfigurableJwtIssuerAuthenticationManagerResolver} 
	 * using the provided parameters.
	 * 
	 * @param trustedIssuers - a map of trusted issuer(s) configuration properties.
	 */
	public ConfigurableJwtIssuerAuthenticationManagerResolver(Map<String, JwtIssuerProperties> trustedIssuers) {		
		Assert.notEmpty(trustedIssuers, "trustedIssuers cannot be empty");
		this.authenticationManager = new ResolvingAuthenticationManager(new ConfigurableTrustedIssuerJwtAuthenticationManagerResolver(trustedIssuers));
	}
	
	/**
	 * Return an {@link AuthenticationManager} based off of the `iss` claim found in the
	 * request's bearer token.
	 * 
	 * @throws OAuth2AuthenticationException if the bearer token is malformed or an
	 * {@link AuthenticationManager} can't be derived from the issuer.
	 */
	@Override
	public AuthenticationManager resolve(HttpServletRequest request) {
		return this.authenticationManager;
	}

	private static class ResolvingAuthenticationManager implements AuthenticationManager {

		private final Converter<BearerTokenAuthenticationToken, String> issuerConverter = new JwtClaimIssuerConverter();
		private final AuthenticationManagerResolver<String> issuerAuthenticationManagerResolver;

		ResolvingAuthenticationManager(AuthenticationManagerResolver<String> issuerAuthenticationManagerResolver) {
			this.issuerAuthenticationManagerResolver = issuerAuthenticationManagerResolver;
		}

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
			
			Assert.isTrue(authentication instanceof BearerTokenAuthenticationToken,	"Authentication must be of type BearerTokenAuthenticationToken");
			var token = (BearerTokenAuthenticationToken) authentication;
			var issuer = this.issuerConverter.convert(token);
			AuthenticationManager authenticationManager = this.issuerAuthenticationManagerResolver.resolve(issuer);
			if (authenticationManager == null) {
				throw new InvalidBearerTokenException(String.format("Invalid issuer [%s].", issuer));
			}
			
			return authenticationManager.authenticate(authentication);
		}

	}
	
	private static class JwtClaimIssuerConverter implements Converter<BearerTokenAuthenticationToken, String> {

		@Override
		public String convert(@NonNull BearerTokenAuthenticationToken token) {

			try {
				var issuer = JWTParser.parse(token.getToken()).getJWTClaimsSet().getIssuer();
				if (issuer != null) {
					return issuer;
				}
				
			} catch (Exception ex) {
				throw new InvalidBearerTokenException(ex.getMessage(), ex);
			}
			throw new InvalidBearerTokenException("Missing issuer.");
		}

	}
	
	@Slf4j
	public static class ConfigurableTrustedIssuerJwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {
		
		private final Predicate<String> trustedIssuer;
		private final Map<String, JwtIssuerProperties> trustedIssuers;
		private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();
		
		ConfigurableTrustedIssuerJwtAuthenticationManagerResolver(Map<String, JwtIssuerProperties> trustedIssuers) {
			this.trustedIssuer = trustedIssuers.values().stream().map(JwtIssuerProperties::getIssuerUri).collect(Collectors.toList())::contains;
			this.trustedIssuers = trustedIssuers;
		}

		@Override
		public AuthenticationManager resolve(String issuer) {
			
			if (this.trustedIssuer.test(issuer)) {
				
				AuthenticationManager authenticationManager = this.authenticationManagers.computeIfAbsent(issuer, k -> {

					log.debug("Constructing AuthenticationManager...");
					JwtDecoder jwtDecoder = ConfigurableJwtDecoders.fromIssuerConfiguration(getIssuerProperties(k, trustedIssuers));

					return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
				});
				
				log.debug("Resolved AuthenticationManager for issuer {}.", issuer);
				return authenticationManager;
				
			} else {
				log.debug("Did not resolve AuthenticationManager since issuer is not trusted.");
			}
			
			return null;
		}
		
		private static JwtIssuerProperties getIssuerProperties(String issuer, Map<String, JwtIssuerProperties> trustedIssuers) {
			// validate the issuer against the map of trusted issuers and return the matching issuer properties
			return trustedIssuers.values().stream()
					.filter(jwtIssuer -> StringUtils.equalsIgnoreCase(jwtIssuer.getIssuerUri(), issuer))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException(String.format("No trusted issuer found matching the requested issuer [%s].", issuer)));
		}

	}
	
	public static class ConfigurableJwtDecoders {
		
		private static final MappedJwtClaimSetConverter converter = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());
		private static final Pair<String, String> anonymousUserClaim = Pair.of(JwtClaimNames.SUB, "anonymous_user");
		
		private ConfigurableJwtDecoders() { }
		
		private static JwtDecoder fromIssuerConfiguration(JwtIssuerProperties trustedIssuer) {
			
			// create the JWT decoder using the trusted issuer's JWKS URI
			var jwtDecoder = NimbusJwtDecoder.withJwkSetUri(trustedIssuer.getJwkSetUriAccessToken()).build();
			
			// create the delegating token validator with the requisite JWT validators (timestamp, issuer and audience)
			var jwtValidator =  new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(trustedIssuer.getIssuerUri()), 
					new JwtAudienceValidator(trustedIssuer.getAudience()));
			jwtDecoder.setJwtValidator(jwtValidator);
			
		    /**
		     * The purpose of modifying the claims converter here is to insert 'anonymous_user' as the 'sub' claim 
		     * within the JWT. This is required as Spring Security 5 OAuth 2.0 shares the authorized client between
		     * the resource server and webclient instances, and the 'principal_name' cannot be null when making 
		     * downstream calls within an authenticated flow. The alternative solution is to write a custom 
		     * ExchangeFilterFunction to replace the ServerOAuth2AuthorizedClientExchangeFilterFunction used to 
		     * build the webclient, but that is more complicated.
		     */
			jwtDecoder.setClaimSetConverter(claims -> {

				var convertedClaims = converter.convert(claims);
				convertedClaims.putIfAbsent(anonymousUserClaim.getKey(), anonymousUserClaim.getValue());

				return convertedClaims;
			});
			
			return jwtDecoder;
		}

	}

}