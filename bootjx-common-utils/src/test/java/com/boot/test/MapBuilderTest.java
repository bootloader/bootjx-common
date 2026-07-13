package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;

import com.boot.utils.JsonPath;
import com.boot.utils.MapBuilder;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Pins down {@link MapBuilder}'s fluent map-building API and its
 * {@link MultiValueMap} adapter, both used widely for constructing
 * request/response payloads.
 */
@RunWith(SpringRunner.class)
public class MapBuilderTest {

	@Test
	public void put_addsKeyValuePairs_toMapAndBuildReturnSameContents() {
		Map<String, Object> map = MapBuilder.map().put("a", 1).put("b", "two").toMap();
		assertEquals(1, map.get("a"));
		assertEquals("two", map.get("b"));
		assertEquals(map, MapBuilder.map().put("a", 1).put("b", "two").build());
	}

	@Test
	public void putIfNotNull_skipsNullValuesOnly() {
		Map<String, Object> map = MapBuilder.map().putIfNotNull("present", "value").putIfNotNull("absent", null)
				.toMap();
		assertTrue(map.containsKey("present"));
		assertFalse(map.containsKey("absent"));
	}

	@Test
	public void putIfNotEmpty_skipsNullAndEmptyStringsAndCollections() {
		Map<String, Object> map = MapBuilder.map().putIfNotEmpty("str", "value").putIfNotEmpty("emptyStr", "")
				.putIfNotEmpty("nullVal", null).putIfNotEmpty("emptyList", Arrays.asList())
				.putIfNotEmpty("nonEmptyList", Arrays.asList("x")).toMap();

		assertTrue(map.containsKey("str"));
		assertTrue(map.containsKey("nonEmptyList"));
		assertFalse(map.containsKey("emptyStr"));
		assertFalse(map.containsKey("nullVal"));
		assertFalse(map.containsKey("emptyList"));
	}

	@Test
	public void data_storesValueUnderDataKey() {
		Map<String, Object> map = MapBuilder.map().data("payload").toMap();
		assertEquals("payload", map.get("data"));
	}

	@Test
	public void put_withJsonPath_writesNestedStructure() {
		Map<String, Object> map = MapBuilder.map().put(JsonPath.at("user/name"), "acme").toMap();
		@SuppressWarnings("unchecked")
		Map<String, Object> user = (Map<String, Object>) map.get("user");
		assertEquals("acme", user.get("name"));
	}

	@Test
	public void toJsonNode_convertsMapToJacksonTree() {
		JsonNode node = MapBuilder.map().put("count", 3).toJsonNode();
		assertEquals(3, node.get("count").asInt());
	}

	@Test
	public void multiValueMap_addAccumulatesValuesPerKey() {
		MultiValueMap<String, String> multi = MapBuilder.multiValueMap();
		multi.add("k", "v1");
		multi.add("k", "v2");
		assertEquals(Arrays.asList("v1", "v2"), multi.get("k"));
		assertEquals("v1", multi.getFirst("k"));
	}

	@Test
	public void multiValueMap_set_replacesAllValuesForKey() {
		MultiValueMap<String, String> multi = MapBuilder.multiValueMap();
		multi.add("k", "v1");
		multi.set("k", "v2");
		assertEquals(Arrays.asList("v2"), multi.get("k"));
	}

	@Test
	public void multiValueMap_toSingleValueMap_keepsFirstValuePerKey() {
		MultiValueMap<String, String> multi = MapBuilder.multiValueMap();
		multi.add("k1", "a");
		multi.add("k1", "b");
		multi.add("k2", "c");
		Map<String, String> single = multi.toSingleValueMap();
		assertEquals("a", single.get("k1"));
		assertEquals("c", single.get("k2"));
	}

	@Test
	public void multiValueMap_addAll_mergesFromAnotherMultiValueMap() {
		MultiValueMap<String, String> source = MapBuilder.multiValueMap();
		source.add("k", "v1");
		source.add("k", "v2");

		MultiValueMap<String, String> target = MapBuilder.multiValueMap();
		target.addAll(source);
		assertEquals(Arrays.asList("v1", "v2"), target.get("k"));
	}

	@Test
	public void multiValueMap_fromExistingMap_wrapsWithoutCopying() {
		Map<String, List<String>> backing = new java.util.HashMap<>();
		backing.put("k", Arrays.asList("v1"));
		MultiValueMap<String, String> multi = MapBuilder.multiValueMap(backing);
		assertEquals(Arrays.asList("v1"), multi.get("k"));
	}
}
