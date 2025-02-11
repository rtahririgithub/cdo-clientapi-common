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

package com.telus.cis.common.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

class CustomCacheErrorHandlerTest
{
    private CustomCacheErrorHandler handler;
    private RuntimeException runTimeException;
    private Cache cache;
    
    @BeforeEach
    void setUp() throws Exception
    {
        handler = new CustomCacheErrorHandler();
        runTimeException = new RuntimeException( "testing" );
        
        cache = mock( Cache.class );
    }


    @Test
    void testHandler()
    {
        assertNotNull(handler);
        handler.handleCacheClearError( runTimeException, cache );
        handler.handleCacheEvictError( runTimeException, cache, "key" );
        handler.handleCacheGetError( runTimeException, cache, "key" );
        handler.handleCachePutError( runTimeException, cache, "key", "value" );
    }

}
