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


import static com.telus.cis.common.mapper.resultset.ResultSetMapper.handleResultSetCloseException;
import static com.telus.cis.common.mapper.resultset.ResultSetMapper.handleResultSetException;
import static java.util.Objects.nonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.telus.cis.common.mapper.domain.ServiceConsumer;
import com.telus.cis.common.mapper.domain.ServiceFunction;

public interface CisResultSetExtractor
{
    /**
     * Result set extractor to map ResultSet data to single Object in U
     * U is expected to be initialized in BiFunction lambda function
     */
    public default <R extends ResultSet, U> U cisResultSetExtractor(R rs, Function<R, U> function)
    {
        U data = null;
        try {
            if ( rs.next() ) {
                return function.apply( rs );
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
            return data;
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return data;
    }


    public default <R extends ResultSet, U> List<U> cisResultSetExtractor(R rs, int fetchSize, Function<R, U> function)
    {
        List<U> resultList = new ArrayList<>();
        try {
            rs.setFetchSize( fetchSize );
            while ( rs.next() ) {
                U item = function.apply( rs );
                if ( nonNull( item ) ) {
                    resultList.add( item );
                }
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return resultList;
    }


    public default <R extends ResultSet, U, S> Map<U, S> cisResultSetExtractor(R rs, int fetchSize, BiConsumer<R, Map<U, S>> consumer)
    {
        Map<U, S> resultMap = new HashMap<>();
        try {
            rs.setFetchSize( fetchSize );
            while ( rs.next() ) {
                consumer.accept( rs, resultMap );
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return resultMap;
    }

    
    /**
     * Result set extractor to map result-set data to single Object in U
     * U is expected to be initialized in BiFunction lambda function
     * There may be multiple values in ResultRet, use biFunction to manage the multiple value handling
     */
    public default <R extends ResultSet, U> U cisResultSetExtractor(R rs, U processData, BiFunction<R, U, U> function)
    {
        try {
            while ( rs.next() ) {
                processData = function.apply( rs, processData );
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
            return null;
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return processData;
    }

    
    public default <R extends ResultSet, U> U cisResultSetExtractor(R rs, BiFunction<R, U, U> function)
    {
        return cisResultSetExtractor( rs, null, function );
    }


    public default <R extends ResultSet, U, S> List<S> cisResultSetExtractor(R rs, U userData, int fetchSize, BiFunction<R, U, S> function)
    {
        List<S> resultList = new ArrayList<>();
        try {
            rs.setFetchSize( fetchSize );
            while ( rs.next() ) {
                S item = function.apply( rs, userData );
                if ( nonNull( item ) ) {
                    resultList.add( item );
                }
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return resultList;
    }


    /**
     * Operation to get ResultSet data to Map<S, V>
     *  
     * @param <R> - extends from result set
     * @param <U> - user meta data
     * @param <S> - Map Key type
     * @param <V> - Map Value type
     * @param rs ResultSet
     * @param serviceData - user meta data
     * @param fetchSize - size of collection in request
     * @param consumer - Lambda @InterfaceFunction that take 3 parameters; (locally defined)
     * @return   
     *   Map <S, V> of query data
     */
    public default <R extends ResultSet, U, S, V> Map<S, V> cisResultSetExtractor(R rs, U serviceData, int fetchSize,
            ServiceConsumer<R, U, Map<S, V>> consumer)
    {
        Map<S, V> resultMap = new HashMap<>();
        try {
            rs.setFetchSize( fetchSize );
            while ( rs.next() ) {
                consumer.accept( rs, serviceData, resultMap );
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return resultMap;
    }


    /**
     * return result from tri-function on the first value; take rs and processed in rsFunction.apply.
     *  
     * @param <R> ResultSet
     * @param <U> user meta data
     * @param <V> result type
     * @param rs
     * @param serviceData
     * @param input - input value for tri-function
     * @param rsFunction - Lambda tri-function to take resultSet, metaData, user supplied data in type V
     * @return   
     *   <n.b. for "@param" above <add a description after the field name>
     *   <n.b. for "@return" above <add the return_Type & the description>
     *   <n.b. for "@throws" (i.e. @exception) above, add "If" & description of when it happens>
     */
    public default <R extends ResultSet, U, V> V cisResultSetExtractor(R rs, U serviceData, V input, ServiceFunction<R, U, V, V> rsFunction)
    {
        try {
            while ( rs.next() ) {
                return rsFunction.apply( rs, serviceData, input );
            }
        }
        catch ( SQLException e ) {
            handleResultSetException( e );
        }
        finally {
            try {
                rs.close();
            }
            catch ( SQLException ex ) {
                handleResultSetCloseException( ex );
            }
        }
        return input;
    }


}
