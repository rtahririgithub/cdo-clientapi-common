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

package com.telus.cis.common.web.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.telus.cis.common.core.utils.ApiUtils;

import lombok.extern.slf4j.Slf4j;


@Slf4j
class Base64UtilsTest
{

    @Test
    void runEncoder()
    {
        String input = "input encoder text";
        input = "";
        String output = Base64.getEncoder().encodeToString( input.getBytes() );
        assertNotNull( output );
        log.info( "output \n[{}]", output );
    }
    
    
    @Test
    void testDecoder()
    {
        String input = "input decoder text";
        input = "";
        String output = new String( Base64.getDecoder().decode( input ) );
        assertNotNull( output );
        log.info( "output \n[{}]", output );
    }
    
    
    @Test
    void testJwt()
    {
        String inputToken = "input token";
        inputToken = "";
        String result = ApiUtils.jwtDecodeClientId( inputToken );
        log.info( "result \n{}", result );
    }


}
