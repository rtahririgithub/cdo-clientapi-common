/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 */

package com.telus.cis.common.amdocs;

import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpStatus;

import com.telus.cis.common.core.domain.ClientIdentity;
import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;

import amdocs.APILink.accesscontrol.APIAccessInfo;
import amdocs.APILink.accesscontrol.APIConnection;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class AmdocsTransactionManager
{
    private final String amdocsUrl;
    private final String uamsUrl;

    @Setter
    private LocalTicketRegistry ticketRegistry;

    public static final Long ERROR_CODE_TUXEDO_FAILURE = 8192L;
    public static final String ERROR_MSG_TUXEDO_DOWN_AFTER_FEW_MIN = "Authentication failed with code 3, and userStatus = 0";
    public static final String ERROR_MSG_INVALID_KB_CREDENTIALS = "Authentication failed with code 4, and userStatus = 0";



    @PostConstruct
    public void init()
    {
        System.setProperty( "amdocs.uams.config.resource", "res/gen/sum/client" );
        System.setProperty( "SEC_SRV_CONN", uamsUrl );
    }


    public AmdocsTransaction beginTransaction(ClientIdentity identity)
    {
        APIAccessInfo accessInfo = connect( identity );
        log.debug( "Connected successfully with ticket [{}]", accessInfo.getTicket() );
        return new AmdocsTransaction( accessInfo );
    }


    @SuppressWarnings( "unused" )
    private APIAccessInfo connect(String ticket, ClientIdentity identity)
    {
        log.debug( "Reconnecting using existing ticket [{}] , amdocsUrl [{}] ...", ticket, amdocsUrl );
        APIAccessInfo accessInfo = APIConnection.connect( amdocsUrl, ticket );
        if ( !isValid( accessInfo ) ) {
            log.debug( "Ticket [{}] is invalid. Reconnecting with client identity.", ticket );
            return connect( identity );
        }
        else {
            log.debug( "Reconnected successfully using existing ticket [{}]...", ticket );
            return accessInfo;
        }
    }


    public APIAccessInfo connect(ClientIdentity identity)
    {
        log.debug( "Opening amdocs transaction for identity [{}]", identity );
        log.debug( "Connecting to amdocs url..." + amdocsUrl );
        APIAccessInfo accessInfo = APIConnection.connect( identity.getKnowbilityUserId(), identity.getKnowbilityCredential(), amdocsUrl,
                identity.getApplicationCode() );
        validateAPIAccessInfo( accessInfo, identity );

        return accessInfo;
    }


    public void validateAPIAccessInfo(APIAccessInfo accessInfo, ClientIdentity identity)
    {
        if ( !isValid( accessInfo ) ) {
            ApiError apiError = ApiError.builder()
                    .code( accessInfo != null ? String.valueOf( accessInfo.getErrorType() ) : "UNKNOWN_ERROR" )
                    .status( getHttpStatusCode( accessInfo ) ).reason( "AmdocsAPIAccessError" )
                    .message( buildErrorMessage( accessInfo, identity ) ).build();
            log.error( "apiError :{}", apiError );
            throw new ApiException( apiError );
        }
    }


    private int getHttpStatusCode(APIAccessInfo accessInfo)
    {
        int httpStatusCode = HttpStatus.BAD_REQUEST.value();
        if ( accessInfo == null || isTuxedoDown( accessInfo ) ) {
            httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        else if ( isInvalidKBCredentials( accessInfo ) ) {
            httpStatusCode = HttpStatus.UNAUTHORIZED.value();
        }
        return httpStatusCode;
    }


    private String buildErrorMessage(APIAccessInfo accessInfo, ClientIdentity identity)
    {
        StringBuilder errorStringBuilder = new StringBuilder( "Unable to acquire new Amdocs ticket using identity: [")
                .append( identity );
        if ( Objects.nonNull( accessInfo ) ) {
            errorStringBuilder.append( ": infoType: [" ).append( accessInfo.getInfoType() )
                              .append( "], errorType [" ).append( accessInfo.getErrorType() );
        }
        return errorStringBuilder.append( "], amdocsUrl: [" ).append( amdocsUrl ).append( "]" ).toString();
    }


    private boolean isTuxedoDown(APIAccessInfo accessInfo)
    {
        return accessInfo != null && ERROR_CODE_TUXEDO_FAILURE.equals( accessInfo.getErrorType() )
                && accessInfo.getInfo().startsWith( ERROR_MSG_TUXEDO_DOWN_AFTER_FEW_MIN );
    }


    private boolean isInvalidKBCredentials(APIAccessInfo accessInfo)
    {
        return accessInfo != null && ERROR_CODE_TUXEDO_FAILURE.equals( accessInfo.getErrorType() )
                && accessInfo.getInfo().startsWith( ERROR_MSG_INVALID_KB_CREDENTIALS );
    }


    private boolean isValid(APIAccessInfo accessInfo)
    {
        return accessInfo != null && accessInfo.getInfoType() != 'E';
    }


    public void closeTransaction(AmdocsTransaction transaction)
    {
        log.debug( "Closing transaction {}", transaction );
        transaction.close();
    }

}