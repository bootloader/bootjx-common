package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.ArgUtil.EnumById;
import com.boot.utils.EnumType;
import com.boot.utils.JsonUtil;

/**
 * Regression coverage for {@link JsonUtil}: the shared Jackson {@code ObjectMapper}
 * (custom BigDecimal/EnumType/EnumById serializers, {@code JavaTimeModule}) used
 * across the whole codebase. These behaviors are exactly what tends to silently
 * change across Jackson/Spring Boot upgrades, so this suite pins down current
 * behavior for later comparison.
 */
@RunWith(SpringRunner.class)
public class JsonUtilTest {

	public static class SamplePojo {
		public String name;
		public int count;
		public BigDecimal amount;
		public Status status;
		public LocalDateTime createdAt;

		public SamplePojo() {
		}

		public SamplePojo(String name, int count, BigDecimal amount, Status status, LocalDateTime createdAt) {
			this.name = name;
			this.count = count;
			this.amount = amount;
			this.status = status;
			this.createdAt = createdAt;
		}
	}

	public enum Status implements EnumType {
		ACTIVE, INACTIVE
	}

	public enum ClientCode implements EnumById {
		AMX("AM"), CBS("CB");

		private String id;

		ClientCode(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}
	}

	@Test
	public void toJson_and_fromJson_roundTripsSimplePojo() {
		SamplePojo pojo = new SamplePojo("acme", 5, new BigDecimal("10.50"), Status.ACTIVE,
				LocalDateTime.of(2024, 1, 15, 10, 30, 0));

		String json = JsonUtil.toJson(pojo);
		assertNotNull(json);

		SamplePojo parsed = JsonUtil.fromJson(json, SamplePojo.class);
		assertEquals("acme", parsed.name);
		assertEquals(5, parsed.count);
		assertEquals(new BigDecimal("10.50"), parsed.amount);
		assertEquals(Status.ACTIVE, parsed.status);
		assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 0), parsed.createdAt);
	}

	@Test
	public void toJson_writesBigDecimalAsRawNumber_notQuotedString() {
		SamplePojo pojo = new SamplePojo("x", 1, new BigDecimal("99.90"), null, null);
		String json = JsonUtil.toJson(pojo);
		// Custom BigDecimalSerializer must emit an unquoted numeric literal.
		assertTrue(json.contains("\"amount\":99.90"));
		assertFalse(json.contains("\"amount\":\"99.90\""));
	}

	@Test
	public void toJson_writesEnumTypeAsPlainName() {
		SamplePojo pojo = new SamplePojo("x", 1, null, Status.INACTIVE, null);
		String json = JsonUtil.toJson(pojo);
		assertTrue(json.contains("\"status\":\"INACTIVE\""));
	}

	@Test
	public void toJson_writesEnumByIdUsingGetId_notEnumName() {
		String json = JsonUtil.toJson(ClientCode.AMX);
		assertEquals("\"AM\"", json);
	}

	@Test
	public void toJson_omitsNullFields() {
		SamplePojo pojo = new SamplePojo(null, 0, null, null, null);
		String json = JsonUtil.toJson(pojo);
		assertFalse(json.contains("\"name\""));
		assertFalse(json.contains("\"amount\""));
		assertFalse(json.contains("\"status\""));
	}

	@Test
	public void fromJson_nullOrBlankOrEmptyQuotes_returnsNull() {
		assertNull(JsonUtil.fromJson((String) null, SamplePojo.class));
		assertNull(JsonUtil.fromJson("", SamplePojo.class));
		assertNull(JsonUtil.fromJson("   ", SamplePojo.class));
		assertNull(JsonUtil.fromJson("\"\"", SamplePojo.class));
	}

	@Test
	public void fromJson_malformedJson_returnsNullInsteadOfThrowing() {
		assertNull(JsonUtil.fromJson("{not-valid-json", SamplePojo.class));
	}

	@Test
	public void fromJson_unknownProperties_areIgnoredNotFatal() {
		SamplePojo parsed = JsonUtil.fromJson("{\"name\":\"acme\",\"unexpectedField\":123}", SamplePojo.class);
		assertNotNull(parsed);
		assertEquals("acme", parsed.name);
	}

	@Test
	public void deepCopy_producesEqualButIndependentInstance() {
		SamplePojo original = new SamplePojo("acme", 5, new BigDecimal("1.00"), Status.ACTIVE,
				LocalDateTime.of(2024, 1, 1, 0, 0));
		SamplePojo copy = JsonUtil.deepCopy(original);

		assertEquals(original.name, copy.name);
		assertEquals(original.amount, copy.amount);
		copy.name = "changed";
		assertEquals("acme", original.name);
	}

	@Test
	public void toMap_and_toObject_roundTripThroughMap() {
		SamplePojo pojo = new SamplePojo("acme", 7, new BigDecimal("2.5"), Status.ACTIVE, null);
		Map<String, Object> map = JsonUtil.toMap(pojo);
		assertEquals("acme", map.get("name"));
		assertEquals(7, map.get("count"));

		SamplePojo back = JsonUtil.toObject(map, SamplePojo.class);
		assertEquals("acme", back.name);
		assertEquals(7, back.count);
	}

	@Test
	public void toJsonMap_flattensNestedObjectToPlainMap() {
		SamplePojo pojo = new SamplePojo("acme", 3, new BigDecimal("4.00"), Status.ACTIVE, null);
		Map<String, Object> map = JsonUtil.toJsonMap(pojo);
		assertEquals("acme", map.get("name"));
		assertEquals("ACTIVE", map.get("status"));
	}

	@Test
	public void fromJsonToMap_parsesJsonObjectAsMap() {
		Map<String, Object> map = JsonUtil.fromJsonToMap("{\"a\":1,\"b\":\"two\"}");
		assertEquals(1, map.get("a"));
		assertEquals("two", map.get("b"));
	}

	@Test
	public void getListFromJsonString_parsesJsonArray() throws Exception {
		List<String> list = JsonUtil.getListFromJsonString("[\"a\",\"b\",\"c\"]");
		assertEquals(3, list.size());
		assertEquals("b", list.get(1));
	}

	@Test
	public void getMapFromJsonString_parsesJsonObject() throws Exception {
		Map<String, Object> map = JsonUtil.getMapFromJsonString("{\"x\":10}");
		assertEquals(10, map.get("x"));
	}

	@Test
	public void parse_dispatchesOnRuntimeTypeOfInput() {
		Map<String, Object> asMap = JsonUtil.fromJsonToMap("{\"name\":\"acme\",\"count\":2}");
		SamplePojo fromMap = JsonUtil.parse(asMap, SamplePojo.class);
		assertEquals("acme", fromMap.name);

		SamplePojo fromString = JsonUtil.parse("{\"name\":\"beta\",\"count\":9}", SamplePojo.class);
		assertEquals("beta", fromString.name);

		assertNull(JsonUtil.parse(null, SamplePojo.class));
	}

	@Test
	public void toJsonPrettyPrint_producesMultiLineOutput() {
		SamplePojo pojo = new SamplePojo("acme", 1, null, null, null);
		String pretty = JsonUtil.toJsonPrettyPrint(pojo);
		assertTrue(pretty.contains("\n"));
	}

	@Test(expected = IllegalStateException.class)
	public void constructor_isBlockedForThisStaticUtilityClass() {
		new JsonUtil();
	}
}
