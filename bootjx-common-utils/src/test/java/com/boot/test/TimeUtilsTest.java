package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.TimeUtils;
import com.boot.utils.TimeUtils.TimePeriod;
import com.boot.utils.TimeUtils.TimePeriodTimer;

/**
 * Pins down {@link TimeUtils}'s period-string parsing (a small DSL used across
 * config: "5s", "3min", "2h", "1d" ...) and expiry helpers, so upgrades of the
 * regex/enum machinery underneath can be verified quickly.
 */
@RunWith(SpringRunner.class)
public class TimeUtilsTest {

	private static final long SECOND = 1000L;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;
	private static final long DAY = 24 * HOUR;

	@Test
	public void toMillis_parsesEachSupportedUnitAlias() {
		assertEquals(5 * SECOND, TimeUtils.toMillis("5s"));
		assertEquals(3 * MINUTE, TimeUtils.toMillis("3min"));
		assertEquals(2 * HOUR, TimeUtils.toMillis("2h"));
		assertEquals(1 * DAY, TimeUtils.toMillis("1d"));
		assertEquals(1 * DAY * 7, TimeUtils.toMillis("1w"));
		assertEquals(2 * DAY * 31, TimeUtils.toMillis("2mo"));
		assertEquals(1 * DAY * 365, TimeUtils.toMillis("1y"));
	}

	@Test
	public void toMillis_isCaseInsensitiveAndTrimsWhitespaceInNumber() {
		assertEquals(5 * SECOND, TimeUtils.toMillis("5S"));
		assertEquals(10 * MINUTE, TimeUtils.toMillis(" 10 min"));
	}

	@Test
	public void toMillis_fallsBackToPlainLongWhenNoUnitSuffix() {
		assertEquals(12345L, TimeUtils.toMillis("12345"));
		assertEquals(0L, TimeUtils.toMillis("not-a-period"));
	}

	@Test
	public void toHours_derivesFromToMillis() {
		assertEquals(2L, TimeUtils.toHours("2h"));
		assertEquals(24L, TimeUtils.toHours("1d"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void timeSinceString_isAliasForToMillis() {
		assertEquals(TimeUtils.toMillis("4h"), TimeUtils.timeSince("4h"));
	}

	@Test
	public void timeSinceLong_returnsNonNegativeElapsedMillis() {
		long past = System.currentTimeMillis() - 500;
		assertTrue(TimeUtils.timeSince(past) >= 500);
	}

	@Test
	public void isDeadAndIsExpired_agreeAndRespectMaxAge() {
		long fiveSecondsAgo = System.currentTimeMillis() - (5 * SECOND);
		assertTrue(TimeUtils.isDead(fiveSecondsAgo, SECOND));
		assertFalse(TimeUtils.isDead(fiveSecondsAgo, HOUR));
		assertEquals(TimeUtils.isDead(fiveSecondsAgo, SECOND), TimeUtils.isExpired(fiveSecondsAgo, SECOND));
		assertEquals(TimeUtils.isDead(fiveSecondsAgo, SECOND), TimeUtils.isExpired(fiveSecondsAgo, "1s"));
	}

	@Test
	public void isExpired_withDateOverload_matchesLongOverload() {
		long fiveSecondsAgo = System.currentTimeMillis() - (5 * SECOND);
		assertTrue(TimeUtils.isExpired(new java.util.Date(fiveSecondsAgo), SECOND));
	}

	@Test
	public void inHours_wholeDayRangeAlwaysTrue_reversedRangeAlwaysFalse() {
		assertTrue(TimeUtils.inHours(0, 24));
		assertFalse(TimeUtils.inHours(24, 24));
	}

	@Test
	public void getTodayStart_isMidnightOfCurrentDay() {
		Calendar todayStart = TimeUtils.getTodayStart();
		Calendar now = Calendar.getInstance();
		assertEquals(now.get(Calendar.YEAR), todayStart.get(Calendar.YEAR));
		assertEquals(now.get(Calendar.DAY_OF_YEAR), todayStart.get(Calendar.DAY_OF_YEAR));
		assertEquals(0, todayStart.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, todayStart.get(Calendar.MINUTE));
		assertEquals(0, todayStart.get(Calendar.SECOND));
		assertEquals(0, todayStart.get(Calendar.MILLISECOND));
	}

	@Test
	public void beforeTimeMillis_subtractsParsedPeriodFromNow() {
		long before = TimeUtils.beforeTimeMillis("1h");
		long expectedApprox = System.currentTimeMillis() - HOUR;
		assertTrue(Math.abs(before - expectedApprox) < 5000);
	}

	@Test
	public void timePeriod_fromString_computesMillisAndRoundTripsToString() {
		TimePeriod tp = TimePeriod.from("5s");
		assertEquals(5 * SECOND, tp.toMillis());
		assertEquals(5L, tp.toSeconds());
		assertEquals("5s", tp.toString());

		TimePeriod tpOf = TimePeriod.of("2h");
		assertEquals(2 * HOUR, tpOf.toMillis());
		assertEquals(2L, tpOf.toHours());
	}

	@Test
	public void timePeriod_conversionsAgreeWithConstants() {
		TimePeriod tp = TimePeriod.from("1d");
		assertEquals(1L, tp.toDays());
		assertEquals(24L, tp.toHours());
		assertEquals(24L * 60, tp.toMinutes());
	}

	@Test
	public void timePeriodTimer_measuresNonNegativeElapsedTime() {
		TimePeriodTimer timer = TimePeriodTimer.start();
		TimePeriod elapsed = timer.now();
		assertTrue(elapsed.toMillis() >= 0);
	}
}
