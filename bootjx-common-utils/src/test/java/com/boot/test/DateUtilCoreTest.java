package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.DateUtil;

/**
 * Pins down the core, deterministic behaviors of {@link DateUtil}. Several of
 * the methods covered here are explicitly flagged in the source as "Critical:
 * Used By PROBOT ... DO NOT CHANGE" - this suite exists precisely so any
 * accidental behavior change (e.g. from a JDK time API upgrade) is caught.
 */
@RunWith(SpringRunner.class)
public class DateUtilCoreTest {

	@Test
	public void parseDate_defaultFormat_parsesDDMMYYYY() {
		Date date = DateUtil.parseDate("25/12/2023");
		String formatted = DateUtil.formatDate(date, "dd/MM/yyyy");
		assertEquals("25/12/2023", formatted);
	}

	@Test
	public void parseDate_invalidString_fallsBackToEpochMillisOrNull() {
		assertNull(DateUtil.parseDate("not-a-date"));
	}

	@Test
	public void parseDate_withCustomFormat_usesGivenPattern() {
		Date date = DateUtil.parseDate("2023-12-25", "yyyy-MM-dd");
		assertEquals("2023-12-25", DateUtil.formatDate(date, "yyyy-MM-dd"));
	}

	@Test
	public void formatDate_variants_produceExpectedPatterns() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(2023, Calendar.MARCH, 5, 12, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date = cal.getTime();

		assertEquals("05-Mar-23", DateUtil.formatDateDDMMMYY(date));
		assertEquals("2023-03-05", DateUtil.formatDateYYYYMMDD(date));
	}

	@Test
	public void forwardAndBackwardDate_shiftByGivenMillis() {
		Date date = new Date(1_000_000L);
		assertEquals(1_001_000L, DateUtil.forwardDate(date, 1000).getTime());
		assertEquals(999_000L, DateUtil.backwardDate(date, 1000).getTime());
	}

	@Test
	public void getDiffInDays_computesWholeDaysBetweenTimestamps() {
		long day1 = 0L;
		long day3 = 2 * DateUtil.ONEDAY;
		assertEquals(2L, DateUtil.getDiffInDays(day1, day3));
	}

	@Test
	public void forwardTsByDays_addsWholeDaysInMillis() {
		long ts = 0L;
		assertEquals(3 * DateUtil.ONEDAY, DateUtil.forwardTsByDays(ts, 3));
	}

	@Test
	public void getDateStart_and_getDateEnd_boundTheGmtDay() {
		long midDay = DateUtil.ONEDAY * 100 + (12L * 60 * 60 * 1000); // some day at noon GMT
		long start = DateUtil.getDateStart(midDay);
		long end = DateUtil.getDateEnd(midDay);

		Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		startCal.setTimeInMillis(start);
		assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, startCal.get(Calendar.MINUTE));
		assertEquals(0, startCal.get(Calendar.SECOND));

		Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		endCal.setTimeInMillis(end);
		assertEquals(23, endCal.get(Calendar.HOUR_OF_DAY));
		assertEquals(59, endCal.get(Calendar.MINUTE));
		assertEquals(59, endCal.get(Calendar.SECOND));

