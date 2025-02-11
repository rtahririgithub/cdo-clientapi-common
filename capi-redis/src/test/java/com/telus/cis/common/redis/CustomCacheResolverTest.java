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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;


@Slf4j
class CustomCacheResolverTest
{
    private CustomCacheResolver resolver;
    private CacheManager cacheManager;
    private String[] activeProfiles = {"s", "b" };

        
    @BeforeEach
    void setUp() throws Exception
    {
        cacheManager = mock ( CacheManager.class );
    }


    @Test
    void testResolveCaches()
    {
        resolver = new CustomCacheResolver( cacheManager, activeProfiles );
        Collection<? extends Cache> caches = resolver.resolveCaches( null );
        log.info( "caches {} ", caches.size()  );
        assertEquals( caches.size(), 1 );
    }
    
    
    @Test
    void testResolveCaches_emptyProfile()
    {
        activeProfiles = new String[0];
        resolver = new CustomCacheResolver( cacheManager, activeProfiles );
        Collection<? extends Cache> caches = resolver.resolveCaches( null );
        log.info( "caches {} ", caches.size()  );
        assertEquals( caches.size(), 1 );
    }
    
    
    @Test
    void testResolveCaches_nullProfile()
    {
        resolver = new CustomCacheResolver( cacheManager, activeProfiles );
        Collection<? extends Cache> caches = resolver.resolveCaches( null );
        log.info( "caches {} ", caches.size()  );
        assertEquals( caches.size(), 1 );
    }

    
}
