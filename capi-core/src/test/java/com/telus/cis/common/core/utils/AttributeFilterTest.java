/*
 *  Copyright (c) 2018 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.utils;

import static com.telus.cis.common.core.utils.ApiUtils.getAllFields;
import static com.telus.cis.common.core.utils.ApiUtils.prettyWrite;
import static com.telus.cis.common.core.utils.AttributeUtils.createAttributeList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.cis.common.core.module.EntityModule;
import com.telus.cis.common.core.module.TestEnum;
import com.telus.cis.common.core.module.TestModule;
import com.telus.cis.common.core.test.JunitTestUtils;

import lombok.extern.slf4j.Slf4j;


@Slf4j
class AttributeFilterTest
{
    private List<EntityModule> entityList;
    
    private List<TestModule> testList;
    
    private Map<String, String> paramMap;
    
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception
    {
        paramMap = new HashMap<>();
        entityList = new ArrayList<>();
        
        objectMapper = JunitTestUtils.getObjectMapper();
        
        EntityModule entity =  EntityModule.builder().id( "1" )
                .stringVal( "string1" ).bigDecimal( BigDecimal.valueOf( 1 ) )
                .dateVal( OffsetDateTime.now() ).numberVal1( 1L ).numberVal2( 1.1D ).build();
        entityList.add( entity );
        
        entity =  EntityModule.builder().id( "2" ).booleanValue(true)
                .stringVal( "string2" ).bigDecimal( BigDecimal.valueOf( 2 ) )
                .dateVal( OffsetDateTime.now() ).numberVal1( 2L ).numberVal2( 2.2D ).build();
        entityList.add( entity );
        
        entity =  EntityModule.builder().id( "3" ).booleanValue(false)
                .bigDecimal( BigDecimal.valueOf( 3 ) )
                .dateVal( OffsetDateTime.now() ).numberVal2( 3.3D ).build();
        entityList.add( entity );
        
        testList = new ArrayList<>();
        TestModule testModule = TestModule.builder().intVal1( 1 ).enumVal( TestEnum.FIRST ).build();
        testList.add( testModule );
        
        testModule = TestModule.builder().intVal1( 2 ).enumVal( TestEnum.SECOND ).build();
        testList.add( testModule );
        
        testModule = TestModule.builder().intVal1( 3 ).enumVal( TestEnum.THIRD ).build();
        testList.add( testModule );
        
    }


    @Test
    void testAttriburteMap_withId()
    {
        paramMap.put( "id", "2");
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, result ), result.size() );
        assertThat( result.size(), equalTo( 1 ) );
    }
    
 
    @Test
    void testAttriburteMap_withMultiIds_12()
    {
        paramMap.put( "id", "1, 2");
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, result ), result.size() );
        assertThat( result.size(), equalTo( 2 ) );
    }
    
    
    @Test
    void testAttriburteMap_withMultiIds_32()
    {
        paramMap.put( "id", "3, 2");
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, result ), result.size() );
        assertThat( result.get( 1 ).getId(), equalTo( "3" ) );
        assertThat( result.size(), equalTo( 2 ) );
    }


    @Test
    void testAttriburteMap_withAttributes()
    {
        paramMap.put( "id", "1");
        paramMap.put( "numberVal1", "2L");
        
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, result ), result.size() );
        assertThat( result.get( 1 ).getId(), equalTo( "2" ) );
        assertThat( result.size(), equalTo( 2 ) );
    }

    
    
    @Test
    void testAttriburteMap_withBooleaNull()
    {
        paramMap.put( "booleanValue", "null");
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), result.size() );
        assertThat( result.size(), equalTo( 1 ) );
    }
    
    
    @Test
    void testAttriburteMap_withBooleaTrue_List()
    {
        paramMap.put( "booleanValue", "true");
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), result.size() );
        assertThat( result.size(), equalTo( 1 ) );
        assertThat( result.get(0).getId(), equalTo( "2" ) );
    }
    
    
    @Test
    void testAttriburteMap_withBooleaFalse_list()
    {
        paramMap.put( "booleanValue", "false");
        List<EntityModule> result = AttributeUtils.applyAttributeFilterList( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), result.size() );
        assertThat( result.size(), equalTo( 1 ) );
        assertThat( result.get(0).getId(), equalTo( "3" ) );
    }
    
    
    @Test
    void testAttriburteMap_withBoolean_False()
    {
        paramMap.put( "booleanValue", "false");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 1 ) );
        assertThat( entityList.get(0).getId(), equalTo( "3" ) );
    }
    
    
    @Test
    void testAttriburteMap_withBoolean_True()
    {
        paramMap.put( "booleanValue", "true");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 1 ) );
        assertThat( entityList.get(0).getId(), equalTo( "2" ) );
    }
    
    
    @Test
    void testAttriburteMap_withBoolean_TrueNull()
    {
        paramMap.put( "booleanValue", "true, null");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 2 ) );
        assertThat( entityList.get(0).getId(), equalTo( "1" ) );
    }
    
    
    @Test
    void testAttriburteMap_withBoolean_TrueXXX()
    {
        paramMap.put( "booleanValue", "true, XXX");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 3 ) );
    }
    
    
    @Test
    void testAttriburteMap_withEmptyAttribute()
    {
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 3 ) );
    }

    
    @Test
    void testAttriburteMap_withNumberVal1()
    {
        paramMap.put( "numberVal1", "1L");
        AttributeUtils.applyAttributeFilters( entityList, createAttributeList( EntityModule.class, paramMap ) );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 1 ) );
        assertThat( entityList.get(0).getId(), equalTo( "1" ) );
    }
    
    
    @Test
    void testAttriburteMap_withMultiParam()
    {
        paramMap.put( "numberVal1", "1L");
        paramMap.put( "id", "2");
        AttributeUtils.applyAttributeFilters( entityList, createAttributeList( EntityModule.class, paramMap ) );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 2 ) );
        assertThat( entityList.get(0).getId(), equalTo( "1" ) );
    }
    
    
    @Test
    void testAttriburteMap_withNumberVal1_12()
    {
        paramMap.put( "numberVal1", "1L, 2L");
        AttributeUtils.applyAttributeFilters( entityList, createAttributeList( EntityModule.class, paramMap ) );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 2 ) );
        assertThat( entityList.get(0).getId(), equalTo( "1" ) );
    }
    
    
    @Test
    void testAttriburteMap_withNumberVal1_12_bad2()
    {
        paramMap.put( "numberVal1", "1L, 2");
        AttributeUtils.applyAttributeFilters( entityList, createAttributeList( EntityModule.class, paramMap ) );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 1 ) );
        assertThat( entityList.get(0).getId(), equalTo( "1" ) );
    }

      
    @Test
    void testAttriburteMap_withStringVal()
    {
        paramMap.put( "stringVal", "string1");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 1 ) );
        assertThat( entityList.get(0).getId(), equalTo( "1" ) );
    }
    
    
    @Test
    void testAttriburteMap_withStringVal_null()
    {
        paramMap.put( "stringVal", null);
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 0 ) );
    }
    
    
    @Test
    void testAttriburteMap_withStringVal_nullValue()
    {
        paramMap.put( "stringVal", "null");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 1 ) );
        assertThat( entityList.get(0).getId(), equalTo( "3" ) );
    }
    
    
    @Test
    void testAttriburteMap_withStringVal_array()
    {
        paramMap.put( "stringVal", " string1, string2 ");
        AttributeUtils.applyAttributeFilters( entityList, EntityModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, entityList ), entityList.size() );
        assertThat( entityList.size(), equalTo( 2 ) );
    }
    
    
    @Test
    void testAttriburteMap_withEnumVal()
    {
        paramMap.put( "enumVal", "first");
        AttributeUtils.applyAttributeFilters( testList, TestModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, testList ), testList.size() );
        assertThat( testList.size(), equalTo( 1 ) );
        assertThat( testList.get(0).getIntVal1(), equalTo( 1 ) );
    }


    @Test
    void testAttriburteMap_withEnumVal_Second()
    {
        paramMap.put( "enumVal", "second");
        AttributeUtils.applyAttributeFilters( testList, TestModule.class, paramMap );
        log.info( "{} - {}", prettyWrite( objectMapper, testList ), testList.size() );
        assertThat( testList.size(), equalTo( 1 ) );
        assertThat( testList.get(0).getIntVal1(), equalTo( 2 ) );
        assertThat( testList.get(0).getEnumVal(), equalTo( TestEnum.SECOND ) );
    }

    
    @Test
    void testTrimAttributes()
    {
        int trials = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for ( int cnt = 0; cnt < trials; cnt++ ) {
            entityList.get( 0 ).setStringVal("  X  ");
            entityList.forEach( this::trimEntityString );
        }
        stopWatch.stop();
        log.info( "time taken field get/set {} ms {} ns", stopWatch.getTime(), stopWatch.getNanoTime() / (double) trials );
    }
    
    @Test
    void testTrimAttributes_1()
    {
        int trials = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for ( int cnt = 0; cnt < trials; cnt++ ) {
            entityList.get( 0 ).setStringVal("  X  ");
            entityList.forEach( this::trimEntityString1 );
        }
        stopWatch.stop();
        log.info( "time taken {} ms {} ns", stopWatch.getTime(), stopWatch.getNanoTime() / (double) trials );
    }
    
    @Test
    void testTrimAttributes_2()
    {
        int trials = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for ( int cnt = 0; cnt < trials; cnt++ ) {
            entityList.get( 0 ).setStringVal("  X  ");
            entityList.forEach( this::trimEntityString2 );
        }
        stopWatch.stop();
        log.info( "time taken pd {} ms {} ns", stopWatch.getTime(), stopWatch.getNanoTime() / (double) trials );
    }
    
    @Test
    void testTrimAttributes_3()
    {
        int trials = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for ( int cnt = 0; cnt < trials; cnt++ ) {
            entityList.get( 0 ).setStringVal("  X  ");
            entityList.forEach( entity -> {
                String value =  entity.getStringVal();
                if (value != null) {
                    entity.setStringVal( value.trim() );
                }
                entity.setId( entity.getId().trim() );
            });
        }
        stopWatch.stop();
        log.info( "time taken pd {} ms {} ns", stopWatch.getTime(), stopWatch.getNanoTime() / (double) trials );
    }
    
    
    private <T> void trimEntityString2( T entity ) 
    {
        PropertyDescriptor[] propertiesDes = BeanUtils.getPropertyDescriptors( entity.getClass() );
        
        for( PropertyDescriptor pd : propertiesDes ) {
            if ( pd.getPropertyType().isAssignableFrom( String.class ) ) {
                Method readMethod = pd.getReadMethod();
                Method wrireMethod = pd.getWriteMethod();
                try {
                    String value = (String) readMethod.invoke( entity );
                    if ( value != null ) {
                        wrireMethod.invoke( entity, value.trim() );
                    }
                }
                catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                    log.warn( "trimEntity exception {}", e.getLocalizedMessage() );
                    log.trace( "trimEntity", e );
                }
            }
        }
    }
    
    
    private <T> void trimEntityString1( T entity ) 
    {
        getAllFields( entity.getClass() ).forEach( field -> trimEntityString1( entity, field ) );
        
    }
    
    
    private <T> void trimEntityString1(T entity, Field field) 
    {
        String fieldName = field.getName();
        if ( field.getType().isAssignableFrom( String.class ) ) {
            try {
                field.setAccessible(true);
                String value = (String) PropertyUtils.getProperty( entity, fieldName );
                if ( value != null ) {
                    PropertyUtils.setProperty( entity, fieldName, value.trim() );
                }
                
            }
            catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e ) {
                log.warn( "trimEntity exception {}", e.getLocalizedMessage() );
                log.trace( "trimEntity", e );
            }
        }
    }
    
    private <T> void trimEntityString( T entity ) 
    {
        getAllFields( entity.getClass() ).forEach( field -> trimEntityString( entity, field ) );
    }
    
    
    private <T> void trimEntityString(T entity, Field field) 
    {
//        String fieldName = field.getName();
        if ( field.getType().isAssignableFrom( String.class ) ) {
            try {
                field.setAccessible(true);
//                String value = (String) PropertyUtils.getProperty( entity, fieldName );
                String value = (String) field.get( entity );
                if ( value != null ) {
//                    PropertyUtils.setProperty( entity, fieldName, value.trim() );
                    field.set( entity, value.trim() );
                }
            }
            catch ( IllegalArgumentException | IllegalAccessException e ) {
                log.warn( "trimEntity exception {}", e.getLocalizedMessage() );
                log.trace( "trimEntity", e );
            }
        }
    }
   
   
}
