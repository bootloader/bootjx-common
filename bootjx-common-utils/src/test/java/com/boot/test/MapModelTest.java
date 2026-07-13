package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.boot.model.MapModel;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.JsonUtil;

/**
 * Regression coverage for {@link MapModel} and {@link NodeEntry} — the
 * dynamic JSON/document wrapper used across Mongo, HTTP, and config layers.
 */
public class MapModelTest {

	@Test
	public void fromMap_getAndPut_roundTripValues() {
		Map<String, Object> source = new HashMap<>();
		source.put("name", "acme");
		source.put("count", 3);
		source.put("amount", "12.50");

		MapModel model = MapModel.from(source);
		assertEquals("acme", model.getString("name"));
		assertEquals(Integer.valueOf(3), model.getInteger("count"));
		assertEquals(new BigDecimal("12.50"), model.getBigDecimal("amount"));
		assertEquals("def", model.getString("missing", "def"));
	}

	@Test
	public void fromJson_parsesObjectAndSupportsEntryApi() {
		MapModel model = MapModel.from("{\"status\":\"OPEN\",\"nested\":{\"id\":7}}");
		assertEquals("OPEN", model.entry("status").asString());
		assertEquals(Long.valueOf(7L), model.pathEntry("nested/id").asLong());
	}

	@Test
	public void entry_putAndReload_updatesUnderlyingMap() {
		MapModel model = MapModel.createInstance();
		model.entry("tenant").save("demo");
		assertEquals("demo", model.getString("tenant"));
	}

	@Test
	public void toJson_roundTripsThroughJacksonCustomDeser() {
		MapModel model = MapModel.createInstance().put("k", "v").put("n", 1);
		String json = model.toJson();
		MapModel parsed = MapModel.from(json);
		assertEquals("v", parsed.getString("k"));
		assertEquals(Integer.valueOf(1), parsed.getInteger("n"));
	}

	@Test
	public void nodeEntry_lessThan_comparesLexicographically() {
		assertTrue(new NodeEntry<String>("v1").lessThan("v2"));
		assertFalse(new NodeEntry<String>("v2").lessThan("v2"));
		assertFalse(new NodeEntry<String>("v3").lessThan("v2"));
		assertTrue(new NodeEntry<String>("").lessThan("v2"));
		assertTrue(new NodeEntry<String>().lessThan("v2"));
	}

	@Test
	public void nodeEntry_greaterThan_and_in_workAsExpected() {
		NodeEntry<String> entry = new NodeEntry<String>("beta");
		assertTrue(entry.greaterThan("alpha"));
		assertTrue(entry.in("beta", "gamma"));
		assertFalse(entry.in("alpha"));
	}

	@Test
	public void nodeEntry_typeCoercion_matchesArgUtilSemantics() {
		NodeEntry<Object> entry = new NodeEntry<Object>().value("42");
		assertEquals(Long.valueOf(42L), entry.asLong());
		assertEquals(Integer.valueOf(42), entry.asInteger());
		assertEquals(new BigDecimal("42"), entry.asBigDecimal());
		assertEquals(Boolean.TRUE, entry.value("true").asBoolean());
	}

	@Test
	public void nodeEntry_exists_isEmpty_isPresent_reflectNullAndValue() {
		NodeEntry<String> missing = new NodeEntry<String>();
		assertFalse(missing.exists());
		assertTrue(missing.isMissing());
		assertFalse(missing.isPresent());
		assertTrue(missing.isEmpty());

		NodeEntry<String> present = new NodeEntry<String>("x");
		assertTrue(present.exists());
		assertTrue(present.isPresent());
		assertFalse(present.isEmpty());
	}

	@Test
	public void fromSafe_invalidJson_doesNotThrowAndMarksCannotSerialize() {
		MapModel model = MapModel.fromSafe("not-json");
		assertTrue(model.cannotSerialize());
		assertEquals("not-json", model.toString());
	}

	@Test
	public void getMap_returnsNestedMapModel() {
		Map<String, Object> nested = new HashMap<>();
		nested.put("id", "x1");
		MapModel model = MapModel.from(MapModel.createInstance().put("child", nested).map());
		assertNotNull(model.getMap("child"));
		assertEquals("x1", model.getMap("child").getString("id"));
	}

	@Test
	public void listModel_parsesJsonArray() {
		MapModel model = MapModel.from("[\"a\",\"b\"]");
		assertNull(model.getString("any"));
		assertNotNull(model.list());
		assertEquals(2, model.list().size());
	}

	@Test
	public void mapModelDeserializer_roundTripsViaJsonUtil() {
		MapModel original = MapModel.createInstance().put("flag", true).put("label", "ok");
		String json = JsonUtil.toJson(original);
		MapModel restored = JsonUtil.fromJson(json, MapModel.class);
		assertEquals(Boolean.TRUE, restored.entry("flag").asBoolean());
		assertEquals("ok", restored.getString("label"));
	}
}
