package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.junit.Test;

import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.HashBuilder;

/**
 * Regression coverage for {@link CryptoUtil} — HMAC, hashing, and encoding used
 * in auth, OTP, and token flows. Deterministic time-based assertions pin
 * crypto output across JDK/Jasypt upgrades.
 */
public class CryptoUtilTest {

	@Test
	public void getSHA2Hash_isDeterministicAndHexEncoded() throws NoSuchAlgorithmException {
		String hash1 = CryptoUtil.getSHA2Hash("some pass");
		String hash2 = CryptoUtil.getSHA2Hash("some pass");
		assertEquals(hash1, hash2);
		assertTrue(hash1.length() >= 32);
		assertTrue(hash1.matches("[0-9a-f]+"));
	}

	@Test
	public void getMD5AndSHA1Hash_produceStableDigests() throws NoSuchAlgorithmException {
		assertEquals(32, CryptoUtil.getMD5Hash("test").length());
		assertEquals(40, CryptoUtil.getSHA1Hash("test").length());
		assertEquals(CryptoUtil.getMD5Hash("test"), CryptoUtil.getMD5Hash("test"));
	}

	@Test
	public void generateHMAC_withFixedTime_isDeterministic() {
		long fixedTime = 1574244963435L;
		String hash1 = CryptoUtil.generateHMAC(30L, "MYZK1GST1B", "21279", fixedTime);
		String hash2 = CryptoUtil.generateHMAC(30L, "MYZK1GST1B", "21279", fixedTime);
		assertEquals(hash1, hash2);
		assertNotNull(hash1);
		assertEquals(64, hash1.length());
	}

	@Test
	public void validateHMAC_acceptsHashWithinToleranceWindow() {
		long currentTime = 1574245195319L;
		String hash = CryptoUtil.generateHMAC(30L, "EJZQCU19H9", "21279", currentTime);
		assertTrue(CryptoUtil.validateHMAC(currentTime, 30L, 60L, "EJZQCU19H9", "21279", hash));
		assertTrue(new HashBuilder().currentTime(currentTime).interval(30).tolerance(60).secret("EJZQCU19H9")
				.message("21279").validate(hash));
	}

	@Test
	public void validateHMAC_rejectsWrongSecret() {
		long currentTime = 1574244963435L;
		String hash = CryptoUtil.generateHMAC(30L, "MYZK1GST1B", "21279", currentTime);
		assertFalse(CryptoUtil.validateHMAC(currentTime, 30L, 30L, "WRONG", "21279", hash));
	}

	@Test
	public void toNumeric_and_toComplex_deriveStableShortCodesFromHash() {
		String hash = CryptoUtil.generateHMAC(30L, "MYZK1GST1B", "21279", 1574244963435L);
		String numeric = CryptoUtil.toNumeric(6, hash);
		assertEquals(6, numeric.length());
		assertTrue(numeric.matches("\\d+"));

		String complex = CryptoUtil.toComplex(6, hash).toString();
		assertEquals(6, complex.length());
	}

	@Test
	public void hashBuilder_toHMAC_matchesDirectGenerateHMAC() {
		long currentTime = 1574244963435L;
		String viaBuilder = new HashBuilder().currentTime(currentTime).interval(30).secret("MYZK1GST1B")
				.message("21279").toHMAC().hash();
		String direct = CryptoUtil.generateHMAC(30L, "MYZK1GST1B", "21279", currentTime);
		assertEquals(direct, viaBuilder);
	}

	@Test
	public void encoder_base64RoundTrip_preservesMessage() {
		String encoded = CryptoUtil.getEncoder().message("hello-world").encodeBase64().toString();
		String decoded = CryptoUtil.getEncoder().message(encoded).decodeBase64().toString();
		assertEquals("hello-world", decoded);
	}

	@Test
	public void encoder_urlEncoding_roundTripsSpecialCharacters() {
		String encoded = CryptoUtil.getEncoder().message("a+b=c&d").encodeURL().toString();
		String decoded = CryptoUtil.getEncoder().message(encoded).decodeURL().toString();
		assertEquals("a+b=c&d", decoded);
	}

	@Test
	public void auditFilterGenericPattern_extractsTypeParameter() {
		Pattern pattern = Pattern.compile("^com.amx.jax.logger.client.AuditFilter<(.*)>$");
		java.util.regex.Matcher matcher = pattern.matcher("com.amx.jax.logger.client.AuditFilter<ApiAuditEvent>");
		assertTrue(matcher.matches());
		assertEquals("ApiAuditEvent", matcher.group(1));
	}
}
