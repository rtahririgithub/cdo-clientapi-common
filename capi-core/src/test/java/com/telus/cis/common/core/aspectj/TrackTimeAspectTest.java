/*
 *  Copyright (c) 2022 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.aspectj;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.telus.cis.common.core.module.TestModule;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


class TrackTimeAspectTest
{

    @InjectMocks
    private TrackTimeAspect trackTimeAspect;
    
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Spy
    private Signature signature = new MethodSignature()
    {
        
        @Override
        public String toShortString()
        {
            return null;
        }
        
        
        @Override
        public String toLongString()
        {
            return null;
        }
        
        
        @Override
        public String getName()
        {
            return "mockMethod";
        }
        
        
        @Override
        public int getModifiers()
        {
            return 0;
        }
        
        
        @Override
        public String getDeclaringTypeName()
        {
            return "mockMethod";
        }
        
        
        @Override
        public Class<TestModule> getDeclaringType()
        {
            return TestModule.class;
        }
        
        
        @SuppressWarnings("rawtypes")
        @Override
        public Class[] getParameterTypes()
        {
            return null;
        }
        
        
        @Override
        public String[] getParameterNames()
        {
            return null;
        }
        
        
        @SuppressWarnings("rawtypes")
        @Override
        public Class[] getExceptionTypes()
        {
            return null;
        }
        
        
        @Override
        public Class<String> getReturnType()
        {
            return String.class;
        }
        
        
        @Override
        public Method getMethod()
        {
            return null;
        }
    };
    
    @Mock
    private TrackTime trackTime;

    private TestModule testModule = TestModule.builder().build();

    private Object [] args = { "arg1", testModule, false };
    
    private Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    
    @BeforeEach
    void setUp() throws Throwable
    {
        
        log.setLevel( Level.OFF );
//        MockitoAnnotations.initMocks(this);
        MockitoAnnotations.openMocks( this );
        doReturn( signature ).when( joinPoint ).getSignature();
        doReturn( args ).when( joinPoint ).getArgs();
        when( joinPoint.proceed() ).thenAnswer( inv -> {
            TimeUnit.SECONDS.sleep( 1 );
            return "test";
        } );
        
        doReturn( true ).when( trackTime ).enabled();
        doReturn( true ).when( trackTime ).parameters();
    }


    @Test
    void trackTimeTest() throws Throwable
    {
//        log.setLevel( Level.TRACE );
        assertNotNull(trackTime);
        assertThat( trackTime.enabled(), equalTo( true ) );
        Object result = trackTimeAspect.trackTimeAround( joinPoint, trackTime );
        assertThat( result, equalTo( "test" ) );
    }
    
    
    @Test
    void trackTimeTest_withLogLevelInfo() throws Throwable
    {
//        log.setLevel( Level.INFO );
        assertNotNull(trackTime);
        assertThat( trackTime.enabled(), equalTo( true ) );
        Object result = trackTimeAspect.trackTimeAround( joinPoint, trackTime );
        assertThat( result, equalTo( "test" ) );
    }
    
    
    @Test
    void trackTimeTest_withLogLevelInfo_diabled() throws Throwable
    {
//        log.setLevel( Level.INFO );
        assertNotNull(trackTime);
        assertThat( trackTime.enabled(), equalTo( true ) );
        Object result = trackTimeAspect.trackTimeAround( joinPoint, trackTime );
        assertThat( result, equalTo( "test" ) );
    }
    
    
    @Test
    void trackTimeTest_withLogLevelInfo_disabled() throws Throwable
    {
//        log.setLevel( Level.INFO );
        doReturn(false).when( trackTime ).enabled();
        Object result = trackTimeAspect.trackTimeAround( joinPoint, trackTime );
        assertThat( result, equalTo( "test" ) );
    }
    

    @Test
    void trackTimeTest_withLogLevelInfo_noParameters() throws Throwable
    {
//        log.setLevel( Level.OFF );
        doReturn(false).when( trackTime ).parameters();
        Object result = trackTimeAspect.trackTimeAround( joinPoint, trackTime );
        assertThat( result, equalTo( "test" ) );
    }

 
    @Test 
    void callCutDefinitionTest()
    {
        trackTimeAspect.trackTimePointCutDefinition();
        assertNotNull( trackTime );
    }


}
