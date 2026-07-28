package com.boot.jx.def;

import com.boot.utils.ArgUtil;

/**
 * Shared Redisson serialization codec selection. Used in Redis key names so
 * FST- and Jackson-encoded data never share the same keys during codec
 * migrations.
 */
public final class MCQCodecDefs {

	public static enum CODEC {
		FST, JACKSON, DEFAULT
	}

	// FST uses deep reflection on JDK internals (e.g. String.value) and fails on
	// Java 17+ with InaccessibleObjectException. Jackson is safe for Java 17.
	public static final CODEC CODEC_SELECTED = CODEC.JACKSON;

	public static final String CODEC_VERSION = ArgUtil.parseAsString(CODEC_SELECTED.ordinal() + 1);

	private MCQCodecDefs() {
	}

}
