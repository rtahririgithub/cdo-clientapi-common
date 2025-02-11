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

package com.telus.cis.common.webflux.security;

import static com.telus.cis.common.core.domain.CommonConstantEnum.DASH_DELIMITER;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.telus.cis.common.core.exception.IdentityValidationException;
import com.telus.cis.common.webflux.security.AuthorizedClient.AuthorizedClientDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class FineGrainedAccessAuthenticator
{
    public static final String HTTP_SERVLET_REQUEST_ATTR_AUTHORIZED_CLIENT = "authorized_client";
    public static final String SDF_APPLICATION_HEADER = "X-TELUS-SDF-AppId";
    public static final String JWT_CLIENT_ID = "client_id";
    
    private final AuthorizedClient authorizedClient;

    
    public AuthorizedClientDetail validateAccessGrant(ServerHttpRequest request, Principal jwtToken)
    {
        log.debug("request.getContextPath(): [{}], request.getRequestURI(): [{}]",
                request.getPath().toString(), request.getURI().toString());
        return validateUser( request, jwtToken );
    }
    
    
    private  AuthorizedClientDetail validateUser(ServerHttpRequest request, Principal jwtToken)
    {
        String hostName = Optional.ofNullable( request )
                            .map( ServerHttpRequest::getRemoteAddress )
                            .map( InetSocketAddress::getHostName )
                            .orElse( "unknown" );
        StringBuilder errorBuilder = new StringBuilder("Unauthorized exception with [")
                                            .append(hostName).append("]");
        String clientId = validateClaims(request, jwtToken);
        
        if (!authorizedClient.allowedClient(request, clientId)) {
            errorBuilder.append(" clientId [").append(clientId).append("] is not allowed for this resource [")
                    .append( request.getPath() ).append("]");
            throw new IdentityValidationException( errorBuilder.toString() );
        }
        AuthorizedClientDetail authorizedDetail = authorizedClient.getClient( clientId );
        log.debug("access validated/granted: clientId [{}], from host [{}]", clientId, request.getRemoteAddress().getHostString() );
        return authorizedDetail;
    }


    /**
     *  For SDF clients, extra work is required to establish client identity. The below method of identifying SDF clients
     *  utilizes the short-term solution (i.e., the custom 'X-TELUS-SDF-AppId' header) as discussed with the API Gateway
     *  team (Edmund Leung and Manjul Dube) on 2021/09/02. 
     *  TODO once long-term solution is implemented by API Gateway team (i.e., providing SDF app identity as a bearer
     *  token claim), refactor this code.
     */   
    private String validateClaims(ServerHttpRequest request, Principal jwtToken)
    {
        String clientId = Optional.ofNullable( jwtToken )
            .filter( JwtAuthenticationToken.class::isInstance )
            .map( JwtAuthenticationToken.class::cast )
            .map( t -> t.getTokenAttributes().get( JWT_CLIENT_ID ) )
            .map( Objects::toString )
            .orElseGet( () -> {
                log.info( "cannot locate client id in principal" );
                return StringUtils.EMPTY;
            } );
            
        return Optional.ofNullable(authorizedClient.getClient(clientId))
                .filter(detail -> StringUtils.equalsIgnoreCase(detail.getClientName(), "SDF") )
                .map(detail -> StringUtils.join(new String[] { detail.getClientId(),
                                                    StringUtils.defaultString(request.getHeaders().getFirst(SDF_APPLICATION_HEADER) ) },
                        DASH_DELIMITER.value()))
                .orElse( clientId );
    }


}
