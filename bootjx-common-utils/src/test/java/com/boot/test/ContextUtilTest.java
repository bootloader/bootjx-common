package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.boot.utils.ContextUtil;

/**
 * Regression coverage for {@link ContextUtil} thread-local trace/flow context.
 * Critical for request-scoped behavior across servlet and async upgrades.
 */
public class ContextUtilTest {

	@After
	public void tearDown() {
		ContextUtil.clear();
	}

	@Test
	public void getTraceId_generatesWhenMissingAndReusesWhenPresent() {
		String first = ContextUtil.getTraceId(true);
		assertNotNull(first);
		assertTrue(first.length() > 0);
		assertEquals(first, ContextUtil.getTraceId(false));
	}

	@Test
	public void setTraceId_overridesGeneratedValue() {
		ContextUtil.setTraceId("trace-fixed-001");
		assertEquals("trace-fixed-001", ContextUtil.getTraceId(false));
	}

	@Test
	public void generateTraceId_buildsFromSessionAndUser() {
		String traceId = ContextUtil.generateTraceId("sess-1", "user-1");
		assertNotNull(traceId);
		assertTrue(traceId.length() > 0);
		assertEquals(traceId, ContextUtil.getTraceId(false));
	}

	@Test
	public void setFlowfix_sanitizesAndStoresValue() {
		ContextUtil.setFlowfix("AbC-123!");
		assertEquals("AbC123", ContextUtil.getFlowfix());
	}

	@Test
	public void clear_removesThreadLocalState() {
		ContextUtil.setTraceId("to-clear");
		ContextUtil.setFlowfix("flow");
		ContextUtil.clear();
		String afterClear = ContextUtil.getTraceId(false);
		assertEquals("", afterClear);
	}

	@Test
	public void map_returnsIndependentThreadLocalInstances() throws Exception {
		ContextUtil.setTraceId("thread-a");
		final String[] otherThreadTrace = new String[1];

		Thread other = new Thread(() -> {
			otherThreadTrace[0] = ContextUtil.getTraceId(true);
			ContextUtil.clear();
		});
		other.start();
		other.join();

		assertNotNull(otherThreadTrace[0]);
		assertNotEquals("thread-a", otherThreadTrace[0]);
		assertEquals("thread-a", ContextUtil.getTraceId(false));
	}
}
