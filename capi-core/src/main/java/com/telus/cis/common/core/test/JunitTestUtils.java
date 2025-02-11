/*
 *  Copyright (c) 2021 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Set;
import java.util.TimeZone;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import com.telus.cis.common.core.exception.ValidationException;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


@Slf4j @UtilityClass
public class JunitTestUtils
{
    private static final String TEST_RESOURCE_PATH = "src/test/resources";



    public static ObjectMapper getObjectMapper()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssXXX" );
        sdf.setTimeZone( TimeZone.getTimeZone( "Canada/Eastern" ) );
        return Jackson2ObjectMapperBuilder.json()
                .modules( new JavaTimeModule() ).featuresToDisable( // SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        SerializationFeature.FAIL_ON_EMPTY_BEANS,
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE )
                .dateFormat( sdf ).build()
                .setSerializationInclusion( Include.NON_NULL );
    }

    
    public static ObjectMapper getObjectMapper(Mode jsonCreatorMode)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssXXX" );
        sdf.setTimeZone( TimeZone.getTimeZone( "Canada/Eastern" ) );
        return Jackson2ObjectMapperBuilder.json()
                .modules( new JavaTimeModule() ).featuresToDisable( // SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        SerializationFeature.FAIL_ON_EMPTY_BEANS,
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE )
                .dateFormat( sdf ).build()
                .setSerializationInclusion( Include.NON_NULL )
                .registerModule( new ParameterNamesModule(
                        jsonCreatorMode ) );
    }

    public static <T> void setTestDataToFile(ObjectMapper objectMapper,
            T object, String path)
    {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File( TEST_RESOURCE_PATH + path ),
                    //                    new File( tester.getResource( path ).getFile() ),
                    object );
        }
        catch ( IOException e ) {
            log.info( "write Json file error : {}", e.getLocalizedMessage() );
        }
    }


    public static <T> T getTestDataFromFile(ObjectMapper objectMapper, String path, Class<T> clazz)
            throws IOException
    {
        return objectMapper.readValue( new File( TEST_RESOURCE_PATH + path ), clazz );
    }


    /**
     * example List<WLSProduct> productList = getTestCollectionFromFile( objectMapper, "/json/it01/testList.json", new TypeReference<List<WLSProduct>>() {} );
     */
    public static <S, T extends Collection<S>> T getTestDataFromFile(
            ObjectMapper objectMapper, String path,
            TypeReference<T> typeReference)
            throws IOException
    {
        return objectMapper.readValue( new File( TEST_RESOURCE_PATH + path ), typeReference );
    }


}
