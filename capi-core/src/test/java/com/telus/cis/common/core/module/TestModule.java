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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class TestModule
{
    Date                dateVal;
    OffsetDateTime      dateTime;
    Integer             intVal1;
    
    Integer             intVal2;
    String              stringVal;
    Double              doubleVal;
    BigDecimal          bdVal;
    Boolean             bVal;
    TestEnum            enumVal;
    List<EntityModule>  entityList;
    TimePeriod          validFor;
    
    // public TestModule() {}
    
}
