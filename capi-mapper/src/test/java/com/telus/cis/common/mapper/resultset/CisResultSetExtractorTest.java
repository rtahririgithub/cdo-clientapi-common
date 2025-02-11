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

import static com.telus.cis.common.mapper.resultset.ResultSetMapper.getStringFromResultSet;
import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import lombok.extern.slf4j.Slf4j;


@Slf4j
class CisResultSetExtractorTest
{

    class DefaultExtractor implements CisResultSetExtractor {
        
    }
    
    private ResultSet resultset;
    private DefaultExtractor extractor;
    private SQLException sqlEx;
    
    @BeforeEach
    void setUp() throws Exception
    {
        resultset = Mockito.mock( ResultSet.class );
        extractor = new DefaultExtractor();
        sqlEx = new SQLException( "testing" );
    }

    
    
    

    @Test
    void testResultSetExtractorRFunctionOfRU() throws SQLException
    {
        doReturn(true, false).when(resultset).next();
        doReturn( "test_data" ).when( resultset ).getString( anyString() );
        String result = extractor.cisResultSetExtractor( resultset, rs -> getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( "test_data", result );
        
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, rs -> getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertNull( result );
    
        doReturn( false ).when(resultset).next();
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, (rs) -> getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertNull( result );
    }

    
    @Test
    void testCisResultSetExtractorRUIntBiFunctionOfRUS() throws SQLException
    {
        
        doReturn(true, true, false).when(resultset).next();
        doReturn( "1", "-2" ).when( resultset ).getString( anyString() );
        String result = extractor.cisResultSetExtractor( resultset, (rs, s) -> ( isNull(s) ? "" : s ) + getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( "1-2", result );
        
        
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, null, (rs, s) -> s + getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertNull( result );
        
        doReturn(false).when(resultset).next();
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, null, (rs, s) -> s + getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertNull( result );
    }
    

    @Test
    void testCisResultSetExtractorRIntFunctionOfRU() throws SQLException
    {
        doReturn(true, true, true, false).when(resultset).next();
        doReturn( "1", null, "2" ).when( resultset ).getString( anyString() );
        List<String> result = extractor.cisResultSetExtractor( resultset, 0, rs -> getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( 2, result.size() );
        
        doReturn(true, true, false).when(resultset).next();
        doReturn( "1", "2" ).when( resultset ).getString( anyString() );
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, 0, rs -> getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
        
        doReturn(false).when(resultset).next();
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, 0, rs -> getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
    }


    @Test
    void testCisResultSetExtractorRIntBiConsumerOfRMapOfUS() throws SQLException
    {
        doReturn(true, true, false).when(resultset).next();
        doReturn( "key1", "value1", "key2", "value2" ).when( resultset ).getString( anyString() );
        Map<String, String> result = extractor.cisResultSetExtractor( resultset, 0, (rs, m) ->  m.put(getStringFromResultSet( rs, "" ), getStringFromResultSet( rs, "" ) ) );
        log.info( "result {}", result );
        assertEquals( 2, result.size() );
        assertEquals( "value1", result.get("key1") );
        assertEquals( "value2", result.get("key2") );
        
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, 0, (rs, m) ->  m.put(getStringFromResultSet( rs, "" ), getStringFromResultSet( rs, "" ) ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
        
        doReturn(false).when(resultset).next();
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, 0, (rs, m) ->  m.put(getStringFromResultSet( rs, "" ), getStringFromResultSet( rs, "" ) ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
        
    }


    @Test
    void testResultSetExtractorRBiFunctionOfRUU() throws SQLException
    {
        String userData = "test";
        doReturn(true, true, true, false).when(resultset).next();
        doReturn( "-1", null, "-2" ).when( resultset ).getString( anyString() );
        List<String> result = extractor.cisResultSetExtractor( resultset, userData, 0, (rs, u) -> {
            String data = getStringFromResultSet( rs, "" );
            return isNull( data ) ? null : u + data;
        } );
        log.info( "result {}", result );
        assertEquals( 2, result.size() );
        assertEquals( "test-1", result.get(0) );
        assertEquals( "test-2", result.get(1) );
        
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, userData, 0, (rs, u) -> u + getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
        
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, userData, 0, (rs, u) -> u + getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
    }


    @Test
    void testCisResultSetExtractorRUIntServiceConsumerOfRUMapOfSV() throws SQLException
    {
        String userData = "test";
        doReturn(true, true, false).when(resultset).next();
        doReturn( "key1", "-value1", "key2", "-value2" ).when( resultset ).getString( anyString() );
        
        Map<String, String> result = extractor.cisResultSetExtractor( resultset, userData, 0, (rs, u, m) ->  m.put(getStringFromResultSet( rs, "" ), u + getStringFromResultSet( rs, "" ) ) );
        log.info( "result {}", result );
        assertEquals( 2, result.size() );
        assertEquals( "test-value1", result.get("key1") );
        assertEquals( "test-value2", result.get("key2") );
        
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, userData, 0, (rs, u, m) ->  m.put(getStringFromResultSet( rs, "" ), u + getStringFromResultSet( rs, "" ) ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
        
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, userData, 0, (rs, u, m) ->  m.put(getStringFromResultSet( rs, "" ), u + getStringFromResultSet( rs, "" ) ) );
        log.info( "result {}", result );
        assertEquals( 0, result.size() );
    }


    @Test
    void testCisResultSetExtractorRUVServiceFunctionOfRUVV() throws SQLException
    {
        String input = "test";
        String userData = "user";
        doReturn(true, false).when(resultset).next();
        doReturn( "result" ).when( resultset ).getString( anyString() );
        String result = extractor.cisResultSetExtractor( resultset, userData, input, (rs, u, v) ->  u + " " + v +" " +getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( "user test result", result );
        
        
        doThrow( sqlEx ).when( resultset ).next();
        result = extractor.cisResultSetExtractor( resultset, userData, input, (rs, u, v) ->  u + " " + v +" " +getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( "test", result );  // result input when exception
        
        
        doReturn(false).when(resultset).next();
        doThrow( sqlEx ).when( resultset ).close();
        result = extractor.cisResultSetExtractor( resultset, userData, input, (rs, u, v) ->  u + " " + v +" " +getStringFromResultSet( rs, "" ) );
        log.info( "result {}", result );
        assertEquals( "test", result );  // result input when exception
    }

}
