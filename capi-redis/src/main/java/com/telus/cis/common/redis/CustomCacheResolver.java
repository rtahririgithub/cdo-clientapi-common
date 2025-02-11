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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CustomCacheResolver implements CacheResolver
{
    private final CacheManager cacheManager;
    private final String [] activeProfiles;
   
    
    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context)
    {
        Collection<Cache> caches = new ArrayList<>();
        String profile = ( ( activeProfiles != null ) && ( activeProfiles.length > 0 ) )
                ? activeProfiles[0] : "";
        String cacheName = new StringBuffer("remoteCache").append( "_" ).append( profile ).toString();
        caches.add(cacheManager.getCache( cacheName ) );
        return caches;
    }

}
