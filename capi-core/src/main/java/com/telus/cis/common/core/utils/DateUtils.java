package com.telus.cis.common.core.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public class DateUtils {

	/**
	 * Compare two dates regardless to the time portion
	 * @param srcDate
	 * @param refDate
	 * @return true if both dates are on the same day
	 */
	public static boolean isSameDay (Date srcDate, Date refDate) {
		Calendar date1 = Calendar.getInstance();
		Calendar date2 = Calendar.getInstance();

		date1.setTime(srcDate);
		date2.setTime(refDate);
		clearTimePortion(date1);
		clearTimePortion(date2);
		return date1.getTimeInMillis() == date2.getTimeInMillis();
	}

	/**
	 * Compare two dates regardless of the time portion.
	 * @param srcDate
	 * @param refDate
	 * @return true if srcDate is before refDate
	 */
	public static boolean isBefore(Date srcDate, Date refDate) {
		Calendar date1 = Calendar.getInstance();
		Calendar date2 = Calendar.getInstance();

		date1.setTime(srcDate);
		date2.setTime(refDate);
		clearTimePortion(date1);
		clearTimePortion(date2);

		return date1.getTimeInMillis() < date2.getTimeInMillis();
	}

	/**
	 * Compare two dates regardless of the time portion.
	 * @param srcDate
	 * @param refDate
	 * @return true if srcDate is after refDate
	 */
	public static boolean isAfter(Date srcDate, Date refDate) {
		Calendar date1 = Calendar.getInstance();
		Calendar date2 = Calendar.getInstance();

		date1.setTime(srcDate);
		date2.setTime(refDate);
		clearTimePortion(date1);
		clearTimePortion(date2);

		return date1.getTimeInMillis() > date2.getTimeInMillis();
	}

	public static void clearTimePortion(Calendar date) {
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
	}

	public static Date clearTimePortion(Date date) {
		if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        clearTimePortion(c);
        return c.getTime();
	}

	public static long calendarToTimezoneDate(Calendar srcDateTime, String timeZoneId) {
		if(srcDateTime == null) {
			srcDateTime = calendarToTimezone(Calendar.getInstance(), timeZoneId);
			return srcDateTime.getTimeInMillis();
		} else {
			TimeZone requestedZone = TimeZone.getTimeZone(timeZoneId);
			long utcTimeInMillis = srcDateTime.getTimeInMillis() - srcDateTime.get(Calendar.ZONE_OFFSET);
			long requestedTimeInMillis = utcTimeInMillis + requestedZone.getRawOffset();
			return requestedTimeInMillis;
		}
	}

	public static Calendar calendarToTimezone(Calendar srcDateTime, String timeZoneId) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
		if(srcDateTime == null) {
			return cal;
		}
		cal.setTimeInMillis(srcDateTime.getTimeInMillis());
		return cal;
	}

	public static Calendar dateToTimezoneCalendar(Date srcDate, String timeZoneId) {
		Calendar cal = Calendar.getInstance();
		if(srcDate != null) {
			cal.setTimeInMillis(srcDate.getTime());
		}
		return calendarToTimezone(cal, timeZoneId);
	}

	public static Date calculateBillCycleCloseDate(Date transactionDate, int billCycleCloseDay) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(transactionDate);
		clearTimePortion(calendar);

		// Update the billCycleCloseDay if it's above the maximum # of days in month
		int maxDay = calendar.getActualMaximum(Calendar.DATE);
		if (billCycleCloseDay > maxDay) {
			billCycleCloseDay = maxDay;
		}

		// Set the date to the bill close day and increment the month if the transaction date is after this date (next bill cycle)
		int actualDay = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, billCycleCloseDay);
		if (actualDay > billCycleCloseDay) {
			calendar.add(Calendar.MONTH, 1);
		}

		return calendar.getTime();
	}

	public static OffsetDateTime mapToOffsetDateTime(LocalDate date, LocalTime time, ZoneOffset zoneOffset) {
		return Optional.ofNullable(date).map(ld -> OffsetDateTime.of(date, time, zoneOffset))
				.orElse(null);
	}

	public static OffsetDateTime normalizeOffsetDateTimeFromLocalDate(LocalDate date) {
		// normalize the resulting java.time.OffsetDateTime conversion from the java.time.LocalDate object by setting
		// time component to NOON
		// this is required in cases where the Amdocs' java.util.Date logic does not handle time zone offsets and
		// simply truncates the date portion when saving to the database or comparing to logical date, while our
		// java.time.OffsetDateTime logic must apply a timezone offset
		// for example, OffsetDateTime '2022-05-12T00:00:00Z' (UTC) will be converted to a legacy Date '2022-05-11'
		// with local timezone offset of -4:00 (EST), but only the date portion is saved to the database; when
		// retrieved subsequently as OffsetDateTime (UTC) this will become '2022-05-11T00:00:00Z'
		return mapToOffsetDateTime(date, LocalTime.NOON, ZoneOffset.UTC);
	}

}