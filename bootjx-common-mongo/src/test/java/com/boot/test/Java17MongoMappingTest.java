package com.boot.test;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.boot.utils.StringUtils.StringMatcher;

/**
 * Regression: Spring Data must not decompose {@link StringMatcher} into JDK
 * {@link Matcher} on Java 17 (module access error during context init).
 */
public class Java17MongoMappingTest {

	/** Mirrors {@code InboxMessage#getMatcher()} without pulling lib-postman-basic. */
	static class MessageWithMatcher {
		private StringMatcher matcher;

		public StringMatcher getMatcher() {
			return matcher;
		}

		public void setMatcher(StringMatcher matcher) {
			this.matcher = matcher;
		}
	}

	@Test
	public void stringMatcherIsSimpleType() throws Exception {
		MongoMappingContext context = new MongoMappingContext();
		context.setSimpleTypeHolder(new SimpleTypeHolder(
				new HashSet<>(Arrays.asList(StringMatcher.class, Pattern.class, Matcher.class)),
				SimpleTypeHolder.DEFAULT));
		context.afterPropertiesSet();
		assertNotNull(context.getPersistentEntity(StringMatcher.class));
	}

	@Test
	public void nestedStringMatcherDoesNotReflectIntoMatcher() throws Exception {
		MongoMappingContext context = new MongoMappingContext();
		context.setSimpleTypeHolder(new SimpleTypeHolder(
				new HashSet<>(Arrays.asList(StringMatcher.class, Pattern.class, Matcher.class)),
				SimpleTypeHolder.DEFAULT));
		context.setInitialEntitySet(new HashSet<>(Arrays.asList(MessageWithMatcher.class)));
		context.afterPropertiesSet();
		assertNotNull(context.getPersistentEntity(MessageWithMatcher.class));
	}

}
