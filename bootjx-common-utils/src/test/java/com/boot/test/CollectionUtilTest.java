package com.boot.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.CollectionUtil;

/**
 * Pins down {@link CollectionUtil}'s null-safety and edge-case behavior (empty
 * list bounds, sorting, dedup) so it can be re-verified quickly after JDK/library
 * upgrades.
 */
@RunWith(SpringRunner.class)
public class CollectionUtilTest {

	@Test
	public void getArray_list_returnsElementOrNullWhenOutOfBounds() {
		List<String> list = Arrays.asList("a", "b", "c");
		assertEquals("b", CollectionUtil.getArray(list, 1));
		assertNull(CollectionUtil.getArray(list, 5));
		assertNull(CollectionUtil.getArray((List<String>) null, 0));
	}

	@Test
	public void getArray_array_returnsElementOrNullWhenOutOfBounds() {
		String[] array = { "a", "b", "c" };
		assertEquals("c", CollectionUtil.getArray(array, 2));
		assertNull(CollectionUtil.getArray(array, 10));
		assertNull(CollectionUtil.getArray((String[]) null, 0));
	}

	@Test
	public void getArray_withDefaultValue_fallsBackWhenMissing() {
		List<String> list = Arrays.asList("a", "b");
		assertEquals("b", CollectionUtil.getArray(list, 1, "default"));
		assertEquals("default", CollectionUtil.getArray(list, 5, "default"));

		String[] array = { "x" };
		assertEquals("x", CollectionUtil.getArray(array, 0, "default"));
		assertEquals("default", CollectionUtil.getArray(array, 5, "default"));
	}

	@Test
	public void getArray_nestedList_navigatesBothIndices() {
		List<List<String>> nested = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d", "e"));
		assertEquals("d", CollectionUtil.getArray(nested, 1, 1));
		assertNull(CollectionUtil.getArray(nested, 5, 0));
		assertNull(CollectionUtil.getArray(nested, 0, 10));
	}

	@Test
	public void asSortedList_sortsCopyWithoutMutatingSource() {
		List<Integer> source = new ArrayList<>(Arrays.asList(3, 1, 2));
		List<Integer> sorted = CollectionUtil.asSortedList(source);
		assertEquals(Arrays.asList(1, 2, 3), sorted);
		assertEquals(Arrays.asList(3, 1, 2), source);
	}

	@Test
	public void asSortedSet_ordersElementsAscending() {
		Set<Integer> sorted = CollectionUtil.asSortedSet(Arrays.asList(5, 1, 3));
		assertEquals(Arrays.asList(1, 3, 5), new ArrayList<>(sorted));
	}

	@Test
	public void putArray_growsListWithNullsWhenIndexBeyondSize() {
		List<String> list = new ArrayList<>();
		CollectionUtil.putArray(list, 2, "value");
		assertEquals(3, list.size());
		assertNull(list.get(0));
		assertNull(list.get(1));
		assertEquals("value", list.get(2));
	}

	@Test
	public void existsStringArray_isNullSafe() {
		String[] arr = { "a", "b" };
		assertTrue(CollectionUtil.exists("a", arr));
		assertFalse(CollectionUtil.exists("z", arr));
		assertFalse(CollectionUtil.exists((String) null, arr));
		assertFalse(CollectionUtil.exists("a", (String[]) null));
	}

