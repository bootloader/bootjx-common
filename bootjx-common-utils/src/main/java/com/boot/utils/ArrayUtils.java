package com.boot.utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

/**
 * Lightweight array helpers for the foundation layer. Does not depend on
 * commons-lang. For commons-lang parity APIs use
 * {@code com.boot.utils.CommonArrayUtils} in bootjx-common-lib.
 */
public final class ArrayUtils {

	private ArrayUtils() {
		throw new IllegalStateException("Class for static methods. Can not be instantiated");
	}

	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	public static void reverse(Object[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			Object tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(boolean[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			boolean tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(byte[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			byte tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(char[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			char tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(double[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			double tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(float[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			float tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(int[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			int tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(long[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			long tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static void reverse(short[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, j = array.length - 1; j > i; i++, j--) {
			short tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}

	public static boolean contains(Object[] array, Object objectToFind) {
		return indexOf(array, objectToFind) >= 0;
	}

	public static int indexOf(Object[] array, Object objectToFind) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (Objects.equals(array[i], objectToFind)) {
				return i;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] add(T[] array, T element) {
		Class<?> type = array != null ? array.getClass().getComponentType()
				: (element != null ? element.getClass() : Object.class);
		int length = array == null ? 0 : array.length;
		T[] result = (T[]) Array.newInstance(type, length + 1);
		if (array != null) {
			System.arraycopy(array, 0, result, 0, length);
		}
		result[length] = element;
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] addAll(T[] array1, T... array2) {
		if (array1 == null) {
			return array2 == null ? null : Arrays.copyOf(array2, array2.length);
		}
		if (array2 == null) {
			return Arrays.copyOf(array1, array1.length);
		}
		Class<?> type = array1.getClass().getComponentType();
		T[] joined = (T[]) Array.newInstance(type, array1.length + array2.length);
		System.arraycopy(array1, 0, joined, 0, array1.length);
		System.arraycopy(array2, 0, joined, array1.length, array2.length);
		return joined;
	}

	public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) {
			return null;
		}
		int start = Math.max(0, startIndexInclusive);
		int end = Math.min(array.length, endIndexExclusive);
		if (start > end) {
			start = end;
		}
		return Arrays.copyOfRange(array, start, end);
	}

	public static Object[] nullToEmpty(Object[] array) {
		return array == null ? new Object[0] : array;
	}

	public static String[] nullToEmpty(String[] array) {
		return array == null ? new String[0] : array;
	}
}
