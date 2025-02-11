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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.telus.cis.common.redis.domain.CollectionProperties;



class RedisCacheConfigTest
{
    private CacheManager cacheManager;
    
    private Environment environment;
    
    private CollectionProperties properties;
    
    private RedisCacheConfig cacheConfig;
    
    private LettuceConnectionFactory connectionFactory;
    
    private CacheErrorHandler errorHandler;
    
    private CacheResolver cacheResolver;
    
    private RedisTemplate<Object, Object> redisTemplate;
    
    private String [] profiles = {"testing"};

    @BeforeEach
    void setUp() throws Exception
    {
        environment = mock( Environment.class );
        
        doReturn(profiles).when(environment).getActiveProfiles();
        
        properties = new CollectionProperties();
        properties.setHost( "localhost" );
        properties.setPort( 6379 );
        properties.setMinIdle( 1 );
        
        
        cacheConfig = new RedisCacheConfig( environment );
        cacheConfig.setCollectionProperties( properties );
        
        
        connectionFactory = cacheConfig.connectionFactory();
        
        cacheManager = cacheConfig.redisCacheManager( connectionFactory );
        
        errorHandler = cacheConfig.errorHandler();
        
        cacheResolver = cacheConfig.cacheResolver();
        
        redisTemplate = cacheConfig.redisTemplate( connectionFactory );
    }


    @Test
    void testRedisConfig()
    {
        assertNotNull( cacheConfig );
        assertNotNull( connectionFactory );
        assertNotNull( cacheManager );
        assertNotNull( errorHandler );
        assertNotNull( cacheResolver );
        assertNotNull( redisTemplate );
    }

    
    @Test
    void testRedisConfigWithPassword()
    {
        properties.setPassword( "test" );
        properties.setTimeout( 300L );
        connectionFactory = cacheConfig.connectionFactory();
        
        assertNotNull( connectionFactory );
        
    }
}
