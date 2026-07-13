package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.boot.jx.auth.AuthStateManager.AuthState;
import com.boot.utils.JsonUtil;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthStateTests {

	public static String stateStr = "eyJtZXNzYWdlIjoiR3paWmgzSzZ1bFNXS2lUOGtlL3dWN0FCczN3ZXYzYXFTRmJLYmllSzFHMmNRNG1jeVY2TVB2YTdDSDV4VVdPdmc0c2t2N3BVVkpXSGFxM05CWHIxeXoxWGdTL281Qm1zMXNGSi9vY0FQOHo2Vkw0aEVKWnFIcmlYVko2WU5HSGRocU1VSXFBRXZoL08vVW1EM1N5dnFwcDk0NXFra1l1NmZaZzdMUFZDdDdJZVZNNUl6WGVHZm1tby8rNmhQaFBFYVN2dVBiSEdWNkk9IiwiZXhwaXJlQXQiOjE3Mjg5ODIxMDA4MjMsImV4cGlyZWQiOmZhbHNlfQ==";

	@Test
	public void decodeTest_restoresCsrfNonceAndRedirectUrl() {
		AuthState state = AuthState.fromString(stateStr);
		assertNotNull(state);
		assertEquals("41R9OHAAMZ", state.getCsrfToken());
		assertEquals("8DHFCuDgeUR", state.getNonce());
		assertEquals("https://app.mehery.xyz/admin/ext/setup/channel/callback/gmail", state.getRedirectUrl());
		assertEquals("demo", state.getDomain());
	}

	@Test
	public void roundTrip_encryptAndDecrypt_preservesFields() {
		AuthState original = new AuthState();
		original.setCsrfToken("csrf-test");
		original.setNonce("nonce-test");
		original.setRedirectUrl("https://example.com/callback");
		original.setDomain("demo");
		original.setTimestamp(System.currentTimeMillis() + 3600_000L);

		AuthState decoded = AuthState.fromString(original.toString());
		assertEquals(original.getCsrfToken(), decoded.getCsrfToken());
		assertEquals(original.getNonce(), decoded.getNonce());
		assertEquals(original.getRedirectUrl(), decoded.getRedirectUrl());
		assertEquals(original.getDomain(), decoded.getDomain());
	}
}
