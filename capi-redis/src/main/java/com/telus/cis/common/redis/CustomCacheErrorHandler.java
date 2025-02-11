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

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler
{

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key)
    {
        log.error( "Error GET from cache : {}", exception.getLocalizedMessage() );
    }


    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value)
    {
        log.error( "Error PUT from cache : {}", exception.getLocalizedMessage() );
    }


    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key)
    {
        log.error( "Error EVICT from cache : {}", exception.getLocalizedMessage() );
    }


    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache)
    {
        log.error( "Error CLEAR from cache : {}", exception.getLocalizedMessage() );
    }


}
