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

package com.telus.cis.common.core.aspectj;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Configuration
public class TrackTimeAspect
{

    @Autowired
    private ObjectMapper objectMapper;



    @Pointcut("@annotation( TrackTime )")
    public void trackTimePointCutDefinition()
    {
        // do nothing
    }


    @Around("trackTimePointCutDefinition() &&  @annotation(trackTime)")
    public Object trackTimeAround(ProceedingJoinPoint joinPoint, TrackTime trackTime) throws Throwable
    {
        if ( !trackTime.enabled() ) {
            return joinPoint.proceed();
        }

        Object returnObject;
        Signature signature = joinPoint.getSignature();
        long startTime = System.nanoTime();
        try {
            if ( log.isTraceEnabled() ) {
                Class<?> returnType = ((MethodSignature) signature).getReturnType();
                log.trace( "Entering... {} with return type {}", signature.getDeclaringTypeName(), returnType.getName() );
            }
            returnObject = joinPoint.proceed();

        }
        catch ( Throwable throwable ) {
            log.info( "Track Time Exception: {}", throwable.getMessage() );
            log.trace( "trackTime Exception", throwable );
            throw throwable;
        }
        finally {
            long timeTaken = System.nanoTime() - startTime;
            Long timeInSecond = timeTaken / 1000000;
            log.info( "{}::{}() time: {}:{} ", signature.getDeclaringType().getSimpleName(), signature.getName(),
                    timeInSecond.doubleValue() / 1000.0, timeTaken % 1000000 );

            if ( trackTime.parameters() ) {
                Object[] args = joinPoint.getArgs();
                log.debug( "{} with {} arguments(s) {}", signature.getName(), args.length,
                        Arrays.toString( ((CodeSignature) signature).getParameterNames() ) );
                for ( int i = 0; i < args.length; i++ ) {
                    String argumentStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString( args[i] );
                    log.debug( ">>> Argument [{}] = {}", i, argumentStr );
                }
            }
            if ( log.isTraceEnabled() ) {
                log.trace( "Exit from : {}", signature.getDeclaringTypeName() );
            }
        }

        return returnObject;
    }

}