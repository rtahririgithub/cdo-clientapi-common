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

import com.telus.cis.common.core.domain.ClientIdentity;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class AmdocsTemplate
{

    private final AmdocsTransactionManager transactionManager;

    @Setter
    private AmdocsApiExceptionHandler exceptionHandler = new AmdocsApiExceptionHandler();



    public void invoke(AmdocsInvocationCallback callback, ClientIdentity identity)
    {

        AmdocsTransaction transaction = transactionManager.beginTransaction( identity );

        try {
            callback.doInTransaction( transaction );
        }
        catch ( Exception e ) {
            exceptionHandler.handleAmdocsException( transaction, null, e );
        }
        finally {
            transactionManager.closeTransaction( transaction );
        }
    }


    public <T> T execute(AmdocsTransactionCallback<T> callback, ClientIdentity identity)
    {

        T result = null;
        AmdocsTransaction transaction = transactionManager.beginTransaction( identity );

        try {
            result = callback.doInTransaction( transaction );
        }
        catch ( Exception e ) {
            exceptionHandler.handleAmdocsException( transaction, null, e );
        }
        finally {
            transactionManager.closeTransaction( transaction );
        }

        return result;
    }

}