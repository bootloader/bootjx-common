package com.boot.utils;

/**
 * Commons-lang3 {@link org.apache.commons.lang3.ArrayUtils} facade for
 * bootjx-common-lib and higher modules. Prefer {@link ArrayUtils} from
 * bootjx-common-utils for lightweight helpers; use this class instead of
 * importing {@code org.apache.commons.lang3.ArrayUtils} directly.
 * <p>
 * Only methods currently used by the project are exposed.
 */
public final class CommonArrayUtils {

	private CommonArrayUtils() {
		throw new IllegalStateException("Class for static methods. Can not be instantiated");
	}

	public static void reverse(Object[] array) {
		org.apache.commons.lang3.ArrayUtils.reverse(array);
	}
}
