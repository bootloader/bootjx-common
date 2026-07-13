package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.boot.jx.AppContextUtil;
import com.boot.utils.ContextUtil;

/**
 * Regression coverage for {@link AppContextUtil} session/trace/tenant helpers
 * layered on {@link ContextUtil}. Used in every server request path.
 */
public class AppContextUtilTest {

	@After
	public void tearDown() {
		ContextUtil.clear();
		AppContextUtil.setTenant((String) null);
	}

	@Test
	public void setAndGetTenant_roundTrip() {
		AppContextUtil.setTenant("demo");
		assertEquals("demo", AppContextUtil.getTenant());
	}

	@Test
	public void setSessionId_and_getSessionId_returnStoredValue() {
		AppContextUtil.setSessionId("sess-abc");
		assertEquals("sess-abc", AppContextUtil.getSessionId(false));
	}

	@Test
	public void getSessionId_generatesWhenMissingAndGenerateTrue() {
		String sessionId = AppContextUtil.getSessionId(true);
		assertNotNull(sessionId);
		assertTrue(sessionId.length() > 0);
		assertEquals(sessionId, AppContextUtil.getSessionId(false));
	}

	@Test
	public void loadTraceId_parsesSessionAndFlowfixFromSystemTrace() {
		AppContextUtil.loadTraceId("RPD-c8d0a-814KuAeqU2L-000000-000-814Kv6a1raN");
		assertEquals("RPD-c8d0a-814KuAeqU2L", AppContextUtil.getSessionId(false));
		assertEquals("000", AppContextUtil.getFlowfix());
	}

	@Test
	public void setFlowfix_isReadableThroughAppContext() {
		AppContextUtil.setFlowfix("Flow-X");
		assertEquals("FlowX", AppContextUtil.getFlowfix());
	}

	@Test
	public void getTraceId_withGenerate_usesSessionContext() {
		AppContextUtil.setSessionId("sess-1");
		AppContextUtil.setRequestUser("agent-1");
		String traceId = AppContextUtil.getTraceId(true, false);
		assertNotNull(traceId);
		assertTrue(traceId.length() > 0);
	}

	@Test
	public void setEnv_and_getEnv_storeEnvironmentKey() {
		AppContextUtil.setEnv("staging");
		assertEquals("staging", AppContextUtil.getEnv());
	}
}
