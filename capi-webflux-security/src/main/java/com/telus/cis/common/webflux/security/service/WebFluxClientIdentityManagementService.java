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

package com.telus.cis.common.webflux.security.service;

import static com.telus.cis.common.core.domain.CommonConstantEnum.ORIGINATING_CLIENT;
import static com.telus.cis.common.core.domain.CommonConstantEnum.USER_ID_TOKEN_HEADER;

import java.text.ParseException;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import com.telus.cis.common.core.domain.ClientIdentity;
import com.telus.cis.common.core.exception.IdentityValidationException;
import com.telus.cis.common.webflux.security.AuthorizedClient;
import com.telus.cis.common.webflux.security.AuthorizedClient.AuthorizedClientDetail;
import com.telus.cis.common.webflux.security.dao.ClientIdentityDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for task(s) related to Kong client and related KB identity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebFluxClientIdentityManagementService
{
    private final ClientIdentityDao clientIdentityDao;
    private final AuthorizedClient authorizedClient;



    /**
     * Client identity is determined in the following order of precedence:
     * <ol>
     * <li> agent user ID (from x-id-token header :: token -> jwtClaimSet -> subject)
     * <li> originating client ID (from originator-client-id header)
     * <li> client ID from request (from authorization header)
     * <br/>get Client Identity Detail
     * </ol>
     */
    public ClientIdentity getKnowbilityIdentity(AuthorizedClientDetail clientDetail, ServerHttpRequest httpRequest)
    {
        String userId = extractAgentUserIdFromRequest( httpRequest );
        if ( StringUtils.isNotBlank( userId ) ) {
            return getClientIdentity( userId, clientDetail.getApplicationCode() );
        }
        String originatingClientId = extractOriginatingClientIdFromRequest( httpRequest );
        if ( StringUtils.isNotBlank( originatingClientId ) ) {
            return getKnowbilityIdentity( originatingClientId );
        }
        return getClientIdentity( clientDetail );
    }


    public ClientIdentity getKnowbilityIdentity(String clientId)
    {
        return getClientIdentity( Optional.ofNullable( authorizedClient.getClient( clientId ) )
                .orElseThrow( () -> new IdentityValidationException( "Client was not found in authorization list." ) ) );
    }


    protected ClientIdentity getClientIdentity(AuthorizedClientDetail clienDetailt)
    {
        try {
            return clientIdentityDao.retrieveClientIdentity( clienDetailt.getApplicationCode(), clienDetailt.getKnowbilityUserId(),
                    clienDetailt.getKnowbilityCredential() );
        }
        catch ( IllegalBlockSizeException | BadPaddingException e ) {
            log.error( "{} exception {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "Exception ", e );
            throw new IdentityValidationException(
                    String.format( "retrieve applicationCode: [%s] error.", clienDetailt.getApplicationCode() ) );
        }
    }


    protected ClientIdentity getClientIdentity(String userId, String applicationCode)
    {
        try {
            return clientIdentityDao.retrieveClientIdentity( userId, applicationCode );
        }
        catch ( IllegalBlockSizeException | BadPaddingException e ) {
            log.error( "{} exception {}", e.getClass().getSimpleName(), e.getLocalizedMessage() );
            log.trace( "Exception ", e );
            throw new IdentityValidationException(
                    String.format( "Invalid paramter id [%s] applicationCode: [%s].", userId, applicationCode ) );
        }
    }


    /**
     * Responsible for extraction of user/agent id from x-id-token header.
     */
    protected static String extractAgentUserIdFromRequest(ServerHttpRequest httpRequest)
    {
        String jwtIdToken = httpRequest.getHeaders().getFirst( USER_ID_TOKEN_HEADER.value() );
        return Optional.ofNullable( jwtIdToken ).map( token -> {
            try {
                return JWTParser.parse( token );
            }
            catch ( ParseException e ) {
                return null;
            }
        } ).map( jwt -> {
            try {
                return jwt.getJWTClaimsSet();
            }
            catch ( ParseException e ) {
                return null;
            }
        } ).map( JWTClaimsSet::getSubject ).orElse( null );
    }


    /**
     * Responsible for extraction of originator-client-id header.
     */
    protected static String extractOriginatingClientIdFromRequest(ServerHttpRequest httpRequest)
    {
        return httpRequest.getHeaders().getFirst( ORIGINATING_CLIENT.value() );
    }


}
