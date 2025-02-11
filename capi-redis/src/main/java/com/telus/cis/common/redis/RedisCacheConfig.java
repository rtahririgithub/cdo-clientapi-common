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

import static org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
import static org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
import static org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.telus.cis.common.redis.domain.CollectionProperties;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Setter
@Configuration
@EnableCaching
@ConfigurationProperties("cis.redis")
@RequiredArgsConstructor
public class RedisCacheConfig extends CachingConfigurerSupport 
{
    private static final Long DEFAULT_TIMEOUT = 1000L;
    
    private final Environment environment;
    
    private CollectionProperties collectionProperties;
    
    
    @Bean
    public LettuceConnectionFactory connectionFactory()
    {
        log.info( "Redis configurations: {} - {} timeout: {}", collectionProperties.getHost(),  collectionProperties.getPort(), collectionProperties.getTimeout() );
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration( 
                collectionProperties.getHost(), collectionProperties.getPort() );
        
        if ( StringUtils.isNotBlank( collectionProperties.getPassword() ) ) {
            standaloneConfig.setPassword( collectionProperties.getPassword().trim() );
        }
        Long timeout = ( collectionProperties.getTimeout() == null ) ?  DEFAULT_TIMEOUT : collectionProperties.getTimeout();
        TimeoutOptions timeoutOptions = TimeoutOptions.builder().fixedTimeout( Duration.ofMillis( timeout ) ).build();
        SocketOptions socketOptions = SocketOptions.builder().keepAlive( true ).build();
        
        int minIdle = collectionProperties.getMinIdle() > 0 ? collectionProperties.getMinIdle() : DEFAULT_MIN_IDLE;
        int maxIdle = collectionProperties.getMaxIdle() > 0 ? collectionProperties.getMaxIdle() : DEFAULT_MAX_IDLE;
        int maxTotal = collectionProperties.getMaxTotal() > 0 ? collectionProperties.getMaxTotal() : DEFAULT_MAX_TOTAL;
        
        ClientOptions clientOptions = ClientOptions.builder()
                .cancelCommandsOnReconnectFailure( true )
                .autoReconnect( true )
                .timeoutOptions( timeoutOptions )
                .socketOptions(socketOptions).build();
        
        @SuppressWarnings("rawtypes")
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle( minIdle );
        poolConfig.setMaxIdle( maxIdle );
        poolConfig.setMaxTotal( maxTotal );
        LettucePoolingClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .clientOptions( clientOptions )
                .poolConfig( poolConfig )
                .build();

        return new LettuceConnectionFactory( standaloneConfig, clientConfiguration);
    }

    
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory)
    {
        return  RedisCacheManager.create(connectionFactory);
    }
    
    
    @Override
    public CacheErrorHandler errorHandler() 
    {
        return new CustomCacheErrorHandler();
    }


    @Bean
    @Override
    public CacheResolver cacheResolver()
    {
        return new CustomCacheResolver( redisCacheManager( connectionFactory() ), environment.getActiveProfiles() );
    }

    
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    @Primary
    public RedisTemplate<Object, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setKeySerializer( new StringRedisSerializer() );
        template.setValueSerializer( new GenericJackson2JsonRedisSerializer() );
        template.setConnectionFactory( connectionFactory );
        return template;
    }

}
