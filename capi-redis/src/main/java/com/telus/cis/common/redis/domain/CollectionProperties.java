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

package com.telus.cis.common.redis.domain;

import lombok.Data;

@Data
public class CollectionProperties
{
    private String host;
    private String password;
    private String url;
    private int port;
    private int maxIdle;
    private int minIdle;
    private int maxTotal;
    private Long timeout;
}
