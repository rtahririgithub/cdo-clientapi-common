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

package com.telus.cis.common.core.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = { "knowbilityCredential" })
public class ClientIdentity implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String applicationCode;
    private String knowbilityUserId;
    private String knowbilityCredential;

}
