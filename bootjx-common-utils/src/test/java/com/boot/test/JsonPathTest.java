package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.boot.utils.JsonPath;

/**
 * Regression coverage for {@link JsonPath} — nested map navigation used by
 * MapModel, Mongo query builders, and webhook payload transforms.
 */
public class JsonPathTest {

	@Test
	public void load_readsNestedMapAndArraySegments() {
		Map<String, Object> root = new LinkedHashMap<>();
		Map<String, Object> key1 = new LinkedHashMap<>();
		key1.put("key2", java.util.Arrays.asList("val1", mapOf("key3", "val2")));
		root.put("key1", key1);

		assertEquals(key1, new JsonPath("key1").load(root, null));
		assertEquals("val1", new JsonPath("key1/key2/[0]").load(root, null));
		assertEquals("val2", new JsonPath("key1/key2/[1]/key3").load(root, null));
	}

	@Test
	public void save_writesNestedValuesCreatingIntermediateMaps() {
		Map<String, Object> root = new HashMap<>();
		new JsonPath("a/b/c").save(root, "deep");
		assertEquals("deep", new JsonPath("a/b/c").load(root, null));
	}

	@Test
	public void save_overwritesExistingLeafValue() {
		Map<String, Object> root = mapOf("x", "old");
		new JsonPath("x").save(root, "new");
		assertEquals("new", root.get("x"));
	}

	@Test
	public void exists_detectsPlaceholderSyntaxOnly() {
		assertFalse(JsonPath.exists(null));
		assertFalse(JsonPath.exists(""));
		assertFalse(JsonPath.exists("key1/key2"));
		assertTrue(JsonPath.exists("${key1/key2}"));
	}

	@Test
	public void create_stripsPlaceholderWrapper_and_at_buildsDirectPath() {
		JsonPath fromCreate = JsonPath.create("${foo/bar}");
		JsonPath fromAt = JsonPath.at("foo/bar");
		Map<String, Object> map = mapOf("foo", mapOf("bar", 99));
		assertEquals(Integer.valueOf(99), fromCreate.load(map, 0));
		assertEquals(Integer.valueOf(99), fromAt.load(map, 0));
	}

	@Test
	public void loadNullable_returnsNullForMissingPath() {
		Map<String, Object> root = mapOf("only", "value");
		assertNull(new JsonPath("missing").loadNullable(root, "def"));
		assertNull(new JsonPath("only/nested").loadNullable(root, "def"));
	}

	@Test
	public void roundTrip_preservesTypes() {
		Map<String, Object> root = new HashMap<>();
		new JsonPath("meta/count").save(root, 42);
		new JsonPath("meta/active").save(root, true);
		assertEquals(Integer.valueOf(42), new JsonPath("meta/count").load(root, 0));
		assertEquals(Boolean.TRUE, new JsonPath("meta/active").load(root, false));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> mapOf(Object... kv) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < kv.length; i += 2) {
			map.put((String) kv[i], kv[i + 1]);
		}
		return map;
	}
}
