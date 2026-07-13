package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.EnumType;

/**
 * Pins down the {@link EnumType} interface's default method contracts, relied
 * on by the custom Jackson serializer ({@code CommonSerilizers.EnumTypeSerializer})
 * to render enums by name.
 */
@RunWith(SpringRunner.class)
public class EnumTypeTest {

	enum Sample implements EnumType {
		FOO, BAR;
	}

	enum OverridingNote implements EnumType {
		FOO {
			@Override
			public String note() {
				return "custom-note";
			}
		};
	}

	@Test
	public void note_defaultsToEnumName() {
		assertEquals("FOO", Sample.FOO.note());
		assertEquals("BAR", Sample.BAR.note());
	}

	@Test
	public void stringValue_defaultsToEnumName() {
		assertEquals("FOO", Sample.FOO.stringValue());
	}

	@Test
	public void enumValue_defaultsToSelf() {
		assertSame(Sample.FOO, Sample.FOO.enumValue());
	}

	@Test
	public void note_canBeOverriddenByImplementor() {
		assertEquals("custom-note", OverridingNote.FOO.note());
	}
}
