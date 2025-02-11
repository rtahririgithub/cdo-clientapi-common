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

package com.telus.cis.common.mapper.resultset;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.telus.cis.common.mapper.resultset.ResultSetMapper.LogHolder;

import lombok.extern.slf4j.Slf4j;


@Slf4j
class ResultSetMapperTest
{
    private ResultSet resultset;
    private SQLException sqlEx;

    @BeforeEach
    void setUp() throws Exception
    {
        resultset = Mockito.mock( ResultSet.class );
        sqlEx = new SQLException( "testing" );
        
        ResultSetMapper.LogHolder logHolder = new LogHolder();
        assertNotNull( logHolder );
    }


    
    
    
    @Test
    void test_getBigDecimalFromResultSet() throws SQLException
    {
        BigDecimal bgValue = new BigDecimal( "12.45" );
        doReturn( bgValue ).when( resultset ).getBigDecimal( anyString() );
        BigDecimal result = ResultSetMapper.getBigDecimalFromResultSet( resultset, "12.34" );
        log.info(" result {}", result.toString() );
        assertEquals( bgValue, result );
        
        doThrow( sqlEx ).when( resultset ).getBigDecimal( anyString() );
        result = ResultSetMapper.getBigDecimalFromResultSet( resultset, "12.34" );
        log.info( "result : {}", result );
        assertNull( result );
    }
    
    
    @Test
    void test_getDateFromResultSet() throws SQLException
    {
        Date today = new Date();
        java.sql.Date dateValue = new java.sql.Date( today.getTime() );
        doReturn( dateValue ).when( resultset ).getDate( anyString() );
        
        Date result = ResultSetMapper.getDateFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( today, result );
        
        doReturn( null ).when( resultset ).getDate( anyString() );
        result = ResultSetMapper.getDateFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertNull( result );
        
        doThrow( sqlEx ).when( resultset ).getDate( anyString() );
        result = ResultSetMapper.getDateFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
        
    }
    

    
    @Test
    void test_getBooleanFromResultSet_Y() throws SQLException
    {
        doReturn( "Y" ).when( resultset ).getString( anyString() );
        Boolean result = ResultSetMapper.getBooleanFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( Boolean.TRUE, result );
        
        
        doThrow( sqlEx ).when( resultset ).getString( anyString() );
        result = ResultSetMapper.getBooleanFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
    }
    
    
    @Test
    void test_getBooleanFromResultSet_N() throws SQLException
    {
        doReturn( "N" ).when( resultset ).getString( anyString() );
        Boolean result = ResultSetMapper.getBooleanFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( Boolean.FALSE, result );
    }
    
    
    @Test
    void test_getDoubleFromResultSet() throws SQLException
    {
        doReturn( 123.4d ).when( resultset ).getDouble( anyString() );
        Double result = ResultSetMapper.getDoubleFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( 123.4d, result );
        
        doThrow( sqlEx ).when( resultset ).getDouble( anyString() );
        result = ResultSetMapper.getDoubleFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
    }
    

