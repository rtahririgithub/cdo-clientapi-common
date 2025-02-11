package com.telus.cis.common.web.security.service;

import java.text.ParseException;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import com.telus.cis.common.core.domain.ClientIdentity;
import com.telus.cis.common.core.domain.CommonConstantEnum;
import com.telus.cis.common.core.exception.IdentityValidationException;
import com.telus.cis.common.core.utils.ApiUtils;
import com.telus.cis.common.web.security.AuthorizedClient;
import com.telus.cis.common.web.security.AuthorizedClient.AuthorizedClientDetail;
import com.telus.cis.common.web.security.dao.ClientIdentityDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for task(s) related to Kong client and related KB identity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientIdentityManagementService {

    private final ClientIdentityDao clientIdentityDao;
    private final AuthorizedClient authorizedClient;    

    public ClientIdentity getKnowbilityIdentity() {
        
        /*
         * Client identity is determined in the following order of precedence:
         * 1. agent user ID (from x-id-token header)
         * 2. originating client ID (from originator-client-id header)
         * 3. client ID from request (from authorization header)
         */
        AuthorizedClientDetail clientDetail = extractAuthorizedClientFromRequest();
        
        String userId = extractAgentUserIdFromRequest();
        if (StringUtils.isNotBlank(userId)) {
            return getClientIdentity(userId, clientDetail.getApplicationCode());
        }
        
        String originatingClientId = extractOriginatingClientIdFromRequest();
        if (StringUtils.isNotBlank(originatingClientId)) {
            return getKnowbilityIdentity(originatingClientId);
        }
        
        return getClientIdentity(clientDetail);
    }

    public String getClientId() {
        AuthorizedClientDetail clientDetail = extractAuthorizedClientFromRequest();
        return clientDetail.getClientId();
    }

    public ClientIdentity getKnowbilityIdentity(String clientId) {
        return getClientIdentity(Optional.ofNullable(authorizedClient.getClient(clientId))
                .orElseThrow(() -> new IdentityValidationException("Client was not found in authorization list.")));
    }

    protected ClientIdentity getClientIdentity(AuthorizedClientDetail client) {

        try {
            return clientIdentityDao.retrieveClientIdentity(client.getApplicationCode(), client.getKnowbilityUserId(), 
                    client.getKnowbilityCredential());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("{} exception {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
            log.trace("Exception ", e);
            throw new IdentityValidationException(String.format("Invalid applicationCode: [%s].", client.getApplicationCode()));
        }
    }
    
    protected ClientIdentity getClientIdentity(String userId, String applicationCode) {

        try {
            return clientIdentityDao.retrieveClientIdentity(userId, applicationCode);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("{} exception {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
            log.trace("Exception ", e);
            throw new IdentityValidationException("Invalid user ID.");
        }
    }

    protected static AuthorizedClientDetail extractAuthorizedClientFromRequest() {
        return Optional.ofNullable(getHttpServletRequest().getAttribute(ApiUtils.HTTP_SERVLET_REQUEST_ATTR_AUTHORIZED_CLIENT))
                .map(AuthorizedClientDetail.class::cast)
                .orElseThrow(() -> new IdentityValidationException("Client was not found from HttpRequest."));
    }
    
    /**
     * Responsible for extraction of user/agent id from x-id-token header.
     */
    protected static String extractAgentUserIdFromRequest() {
        
        String jwtIdToken = getHttpServletRequest().getHeader(CommonConstantEnum.USER_ID_TOKEN_HEADER.value());
        String subject = Optional.ofNullable(jwtIdToken)
                .map(token -> {
                    try {
                        return JWTParser.parse(token);  
                    } catch (ParseException e) {
                        return null;    
                    }   
                })
                .map(jwt -> {
                    try {
                        return jwt.getJWTClaimsSet();
                    } catch (ParseException e) {
                        return null;
                    }
                })
                .map(JWTClaimsSet::getSubject)
                .orElse(null);
        if (jwtIdToken != null && subject == null) {
            throw new IdentityValidationException("Invalid user token.");
        }
        
        return subject;
    }
    
    /**
     * Responsible for extraction of originator-client-id header.
     */
    protected static String extractOriginatingClientIdFromRequest() {       
        return getHttpServletRequest().getHeader(CommonConstantEnum.ORIGINATING_CLIENT.value());
    }
    
    private static HttpServletRequest getHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow(() -> new IdentityValidationException("HttpRequest was not found from RequestAttributes."));
    }

}