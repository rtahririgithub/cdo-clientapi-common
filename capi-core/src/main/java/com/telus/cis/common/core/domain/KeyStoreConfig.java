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

import lombok.Data;

@Data
public class KeyStoreConfig
{
    private String keyStoreType;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
    private String keyTransformation;
}
