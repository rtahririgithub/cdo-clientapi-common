package com.telus.cis.common.web.security;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.telus.cis.common.core.domain.CommonConstantEnum;
import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Check/validate if the client is allowed to access specific resource(s).
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class FineGrainedAuthorizer {

	public static final String HTTP_SERVLET_REQUEST_ATTR_AUTHORIZED_CLIENT = "authorized_client";
	public static final String SDF_APPLICATION_HEADER = "X-TELUS-SDF-AppId";
	public static final String JWT_CLIENT_ID = "client_id";

	@Autowired
	private final AuthorizedClient authorizedClient;

	@Before("@annotation(com.telus.cis.common.web.security.ValidAccessGrant)")
	public void validAccessGrantAround(JoinPoint joinPoint) {
		
		HttpServletRequest request = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
				.filter(ServletRequestAttributes.class::isInstance)
				.map(ServletRequestAttributes.class::cast)
				.map(ServletRequestAttributes::getRequest)
				.orElseThrow(() -> new ApiException(ApiError.builder()
						.code(HttpStatus.BAD_REQUEST.toString())
						.status(HttpStatus.BAD_REQUEST.value())
						.reason(HttpStatus.BAD_REQUEST.getReasonPhrase())
						.message("HttpRequest was not found from RequestAttributes.")
						.build()));
		log.debug("request.getContextPath(): [{}], request.getRequestURI(): [{}]", request.getContextPath(), request.getRequestURI());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		log.debug("authentication: {}", authentication);

		validateUser(authentication, request);
	}

	/**
	 * Validate if the client is allowed to access the given resource (URI). Put valid clients in HttpServletRequest attribute.
	 * 
	 * @param authentication Authentication object coming through Spring security context.
	 */
	private void validateUser(Authentication authentication, HttpServletRequest request) {

		StringBuilder errorBuilder = new StringBuilder("Unauthorized exception with [").append(request.getRemoteHost()).append("]");

		if (authentication == null || authentication.getPrincipal() == null) {
			errorBuilder.append(" authentication or its principal can not be null");
			ApiError apiError = ApiError.builder()
					.code(HttpStatus.UNAUTHORIZED.toString())
					.status(HttpStatus.UNAUTHORIZED.value())
					.reason(HttpStatus.UNAUTHORIZED.getReasonPhrase())
					.message(errorBuilder.toString())
					.build();
			throw new ApiException(apiError);
		}

		String clientId = validateClaims(authentication, request);

		if (!authorizedClient.allowedClient(request, clientId)) {
			errorBuilder.append(" clientId [").append(clientId).append("] is not allowed for this resource [").append(request.getRequestURI()).append("]");
			ApiError apiError = ApiError.builder()
					.code(HttpStatus.UNAUTHORIZED.toString())
					.status(HttpStatus.UNAUTHORIZED.value())
					.reason(HttpStatus.UNAUTHORIZED.getReasonPhrase())
					.message(errorBuilder.toString())
					.build();
			throw new ApiException(apiError);
		}
		request.setAttribute(HTTP_SERVLET_REQUEST_ATTR_AUTHORIZED_CLIENT, authorizedClient.getClient(clientId));

		log.debug("Access validated/granted: clientId [{}], coming from [{}].", clientId, request.getRemoteHost());
	}

	/**
	 * Get client id from {@link Authentication} -> {@link JwtAuthenticationToken}
	 * 
	 * Note: For SDF clients, extra work is required to establish client identity. The below method of identifying SDF clients utilizes the short-term solution
	 * (i.e., the custom 'X-TELUS-SDF-AppId' header) as discussed with the API Gateway team (Edmund Leung and Manjul Dube) on 2021/09/02. TODO once long-term
	 * solution is implemented by API Gateway team (i.e., providing SDF app identity as a bearer token claim), refactor this code.
	 */
	private String validateClaims(Authentication authentication, HttpServletRequest request) {

		String clientId = Optional.ofNullable(authentication)
				.filter(JwtAuthenticationToken.class::isInstance)
				.map(JwtAuthenticationToken.class::cast)
				.map(t -> t.getTokenAttributes().get(JWT_CLIENT_ID))
				.map(Objects::toString)
				.orElseGet(() -> {
					log.info("Cannot locate client ID in principal.");
					return StringUtils.EMPTY;
				});
		return Optional.ofNullable(authorizedClient.getClient(clientId))
				.filter(detail -> StringUtils.equalsIgnoreCase(detail.getClientName(), "SDF"))
				.map(detail -> StringUtils.join(new String[] { detail.getClientId(), StringUtils.defaultString(request.getHeader(SDF_APPLICATION_HEADER)) },
						CommonConstantEnum.DASH_DELIMITER.value()))
				.orElse(clientId);
	}

}