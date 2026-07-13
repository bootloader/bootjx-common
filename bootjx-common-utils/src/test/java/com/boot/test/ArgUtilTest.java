package com.boot.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import com.boot.utils.ArgUtil;
import com.boot.utils.ArgUtil.EnumById;
import com.boot.utils.Constants;
import com.boot.utils.EnumType;

/**
 * Regression coverage for {@link ArgUtil} — the most-referenced utility in the
 * codebase (~180+ files). Pins parsing, emptiness, equality, and coercion
 * behavior that must stay stable across Java/Jackson/Spring upgrades.
 */
public class ArgUtilTest {

	public enum PlainStatus implements EnumType {
		ACTIVE, INACTIVE
	}

	public enum ClientCode implements EnumById {
		AMX("AM"), CBS("CB");

		private final String id;

		ClientCode(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}
	}

	@Test
	public void is_and_isEmpty_handleNullBlankAndCollections() {
		assertTrue(ArgUtil.isEmpty(null));
		assertTrue(ArgUtil.isEmpty(""));
		assertTrue(ArgUtil.isEmpty("   "));
		assertFalse(ArgUtil.isEmpty("x"));
		assertTrue(ArgUtil.isEmpty(Collections.emptyList()));
		assertTrue(ArgUtil.is("hello"));
		assertTrue(ArgUtil.blank(null));
		assertTrue(ArgUtil.isNotEmpty("a"));
	}

	@Test
	public void isEmptyValue_treatsZeroAndFalseAsEmpty() {
		assertTrue(ArgUtil.isEmptyValue(null));
		assertTrue(ArgUtil.isEmptyValue(0));
		assertTrue(ArgUtil.isEmptyValue(0L));
		assertTrue(ArgUtil.isEmptyValue(false));
		assertTrue(ArgUtil.isEmptyValue(Boolean.FALSE));
		assertTrue(ArgUtil.isEmptyValue(""));
		assertFalse(ArgUtil.isEmptyValue(3));
		assertFalse(ArgUtil.isEmptyValue("3"));
		assertFalse(ArgUtil.isEmptyValue("0"));
	}

	@Test
	public void is_withMultipleBooleans_followsAllTrueRule() {
		assertTrue(ArgUtil.is(true, true));
		assertFalse(ArgUtil.is(true, false));
		assertTrue(ArgUtil.is(false, false));
		Boolean X = false, Y = false;
		assertTrue(ArgUtil.is(X, Y));
	}

	@Test
	public void isEqual_comparesNullableValues() {
		assertFalse(ArgUtil.isEqual(null));
		assertTrue(ArgUtil.isEqual(null, null));
		assertFalse(ArgUtil.isEqual("", null, null));
		assertTrue(ArgUtil.isEqual(null, "", null));
		assertTrue(ArgUtil.isEqual(null, "x", null));
		assertTrue(ArgUtil.isEqual("a", "a", "b"));
		assertFalse(ArgUtil.isEqual("a", "b"));
	}

	@Test
	public void areEqual_and_equals_matchExpectedSemantics() {
		assertTrue(ArgUtil.areEqual("a", "a"));
		assertFalse(ArgUtil.areEqual("a", "b"));
		assertTrue(ArgUtil.equalsIgnoreCase("AbC", "abc"));
	}

	@Test
	public void parseAsBoolean_supportsBooleanNumberAndString() {
		assertEquals(Boolean.TRUE, ArgUtil.parseAsBoolean(true));
		assertEquals(Boolean.FALSE, ArgUtil.parseAsBoolean(0));
		assertEquals(Boolean.TRUE, ArgUtil.parseAsBoolean(1));
		assertEquals(Boolean.TRUE, ArgUtil.parseAsBoolean("true"));
		assertEquals(Boolean.FALSE, ArgUtil.parseAsBoolean("FALSE"));
		assertEquals(Boolean.FALSE, ArgUtil.parseAsBoolean("maybe"));
		assertEquals(Boolean.TRUE, ArgUtil.parseAsBoolean(null, true));
	}