    @Test
    void test_getFloatFromResultSet() throws SQLException
    {
        doReturn( 123.4f ).when( resultset ).getFloat( anyString() );
        Float result = ResultSetMapper.getFloatFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( 123.4f, result );
        
        doThrow( sqlEx ).when( resultset ).getFloat( anyString() );
        result = ResultSetMapper.getFloatFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
        
    }
    
    
    @Test
    void test_getIntegerFromResultSet() throws SQLException
    {
        doReturn( 123 ).when( resultset ).getInt( anyString() );
        Integer result = ResultSetMapper.getIntegerFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( 123, result );
        
        doThrow( sqlEx ).when( resultset ).getInt( anyString() );
        result = ResultSetMapper.getIntegerFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
    }
    
    
    @Test
    void test_getLongFromResultSet() throws SQLException
    {
        doReturn( 123L ).when( resultset ).getLong( anyString() );
        Long result = ResultSetMapper.getLongFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( 123L, result );
        
        doThrow( sqlEx ).when( resultset ).getLong( anyString() );
        result = ResultSetMapper.getLongFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    void test_getObjectFromResultSet() throws SQLException
    {
        doReturn( "test" ).when( resultset ).getObject( anyString(), any(Class.class ) );
        Object result = ResultSetMapper.getObjectFromResultSet( resultset, "", String.class );
        log.info(" result {}", result );
        assertEquals( "test", result.toString() );
        
        doThrow( sqlEx ).when( resultset ).getObject( anyString(), any(Class.class ) );
        result = ResultSetMapper.getObjectFromResultSet( resultset, "", String.class );
        log.info( "result : {}", result );
        assertNull( result );
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    void test_getOffsetDateTimeFromResultSet() throws SQLException
    {
        OffsetDateTime today = OffsetDateTime.now();
        doReturn( today, (Object)null ).when( resultset ).getObject( anyString(), any( Class.class ) );
        OffsetDateTime result = ResultSetMapper.getOffsetDateTimeFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertEquals( today.getNano(), result.getNano() );
        
        result = ResultSetMapper.getOffsetDateTimeFromResultSet( resultset, "" );
        log.info(" result {}", result );
        assertNull( result );
        
        doThrow( sqlEx ).when( resultset ).getObject( anyString(), any(Class.class ) );
        result = ResultSetMapper.getOffsetDateTimeFromResultSet( resultset, "" );
        log.info( "result : {}", result );
        assertNull( result );
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    void test_getOffsetDateTimeFromResultSet_zoneid() throws SQLException
    {
        OffsetDateTime today = OffsetDateTime.now();
        doReturn( today ).when( resultset ).getObject( anyString(), any( Class.class ) );
        OffsetDateTime result = ResultSetMapper.getOffsetDateTimeFromResultSet( resultset, "", "CST6CDT" );
        
        log.info(" today {} {}", today, today.toZonedDateTime().getZone().getId() );
        log.info(" result {} {}", result, result.toZonedDateTime().getZone().getId() );
        assertEquals( today.getNano(), result.getNano() );
    }
    
    
    @Test
    void getOffsetDateTimeFromTime() throws SQLException
    {
        OffsetDateTime result = ResultSetMapper.getOffsetDateTimeFromTime( 0, "UTC" );
        log.info(" result {} {}", result, result.toZonedDateTime().getZone().getId() );
        assertEquals( 1970, result.getYear() );
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    void test_getOffsetDateTimeTruncatedFromResultSet() throws SQLException
    {
        OffsetDateTime today = OffsetDateTime.now();
        doReturn( today, (Object)null ).when( resultset ).getObject( anyString(), any( Class.class ) );
        OffsetDateTime result = ResultSetMapper.getOffsetDateTimeTruncatedFromResultSet( resultset, "");
        
        log.info(" today {} {}", today, today.toZonedDateTime().getZone().getId() );
        log.info(" result {} {}", result, result.toZonedDateTime().getZone().getId() );
        assertEquals( 0, result.getNano() );
        
        result = ResultSetMapper.getOffsetDateTimeTruncatedFromResultSet( resultset, "");
        log.info(" result {}", result );
        assertNull( result );
        
        doThrow( sqlEx ).when( resultset ).getObject( anyString(), any(Class.class ) );
        result = ResultSetMapper.getOffsetDateTimeTruncatedFromResultSet( resultset, "");
        log.info( "result : {}", result );
        assertNull( result );

    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    void test_getOffsetDateTimeTruncatedFromResultSet_zoneid() throws SQLException
    {
        OffsetDateTime today = OffsetDateTime.now();
        doReturn( today ).when( resultset ).getObject( anyString(), any( Class.class ) );
        OffsetDateTime result = ResultSetMapper.getOffsetDateTimeTruncatedFromResultSet( resultset, "", "CST6CDT" );
        
        log.info(" today {} {}", today, today.toZonedDateTime().getZone().getId() );
        log.info(" result {} {}", result, result.toZonedDateTime().getZone().getId() );
        assertEquals( 0, result.getNano() );
    }
    
    @Test
    void test_getStringFromResultSet() throws SQLException
    {
        doReturn( " abcd ", (Object)null ).when( resultset ).getString( anyString() );
        String result = ResultSetMapper.getStringFromResultSet( resultset, "" );
        log.info(" result [{}]", result );
        assertEquals( " abcd " , result );
        
        result = ResultSetMapper.getStringFromResultSet( resultset, "" );
        log.info(" result [{}]", result );
        assertNull( result );
    }
    
 
    @Test
    void test_getStringFromResultSet_trim() throws SQLException
    {
        doReturn( " abcd ", " defg " ).when( resultset ).getString( anyString() );
        String result = ResultSetMapper.getStringFromResultSet( resultset, "", false );
        log.info(" result [{}]", result );
        assertEquals( " abcd " , result );
        result = ResultSetMapper.getStringFromResultSet( resultset, "", true );
        log.info(" result [{}]", result );
        assertEquals( "defg" , result );
    }
    


}
