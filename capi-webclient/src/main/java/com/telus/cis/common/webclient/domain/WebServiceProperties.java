/*
 *  Copyright (c) 2023 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */

package com.telus.cis.common.webclient.domain;

import lombok.Data;

@Data
public class WebServiceProperties
{
    private String serviceUri;
    private String resourceId;
    private String userName;
    private String password;
}
