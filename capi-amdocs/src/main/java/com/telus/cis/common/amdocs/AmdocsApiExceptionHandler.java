/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.amdocs;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;

import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;

import amdocs.APILink.datatypes.UsrMsgs;
import amdocs.APILink.exceptions.APILinkException;
import amdocs.APILink.exceptions.BackendException;
import amdocs.APILink.exceptions.ValidateException;
import amdocs.APILink.sessions.interfaces.RefDataMngr;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AmdocsApiExceptionHandler
{

    private static final int ERROR_ID_KB_BAN_NOT_FOUND = 114080;
    private static final String ERROR_MSG_KB_BAN_NOT_FOUND = "Billing account number does not exist";



    public void handleAmdocsException(AmdocsTransaction transaction, ApiError apiError, Throwable cause)
    {
        if ( cause instanceof APILinkException ) {

            APILinkException apilex = (APILinkException) cause;
            String kbErrorMsg = Objects.equals( ERROR_ID_KB_BAN_NOT_FOUND, apilex.getErrorInd() )
                    ? ERROR_MSG_KB_BAN_NOT_FOUND
                    : apilex.getErrorMsg();
            log.error( "AmdocsAPILinkException : {} ", kbErrorMsg );
            apiError = ApiError.builder().code( String.valueOf( apilex.getErrorInd() ) ).status( HttpStatus.BAD_REQUEST.value() )
                    .reason( "AmdocsAPILinkException" ).message( kbErrorMsg ).build();

        }
        else if ( cause instanceof BackendException ) {

            BackendException be = (BackendException) cause;
            String message = readTuxedoMessages( be, transaction );
            apiError = ApiError.builder().code( String.valueOf( be.getErrorInd() ) ).status( HttpStatus.BAD_REQUEST.value() )
                    .reason( "AmdocsBackendException" ).message( message ).build();
            log.error( "AmdocsBackendException : {} ", message );

        }
        else if ( cause instanceof ValidateException ) {

            ValidateException ve = (ValidateException) cause;
            apiError = ApiError.builder().code( String.valueOf( ve.getErrorInd() ) ).status( HttpStatus.BAD_REQUEST.value() )
                    .reason( "AmdocsValidateException" ).message( ve.getErrorMsg() ).build();
            log.error( "AmdocsValidateException : {} ", ve.getErrorMsg() );

        }
        else if ( cause instanceof ApiException ) {

            ApiException apiEx = (ApiException) cause;
            log.error( "ApiException: ", apiEx );
            apiError = apiEx.getApiError();

        }
        else if ( cause instanceof Throwable ) {

            log.error( "Amdocs UnknownError: ", cause );
            apiError = ApiError.builder().code( "UNKNOWN_ERROR" ).status( HttpStatus.INTERNAL_SERVER_ERROR.value() )
                    .reason( "AmdocsUnknownError" ).message( cause.getLocalizedMessage() ).build();
        }

        throw new ApiException( apiError, cause );
    }


    private String readTuxedoMessages(BackendException be, AmdocsTransaction transaction)
    {
        StringBuilder sb = new StringBuilder( be.getMessage() ).append( "\n" );
        boolean removeMessage = true;
        log.error( "BackendException occurs: ", be );
        try {
            log.info( "Retrieving Tuxedo message." );

            RefDataMngr refDataMngr = transaction.createBean( RefDataMngr.class );
            sb.append( "Error Messages:\n" );
            UsrMsgs[] messages = refDataMngr.getErrorMessage( removeMessage );

            final AtomicInteger count = new AtomicInteger();
            Stream.of( messages ).forEach( msg -> sb.append( count.incrementAndGet() + " : " ).append( msg.getMsgNum() ).append( "\n" ) );
            messages = refDataMngr.getUserMessage( removeMessage );
            sb.append( "User Messages:\n" );

            Stream.of( messages ).forEach( msg -> sb.append( count.incrementAndGet() + " : " ).append( msg.getMsgNum() ).append( "\n" ) );

        }
        catch ( Exception e ) {
            log.error( "Encountered error while retrieving Tuxedo messages (this exception will be not thrown) - logging exception.", e );
            sb.append( "\n (Retrieving Tuxedo message failed: " ).append( e.getMessage() ).append( ")" );
        }

        return sb.toString();
    }

}