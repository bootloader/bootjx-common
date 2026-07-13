package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.Random;

/**
 * Pins down {@link Random}'s output length/charset contracts.
 */
@RunWith(SpringRunner.class)
public class RandomTest {

	@Test
	public void getInt_staysWithinInclusiveBounds() {
		for (int i = 0; i < 1000; i++) {
			int value = Random.getInt(5, 10);
			assertTrue("value=" + value, value >= 5 && value <= 10);
		}
	}

	@Test
	public void getInt_singleValueRange_alwaysReturnsThatValue() {
		for (int i = 0; i < 50; i++) {
			assertEquals(7, Random.getInt(7, 7));
		}
	}

	@Test
	public void randomAlpha_producesRequestedLengthFromUppercaseAlphabetOnly() {
		String value = Random.randomAlpha(20);
		assertEquals(20, value.length());
		assertTrue("unexpected chars in: " + value, value.matches("^[A-Z]+$"));
	}

	@Test
	public void randomAlpha_zeroCount_returnsEmptyString() {
		assertEquals("", Random.randomAlpha(0));
	}

	@Test
	public void randomAlpha_withCustomAlphabet_onlyUsesProvidedChars() {
		String value = Random.randomAlpha(30, "XY");
		assertEquals(30, value.length());
		assertTrue(value.matches("^[XY]+$"));
	}

	@Test
	public void randomNumeric_producesOnlyDigits() {
		String value = Random.randomNumeric(15);
		assertEquals(15, value.length());
		assertTrue(value.matches("^[0-9]+$"));
	}

	@Test
	public void randomAlphaNumeric_producesOnlyUppercaseAndDigits() {
		String value = Random.randomAlphaNumeric(25);
		assertEquals(25, value.length());
		assertTrue(value.matches("^[A-Z0-9]+$"));
	}

	@Test
	public void randomHexa_producesOnlyValidHexDigits() {
		String value = Random.randomHexa(16);
		assertEquals(16, value.length());
		assertTrue(value.matches("^[A-F0-9]+$"));
	}

	@Test
	public void randomPassword_producesRequestedLengthFromExtendedCharset() {
		String value = Random.randomPassword(12);
		assertEquals(12, value.length());
		assertTrue(value.matches("^[A-Z0-9!@#$%^&*_=+\\-/.?<>)]+$"));
	}
}
