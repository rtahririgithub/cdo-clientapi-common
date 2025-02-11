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

import static com.telus.cis.common.mapper.domain.ExceptionEnum.SQL_EXCEPTION;
import static com.telus.cis.common.mapper.domain.ExceptionEnum.SQL_EXCEPTION_1;
import static com.telus.cis.common.mapper.domain.ExceptionEnum.SQL_EXCEPTION_CLOSE;
import static com.telus.cis.common.mapper.domain.ExceptionEnum.SQL_EXCEPTION_LABEL_1;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * define getXXXFromResultSet
 */
public interface ResultSetMapper {
	
	public static final String UTC_VALUE = "UTC";

	@Slf4j
	final class LogHolder {	}

	public static void handleResultSetCloseException(SQLException ex) {
		LogHolder.log.warn(SQL_EXCEPTION_CLOSE.text(), ex.getLocalizedMessage());
		LogHolder.log.trace(SQL_EXCEPTION.text(), ex);
	}

	public static void handleResultSetException(SQLException e) {
		LogHolder.log.warn(SQL_EXCEPTION_1.text(), e.getLocalizedMessage());
		LogHolder.log.trace(SQL_EXCEPTION.text(), e);
	}

	public static void handleResultSetExceptionWithLabel(String label, SQLException e) {
		LogHolder.log.warn(SQL_EXCEPTION_LABEL_1.text(), label, e.getLocalizedMessage());
		LogHolder.log.trace(SQL_EXCEPTION.text(), e);
	}

	public static String getStringFromResultSet(ResultSet rs, String label) {
		return getStringFromResultSet(rs, label, false);
	}

	public static String getStringFromResultSet(ResultSet rs, String label, Boolean trim) {
		
		try {
			String result = rs.getString(label);
			if ((result != null) && (Boolean.TRUE.equals(trim))) {
				return result.trim();
			}
			return result;
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static BigDecimal getBigDecimalFromResultSet(ResultSet rs, String label) {
		
		try {
			return rs.getBigDecimal(label);
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static Long getLongFromResultSet(ResultSet rs, String label) {
		
		try {
			return rs.getLong(label);
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static Float getFloatFromResultSet(ResultSet rs, String label) {
		
		try {
			return rs.getFloat(label);
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static Double getDoubleFromResultSet(ResultSet rs, String label) {
		
		try {
			return rs.getDouble(label);
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getObjectFromResultSet(ResultSet rs, String label, Class<?> classType) {
		
		try {
			return (T) rs.getObject(label, classType);
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static Integer getIntegerFromResultSet(ResultSet rs, String label) {
		
		try {
			return rs.getInt(label);
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static Date getDateFromResultSet(ResultSet rs, String label) {
		
		try {
			java.sql.Date sqlDate = rs.getDate(label);
			return sqlDate == null ? null : new Date(sqlDate.getTime());
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static OffsetDateTime getOffsetDateTimeTruncatedFromResultSet(ResultSet rs, String label) {
		return getOffsetDateTimeTruncatedFromResultSet(rs, label, UTC_VALUE);
	}

	/**
	 * This method originally used java.sql.Date, ostensibly to return a date object with no time or time set to 00:00 GMT (see getDateFromResultSet above). This
	 * semantic is being preserved by truncating the OffsetDateTime result to days.
	 */
	public static OffsetDateTime getOffsetDateTimeTruncatedFromResultSet(ResultSet rs, String label, String zoneId) {
		
		try {
			OffsetDateTime odt = rs.getObject(label, OffsetDateTime.class);
			return odt == null ? null : OffsetDateTime.ofInstant(odt.truncatedTo(ChronoUnit.DAYS).toInstant(), ZoneId.of(zoneId));
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static OffsetDateTime getOffsetDateTimeFromResultSet(ResultSet rs, String label) {
		return getOffsetDateTimeFromResultSet(rs, label, UTC_VALUE);
	}

	public static OffsetDateTime getOffsetDateTimeFromResultSet(ResultSet rs, String label, String zoneId) {
		
		try {
			OffsetDateTime odt = rs.getObject(label, OffsetDateTime.class);
			return odt == null ? null : OffsetDateTime.ofInstant(odt.toInstant(), ZoneId.of(zoneId));
		} catch (SQLException e) {
			handleResultSetExceptionWithLabel(label, e);
		}
		
		return null;
	}

	public static OffsetDateTime getOffsetDateTimeFromTime(long time, String zoneId) {
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of(zoneId));
	}

	public static Boolean getBooleanFromResultSet(ResultSet rs, String label) {
		
		Set<String> positiveSet = new HashSet<>(Arrays.asList("Y", "y"));
		String result = getStringFromResultSet(rs, label);
		
		return result == null ? null : positiveSet.contains(result);
	}

}