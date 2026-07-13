package com.boot.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;

import javax.sql.rowset.serial.SerialClob;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.IoUtils;

/**
 * Pins down {@link IoUtils}'s stream/Clob conversion helpers.
 */
@RunWith(SpringRunner.class)
public class IoUtilsTest {

	@Test
	public void toByteArray_readsEntireInputStreamContent() {
		byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
		byte[] result = IoUtils.toByteArray(new ByteArrayInputStream(data));
		assertArrayEquals(data, result);
	}

	@Test
	public void toByteArray_emptyStream_returnsEmptyArray() {
		byte[] result = IoUtils.toByteArray(new ByteArrayInputStream(new byte[0]));
		assertEquals(0, result.length);
	}

	@Test
	public void inputstream_to_string_readsAllCharacters() throws IOException {
		String text = "some text content";
		String result = IoUtils.inputstream_to_string(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
		assertEquals(text, result);
	}

	@Test
	public void inputStreamToString_withCharset_joinsLinesWithSystemLineSeparator() throws IOException {
		String text = "line1\nline2\nline3";
		String result = IoUtils.inputStreamToString(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
				StandardCharsets.UTF_8);
		String expected = String.join(System.lineSeparator(), "line1", "line2", "line3");
		assertEquals(expected, result);
	}

	@Test
	public void streamWrapper_canBeReadMultipleTimesFromSameSource() throws IOException {
		String text = "repeatable content";
		IoUtils.StreamWrapper wrapper = new IoUtils.StreamWrapper(
				new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

		String first = IoUtils.inputstream_to_string(wrapper.toStream());
		String second = IoUtils.inputstream_to_string(wrapper.toStream());
		assertEquals(text, first);
		assertEquals(text, second);
	}

	@Test
	public void stringToClob_and_clobStringConversion_roundTrip() throws Exception {
		String original = "clob round trip content";
		Clob clob = IoUtils.stringToClob(original);
		String result = IoUtils.clobStringConversion(clob);
		assertEquals(original, result);
	}

	@Test
	public void clobStringConversion_multilineClob_concatenatesLinesWithoutSeparators() throws Exception {
		Clob clob = new SerialClob("line1\nline2".toCharArray());
		String result = IoUtils.clobStringConversion(clob);
		assertEquals("line1line2", result);
	}
}
