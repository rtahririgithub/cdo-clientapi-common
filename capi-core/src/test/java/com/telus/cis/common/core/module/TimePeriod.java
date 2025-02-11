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

package com.telus.cis.common.core.module;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class TimePeriod
{
    private OffsetDateTime startDate;
    private OffsetDateTime endData;
}