		assertTrue(start < midDay && midDay < end);
	}

	@Test
	public void removeTime_zeroesOutTimeFieldsKeepingDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(2023, Calendar.JUNE, 15, 14, 30, 45);
		Date withTime = cal.getTime();

		Date withoutTime = DateUtil.removeTime(withTime);
		Calendar result = Calendar.getInstance();
		result.setTime(withoutTime);
		assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, result.get(Calendar.MINUTE));
		assertEquals(0, result.get(Calendar.SECOND));
		assertEquals(2023, result.get(Calendar.YEAR));
		assertEquals(Calendar.JUNE, result.get(Calendar.MONTH));
		assertEquals(15, result.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void asDate_asLocalDate_asLocalDateTime_roundTripThroughUtc() {
		LocalDate localDate = LocalDate.of(2023, 7, 4);
		Date date = DateUtil.asDate(localDate);
		assertEquals(localDate, DateUtil.asLocalDate(date));

		LocalDateTime localDateTime = LocalDateTime.of(2023, 7, 4, 10, 15, 30);
		Date dateTime = DateUtil.asDate(localDateTime);
		assertEquals(localDateTime, DateUtil.asLocalDateTime(dateTime));
	}

	@Test
	public void getNextZonedDay_advancesOneDayAndResetsTimeToMidnight() {
		ZonedDateTime from = ZonedDateTime.of(2023, 1, 31, 15, 45, 30, 0, ZoneId.of("UTC"));
		ZonedDateTime next = DateUtil.getNextZonedDay(from);
		assertEquals(2023, next.getYear());
		assertEquals(2, next.getMonthValue());
		assertEquals(1, next.getDayOfMonth());
		assertEquals(0, next.getHour());
		assertEquals(0, next.getMinute());
		assertEquals(0, next.getSecond());
		assertEquals(0, next.getNano());
	}

	@Test
	public void getZonedDayPlus_advancesByGivenNumberOfDays() {
		ZonedDateTime from = ZonedDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
		ZonedDateTime plusFive = DateUtil.getZonedDayPlus(from, 5);
		assertEquals(6, plusFive.getDayOfMonth());
		assertEquals(0, plusFive.getHour());
	}

	@Test
	public void arabicToISODayOfWeek_and_back_areMutualInverses() {
		for (int arabic = 1; arabic <= 7; arabic++) {
			int iso = DateUtil.arabicToISODayOfWeek(arabic);
			assertTrue("iso out of range: " + iso, iso >= 1 && iso <= 7);
			assertEquals(arabic, DateUtil.ISOToArabicDayOfWeek(iso));
		}
		assertEquals(-1, DateUtil.arabicToISODayOfWeek(0));
		assertEquals(-1, DateUtil.ISOToArabicDayOfWeek(8));
	}

	@Test
	public void isValidDayOfWeek_acceptsOnlyOneThroughSeven() {
		assertTrue(DateUtil.isValidDayOfWeek(1));
		assertTrue(DateUtil.isValidDayOfWeek(7));
		assertFalse(DateUtil.isValidDayOfWeek(0));
		assertFalse(DateUtil.isValidDayOfWeek(8));
	}

	@Test
	public void getHrMinIntVal_encodesHourMinuteAsPackedInt() {
		assertEquals(1430, DateUtil.getHrMinIntVal(14, 30));
		// 90 minutes overflows into +1 hour and 30 minutes.
		assertEquals(1530, DateUtil.getHrMinIntVal(14, 90));
	}

	@Test
	public void getHrMinIntVal_fromString_parsesColonOrDotSeparatedTime() {
		assertEquals(1430, DateUtil.getHrMinIntVal("14:30"));
		// single-digit fractional part is right-padded with a zero, not treated as
		// a decimal, so "14.5" means minute="50", not minute="05".
		assertEquals(1450, DateUtil.getHrMinIntVal("14.5"));
		assertEquals(1400, DateUtil.getHrMinIntVal("14"));
	}

	@Test
	public void extractHourAndMinute_recoverOriginalComponentsFromPackedInt() {
		assertEquals(14, DateUtil.extractHour(1430));
		assertEquals(30, DateUtil.extractMinute(1430));
		assertEquals(-1, DateUtil.extractHour(2500));
	}

	@Test
	public void validateDate_and_format_roundTripWithGivenPattern() {
		LocalDate parsed = DateUtil.validateDate("2023-11-20", "yyyy-MM-dd");
		assertNotNull(parsed);
		assertEquals(LocalDate.of(2023, 11, 20), parsed);
		assertEquals("2023-11-20", DateUtil.format(parsed, "yyyy-MM-dd"));
	}

	@Test
	public void validateDate_invalidInput_returnsNullInsteadOfThrowing() {
		assertNull(DateUtil.validateDate("not-a-date", "yyyy-MM-dd"));
	}

	@Test
	public void isSameDay_comparesCalendarDateIgnoringTime() {
		Calendar c1 = Calendar.getInstance();
		c1.set(2023, Calendar.MAY, 1, 1, 0, 0);
		Calendar c2 = Calendar.getInstance();
		c2.set(2023, Calendar.MAY, 1, 23, 59, 0);
		assertTrue(DateUtil.isSameDay(c1.getTime(), c2.getTime()));

		Calendar c3 = Calendar.getInstance();
		c3.set(2023, Calendar.MAY, 2, 0, 0, 1);
		assertFalse(DateUtil.isSameDay(c1.getTime(), c3.getTime()));
	}

	@Test
	public void isToday_trueForNowAndFalseForNullOrOtherDay() {
		assertTrue(DateUtil.isToday(new Date()));
		assertFalse(DateUtil.isToday(null));
		assertFalse(DateUtil.isToday(new Date(0)));
	}

	@Test
	public void isValidDateFormat_detectsMalformedPatterns() {
		assertTrue(DateUtil.isValidDateFormat("yyyy-MM-dd"));
		assertFalse(DateUtil.isValidDateFormat("yyyy-QQQQQ-dd-garbage["));
	}

	@Test
	public void isFutureDate_todayAndFutureAreTrue_pastIsFalse() {
		assertTrue(DateUtil.isFutureDate(new Date()));
		assertTrue(DateUtil.isFutureDate(new Date(System.currentTimeMillis() + DateUtil.ONEDAY * 2)));
		assertFalse(DateUtil.isFutureDate(new Date(System.currentTimeMillis() - DateUtil.ONEDAY * 2)));
	}

	@Test
	public void toISOString_formatsAsUtcIsoMinutePrecision() {
		// 2023-01-01T00:00:00Z
		String iso = DateUtil.toISOString(1672531200000L);
		assertEquals("2023-01-01T00:00Z", iso);
	}

	@Test
	public void calculateAge_computesFullYearsFromBirthDateToNow() {
		Calendar birth = Calendar.getInstance();
		birth.setTime(new Date());
		birth.add(Calendar.YEAR, -30);
		birth.add(Calendar.DAY_OF_YEAR, -1);
		assertEquals(30, DateUtil.calculateAge(birth.getTime()));
	}

	@Test
	public void getStartTimestamp_and_getEndTimestamp_bracketGivenMonth() {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		java.sql.Timestamp start = DateUtil.getStartTimestamp(Calendar.JANUARY, year);
		java.sql.Timestamp end = DateUtil.getEndTimestamp(Calendar.JANUARY, year);

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(start);
		assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY));

		Calendar endCal = Calendar.getInstance();
		endCal.setTime(end);
		assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH));
		assertTrue(start.before(end));
	}

	@Test
	public void getCovertDate_convertsSlashSeparatedDDMMYYYYtoDashedYYYYMMDD() {
		assertEquals("2023-12-25", DateUtil.getCovertDate("25/12/2023"));
	}
}