	@Test
	public void parseAsInteger_and_parseAsLong_handleNumbersAndStrings() {
		assertEquals(Integer.valueOf(42), ArgUtil.parseAsInteger(42));
		assertEquals(Integer.valueOf(42), ArgUtil.parseAsInteger("42"));
		assertEquals(Integer.valueOf(7), ArgUtil.parseAsInteger("7", 0));
		assertEquals(Long.valueOf(99L), ArgUtil.parseAsLong("99"));
		assertEquals(Long.valueOf(0L), ArgUtil.parseAsLongOrZero(null));
	}

	@Test
	public void parseAsBigDecimal_and_parseAsDouble_parseNumericStrings() {
		assertEquals(new BigDecimal("10.5"), ArgUtil.parseAsBigDecimal("10.5"));
		assertEquals(new BigDecimal("1.00"), ArgUtil.parseAsBigDecimal("1.00", BigDecimal.ZERO));
		assertEquals(Double.valueOf(3.14), ArgUtil.parseAsDouble("3.14"));
	}

	@Test
	public void parseAsString_handlesNullAndDefaults() {
		assertEquals("hello", ArgUtil.parseAsString("hello"));
		assertNull(ArgUtil.parseAsString(null));
		assertEquals("def", ArgUtil.parseAsString(null, "def"));
		assertEquals("keep", ArgUtil.parseAsStringNull("keep", "def"));
	}

	@Test
	public void parseAsEnumIgnoreCase_resolvesEnumConstants() {
		assertEquals(PlainStatus.ACTIVE, ArgUtil.parseAsEnumIgnoreCase("active", PlainStatus.class));
		assertEquals(PlainStatus.INACTIVE, ArgUtil.parseAsEnumIgnoreCase("INACTIVE", PlainStatus.class));
	}

	@Test
	public void getType_reportsKnownTypes() {
		assertEquals("string", ArgUtil.getType("x"));
		assertEquals("integer", ArgUtil.getType(1));
		assertEquals("long", ArgUtil.getType(1L));
		assertEquals("double", ArgUtil.getType(1.1));
		assertEquals("boolean", ArgUtil.getType(true));
		assertEquals("date", ArgUtil.getType(new Date()));
		assertEquals("object", ArgUtil.getType(Constants.EMPTY_MAP));
	}

	@Test
	public void getTypeEnum_listsLowercaseIdsForEnumById() {
		String[] ids = ArgUtil.getTypeEnum(ClientCode.AMX);
		assertArrayEquals(new String[] { "am", "cb" }, ids);
	}

	@Test
	public void any_all_none_nand_booleanVarargs() {
		assertTrue(ArgUtil.any(false, true));
		assertFalse(ArgUtil.any(false, false));
		assertTrue(ArgUtil.all(true, true));
		assertFalse(ArgUtil.all(true, false));
		assertTrue(ArgUtil.none(false, false));
		assertTrue(ArgUtil.nand(true, false));
		assertFalse(ArgUtil.nand(true, true));
	}

	@Test
	public void nonEmpty_and_anyOf_returnFirstPresentValue() {
		assertEquals("a", ArgUtil.nonEmpty(null, "a"));
		assertEquals("b", ArgUtil.anyOf(null, "b"));
		assertEquals("c", ArgUtil.ifNotEmpty("", "c"));
		assertEquals("d", ArgUtil.assignDefaultIfNull(null, "d"));
	}

	@Test
	public void presentIn_and_is_checksMembership() {
		assertTrue(ArgUtil.presentIn("a", "x", "a", "b"));
		assertTrue(ArgUtil.is("z", "z"));
		assertFalse(ArgUtil.is("z", "y"));
	}

	@Test
	public void parseAsT_coercesUsingDefaultValueType() {
		assertEquals(Integer.valueOf(5), ArgUtil.parseAsT("5", 0, false));
		assertEquals("123", ArgUtil.parseAsT(123, "txt", false));
	}

	@Test
	public void constructor_isBlockedForThisStaticUtilityClass() throws Exception {
		Constructor<ArgUtil> constructor = ArgUtil.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			fail("Expected IllegalStateException");
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
		}
	}
}
