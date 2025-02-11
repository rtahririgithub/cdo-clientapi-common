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

import java.util.HashMap;
import java.util.Map;

import com.telus.cis.common.core.domain.ClientIdentity;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LocalTicketRegistry
{
    private Map<String, String> tickets = new HashMap<>();



    public String getTicket(ClientIdentity identity)
    {
        return tickets.get( getClientIdentityKey( identity ) );
    }


    public void registerTicket(ClientIdentity identity, String ticket)
    {
        String identityKey = getClientIdentityKey( identity );
        log.debug( "Registering ticket [{}] for identity key [{}]", ticket, identityKey );
        tickets.put( identityKey, ticket );
    }


    private String getClientIdentityKey(ClientIdentity identity)
    {
        return new StringBuilder().append( identity.getKnowbilityUserId() ).append( ":" ).append( identity.getApplicationCode() )
                .toString();
    }

}