	@Test
	public void existsGenericCollection_isNullSafe() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		assertTrue(CollectionUtil.exists(2, list));
		assertFalse(CollectionUtil.exists(9, list));
		assertFalse(CollectionUtil.exists((Integer) null, list));
		assertFalse(CollectionUtil.exists(1, (List<Integer>) null));
	}

	@Test
	public void existsOverloads_collectionAndArrayCombinations() {
		List<String> collection = Arrays.asList("a", "b", "c");
		List<String> needleList = Arrays.asList("z", "b");
		List<String> missingList = Arrays.asList("x", "y");
		assertTrue(CollectionUtil.<String>exists(collection, needleList));
		assertFalse(CollectionUtil.<String>exists(collection, missingList));

		assertTrue(CollectionUtil.exists(collection, "z", "c"));
		assertFalse(CollectionUtil.exists(collection, "x", "y"));

		String[] array = { "a", "b", "c" };
		assertTrue(CollectionUtil.<String>exists(array, needleList));
		assertTrue(CollectionUtil.exists(array, "q", "b"));
		assertFalse(CollectionUtil.exists(array, "q", "r"));
	}

	@Test
	public void addAll_appendsFromCollectionOrArray() {
		List<String> target = new ArrayList<>();
		CollectionUtil.addAll(target, Arrays.asList("a", "b"));
		assertEquals(Arrays.asList("a", "b"), target);

		CollectionUtil.addAll(target, new String[] { "c", "d" });
		assertEquals(Arrays.asList("a", "b", "c", "d"), target);
	}

	@Test
	public void listAndSetFactories_buildFromVarargsAndArrays() {
		assertEquals(Arrays.asList(1, 2, 3), CollectionUtil.getList(1, 2, 3));
		assertEquals(Arrays.asList(1, 2, 3), CollectionUtil.asList(1, 2, 3));
		assertEquals(Arrays.asList("a", "b"), CollectionUtil.getList(new String[] { "a", "b" }));
		assertTrue(CollectionUtil.getList(String.class).isEmpty());
		assertEquals(3, CollectionUtil.getSet(1, 2, 2, 3).size());
	}

	@Test
	public void asList_fromIterable_preservesOrder() {
		Iterable<String> iterable = Arrays.asList("x", "y", "z");
		List<String> result = CollectionUtil.asList(iterable);
		assertEquals(Arrays.asList("x", "y", "z"), result);
	}

	@Test
	public void asArray_fromVarargsSetAndList() {
		Integer[] arr = CollectionUtil.asArray(1, 2, 3);
		assertArrayEquals(new Integer[] { 1, 2, 3 }, arr);

		assertEquals(0, CollectionUtil.asArray((Set<String>) null).length);
		assertEquals(0, CollectionUtil.asArray((List<String>) null).length);
		assertEquals(2, CollectionUtil.asArray(Arrays.asList("a", "b")).length);
	}

	@Test
	public void reversed_returnsNewReversedArray_originalUnchanged() {
		Integer[] original = { 1, 2, 3, 4 };
		Integer[] reversed = CollectionUtil.reversed(original);
		assertArrayEquals(new Integer[] { 4, 3, 2, 1 }, reversed);
		assertArrayEquals(new Integer[] { 1, 2, 3, 4 }, original);
	}

	@Test
	public void firstAndGet_handleEmptyAndOutOfRangeGracefully() {
		assertEquals("a", CollectionUtil.first(Arrays.asList("a", "b")));
		assertNull(CollectionUtil.first(new ArrayList<String>()));
		assertNull(CollectionUtil.first((List<String>) null));

		assertEquals("a", CollectionUtil.getOne(Arrays.asList("a", "b")));

		assertEquals("x", CollectionUtil.first(new String[] { "x", "y" }));

		assertEquals("b", CollectionUtil.get(Arrays.asList("a", "b"), 1));
		assertNull(CollectionUtil.get(Arrays.asList("a", "b"), 5));
		assertNull(CollectionUtil.get((List<String>) null, 0));

		String[] arr = { "a", "b" };
		assertEquals("a", CollectionUtil.get(arr, 0));
		assertNull(CollectionUtil.get(arr, 9));
	}

	@Test
	public void set_appendsExtendsOrReplacesAtIndex() {
		List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
		CollectionUtil.set(list, 1, "B");
		assertEquals(Arrays.asList("a", "B"), list);

		CollectionUtil.set(list, 2, "c");
		assertEquals(Arrays.asList("a", "B", "c"), list);

		CollectionUtil.set(list, 5, "f", "EMPTY");
		assertEquals(Arrays.asList("a", "B", "c", "EMPTY", "EMPTY", "f"), list);
	}

	@Test
	public void distinct_removesDuplicatesPreservingOrder() {
		List<Integer> withDupes = Arrays.asList(1, 2, 2, 3, 1);
		assertEquals(Arrays.asList(1, 2, 3), CollectionUtil.distinct(withDupes));
	}
}
