/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.core.module;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.springframework.validation.annotation.Validated;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Validated
public class EntityModule implements Serializable
{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    @NotNull( message = "id is null" )
    String id;
    
    Boolean booleanValue;
    

    String stringVal;
    
    OffsetDateTime dateVal;
    
    BigDecimal bigDecimal;
    
    @Positive( message = " invalid value '${validatedValue}'" )
    @NotNull( message = "numberVal is null" )
    private Number numberVal1;
    
    private Number numberVal2;
    
    public EntityModule() {}
    
}
