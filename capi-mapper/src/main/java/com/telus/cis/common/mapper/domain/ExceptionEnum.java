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

package com.telus.cis.common.mapper.domain;

public enum ExceptionEnum {
    SQL_EXCEPTION ("SQL Exception"),
    SQL_EXCEPTION_1 ("SQL Exception {}"),
    SQL_EXCEPTION_LABEL_1 ("SQL Exception on {} : {}"),
    SQL_EXCEPTION_CLOSE ("resultset close error {}");
    
    private String text;
    
    ExceptionEnum(String text)
    {
        this.text = text;
    }
    
    public String text() {
        return text;
    }
}
