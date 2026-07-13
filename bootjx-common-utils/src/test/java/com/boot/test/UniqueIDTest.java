package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.UniqueID;

/**
 * Pins down {@link UniqueID}'s uniqueness/format guarantees, which many
 * downstream modules rely on for session/request/trace IDs.
 */
@RunWith(SpringRunner.class)
public class UniqueIDTest {

	@Test
	public void generate_producesUniqueValuesEvenUnderTightLoop() {
		Set<Long> seen = new HashSet<>();
		for (int i = 0; i < 5000; i++) {
			assertTrue("duplicate id generated", seen.add(UniqueID.generate()));
		}
	}

	@Test
	public void generate_isNonDecreasingOverTime() {
		long first = UniqueID.generate();
		long second = UniqueID.generate();
		assertTrue(second >= first);
	}

	@Test
	public void generateString_isRadix36EncodingOfGenerate() {
		String str = UniqueID.generateString();
		assertTrue(str.matches("^[0-9a-z]+$"));
		// Must be parseable back as a base-36 long without error.
		Long.parseLong(str, 36);
	}

	@Test
	public void generateString62_usesAlphanumericAlphabetOnly() {
		String str = UniqueID.generateString62();
		assertTrue("unexpected chars in: " + str, str.matches("^[0-9a-zA-Z]+$"));
	}

	@Test
	public void pref_isThreeUppercaseLetters() {
		assertEquals(3, UniqueID.PREF.length());
		assertTrue(UniqueID.PREF.matches("^[A-Z]{3}$"));
	}

	@Test
	public void generateSessionId_hasThreeDashSeparatedSegments() {
		String sessionId = UniqueID.generateSessionId();
		String[] parts = sessionId.split("-");
		assertEquals(3, parts.length);
		assertEquals(UniqueID.PREF, parts[0]);
		assertEquals(5, parts[1].length());
		assertFalse(parts[2].isEmpty());
	}

	@Test
	public void generateSessionId_withCustomPrefix_padsOrTrimsToFiveChars() {
		String sessionId = UniqueID.generateSessionId("ab");
		String[] parts = sessionId.split("-");
		assertEquals("abxxx", parts[1]);

		String sessionIdLong = UniqueID.generateSessionId("abcdefghij");
		String[] partsLong = sessionIdLong.split("-");
		assertEquals("abcde", partsLong[1]);
	}

	@Test
	public void generateRequestId_defaultsRequestUserToSixZeros() {
		String requestId = UniqueID.generateRequestId("SESSION123", "GRP");
		String[] parts = requestId.split("-");
		// sessionId - 000000 - GRP - trailing62
		assertEquals(4, parts.length);
		assertEquals("SESSION123", parts[0]);
		assertEquals("000000", parts[1]);
		assertEquals("GRP", parts[2]);
		assertFalse(parts[3].isEmpty());
	}

	@Test
	public void generateRequestId_zeroPadsShortRequestUserToSixDigits() {
		String requestId = UniqueID.generateRequestId("SESSION123", "42", "GRP");
		String[] parts = requestId.split("-");
		assertEquals("000042", parts[1]);
	}

	@Test
	public void generateSystemString_composesPrefixMidfixAndSuffix() {
		@SuppressWarnings("deprecation")
		String systemString = UniqueID.generateSystemString("MID", "PFX");
		String[] parts = systemString.split("-");
		assertEquals(UniqueID.PREF, parts[0]);
		assertEquals("MID", parts[1]);
		assertEquals("PFX", parts[2]);
		assertFalse(parts[3].isEmpty());
	}
}
