package com.boot.utils;

/**
 * Commons-lang3 {@link org.apache.commons.lang3.StringUtils} facade for
 * bootjx-common-lib and higher modules. Prefer {@link StringUtils} from
 * bootjx-common-utils for boot-native helpers; use this class instead of
 * importing {@code org.apache.commons.lang3.StringUtils} directly.
 * <p>
 * Only methods currently used by the project are exposed.
 */
public final class CommonStringUtils {

	private CommonStringUtils() {
		throw new IllegalStateException("Class for static methods. Can not be instantiated");
	}

	public static boolean isBlank(CharSequence cs) {
		return org.apache.commons.lang3.StringUtils.isBlank(cs);
	}

	public static boolean isNotBlank(CharSequence cs) {
		return org.apache.commons.lang3.StringUtils.isNotBlank(cs);
	}

	public static boolean isNotEmpty(CharSequence cs) {
		return org.apache.commons.lang3.StringUtils.isNotEmpty(cs);
	}

	public static String stripEnd(String str, String stripChars) {
		return org.apache.commons.lang3.StringUtils.stripEnd(str, stripChars);
	}

	public static String join(Iterable<?> iterable, String separator) {
		return org.apache.commons.lang3.StringUtils.join(iterable, separator);
	}

	/**
	 * Apache semantics: substring from {@code start} to end of string.
	 * Distinct from {@link StringUtils#substring(String, int)} which truncates
	 * to a max length.
	 */
	public static String substring(String str, int start) {
		return org.apache.commons.lang3.StringUtils.substring(str, start);
	}
}
