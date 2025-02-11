package com.telus.cis.common.webflux.oauth2.jwt;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import com.nimbusds.jwt.JWTParser;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ConfigurableJwtIssuerReactiveAuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<ServerWebExchange> {

	private final ReactiveAuthenticationManager authenticationManager;
	
	/**
	 * Construct a {@link ConfigurableJwtIssuerReactiveAuthenticationManagerResolver} 
	 * using the provided parameters.
	 * 
	 * @param trustedIssuers - a map of trusted issuer(s) configuration properties.
	 */
	public ConfigurableJwtIssuerReactiveAuthenticationManagerResolver(Map<String, JwtIssuerProperties> trustedIssuers) {		
		Assert.notEmpty(trustedIssuers, "trustedIssuers cannot be empty");
		this.authenticationManager = new ResolvingAuthenticationManager(new ConfigurableTrustedIssuerJwtAuthenticationManagerResolver(trustedIssuers));
	}
	
	/**
	 * Return an {@link ReactiveAuthenticationManager} based off of the `iss` claim found in the
	 * request's bearer token.
	 * 
	 * @throws OAuth2AuthenticationException if the bearer token is malformed or an
	 * {@link ReactiveAuthenticationManager} can't be derived from the issuer.
	 */
	@Override
	public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange exchange) {
		return Mono.just(this.authenticationManager);
	}

	private static class ResolvingAuthenticationManager implements ReactiveAuthenticationManager {

		private final Converter<BearerTokenAuthenticationToken, Mono<String>> issuerConverter = new JwtClaimIssuerConverter();
		private final ReactiveAuthenticationManagerResolver<String> issuerAuthenticationManagerResolver;

		ResolvingAuthenticationManager(ReactiveAuthenticationManagerResolver<String> issuerAuthenticationManagerResolver) {
			this.issuerAuthenticationManagerResolver = issuerAuthenticationManagerResolver;
		}

		@Override
		public Mono<Authentication> authenticate(Authentication authentication) {
			
			Assert.isTrue(authentication instanceof BearerTokenAuthenticationToken,	"Authentication must be of type BearerTokenAuthenticationToken");
			var token = (BearerTokenAuthenticationToken) authentication;
			return this.issuerConverter.convert(token)
					.flatMap((issuer) -> this.issuerAuthenticationManagerResolver.resolve(issuer)
							.switchIfEmpty(Mono.error(() -> new InvalidBearerTokenException(String.format("Invalid issuer [%s].", issuer)))))
					.flatMap((manager) -> manager.authenticate(authentication));
		}

	}
	
	private static class JwtClaimIssuerConverter implements Converter<BearerTokenAuthenticationToken, Mono<String>> {

		@Override
		public Mono<String> convert(@NonNull BearerTokenAuthenticationToken token) {
			
			try {
				var issuer = JWTParser.parse(token.getToken()).getJWTClaimsSet().getIssuer();
				if (issuer == null) {
					throw new InvalidBearerTokenException("Missing issuer.");
				}
				
				return Mono.just(issuer);
				
			} catch (Exception ex) {
				return Mono.error(() -> new InvalidBearerTokenException(ex.getMessage(), ex));
			}
		}

	}
	
	public static class ConfigurableTrustedIssuerJwtAuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<String> {
		
		private final Predicate<String> trustedIssuer;
		private final Map<String, JwtIssuerProperties> trustedIssuers;
		private final Map<String, Mono<ReactiveAuthenticationManager>> authenticationManagers = new ConcurrentHashMap<>();
		
		ConfigurableTrustedIssuerJwtAuthenticationManagerResolver(Map<String, JwtIssuerProperties> trustedIssuers) {
			this.trustedIssuer = trustedIssuers.values().stream().map(JwtIssuerProperties::getIssuerUri).collect(Collectors.toList())::contains;
			this.trustedIssuers = trustedIssuers;
		}

		@Override
		public Mono<ReactiveAuthenticationManager> resolve(String issuer) {

			if (!this.trustedIssuer.test(issuer)) {
				return Mono.empty();
			}

			return this.authenticationManagers
					.computeIfAbsent(issuer, k -> Mono.<ReactiveAuthenticationManager>fromCallable(
							() -> new JwtReactiveAuthenticationManager(ConfigurableReactiveJwtDecoders.fromIssuerConfiguration(getIssuerProperties(k, trustedIssuers))))
					.subscribeOn(Schedulers.boundedElastic())
					.cache((manager) -> Duration.ofMillis(Long.MAX_VALUE), (ex) -> Duration.ZERO, () -> Duration.ZERO));
		}
		
		private static JwtIssuerProperties getIssuerProperties(String issuer, Map<String, JwtIssuerProperties> trustedIssuers) {
			// validate the issuer against the map of trusted issuers and return the matching issuer properties
			return trustedIssuers.values().stream()
					.filter(jwtIssuer -> StringUtils.equalsIgnoreCase(jwtIssuer.getIssuerUri(), issuer))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException(String.format("No trusted issuer found matching the requested issuer [%s].", issuer)));
		}

	}
	
	public static class ConfigurableReactiveJwtDecoders {
		
		private static final MappedJwtClaimSetConverter converter = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());
		private static final Pair<String, String> anonymousUserClaim = Pair.of(JwtClaimNames.SUB, "anonymous_user");
		
		private ConfigurableReactiveJwtDecoders() { }
		
		private static ReactiveJwtDecoder fromIssuerConfiguration(JwtIssuerProperties trustedIssuer) {
			
			// create the reactive JWT decoder using the trusted issuer's JWKS URI
			var jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(trustedIssuer.getJwkSetUriAccessToken()).build();
			
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