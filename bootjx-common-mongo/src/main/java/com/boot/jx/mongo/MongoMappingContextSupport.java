package com.boot.jx.mongo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.boot.utils.StringUtils.StringMatcher;

/**
 * Shared wiring for {@link MongoMappingContext} + {@link MappingMongoConverter}
 * used by {@link CommonMongoSource}. Registers runtime types as simple types so
 * Java 17 does not reflect into {@code java.util.regex.Matcher} during mapping.
 */
final class MongoMappingContextSupport {

	private static final Set<Class<?>> JAVA_17_RUNTIME_TYPES = new HashSet<>(Arrays.asList(StringMatcher.class,
			Pattern.class, Matcher.class));

	private MongoMappingContextSupport() {
	}

	static SimpleTypeHolder java17SafeSimpleTypes(SimpleTypeHolder defaults) {
		return new SimpleTypeHolder(JAVA_17_RUNTIME_TYPES, defaults);
	}

	static DeferredInitMongoMappingContext newContext(boolean autoIndexCreation) {
		DeferredInitMongoMappingContext context = new DeferredInitMongoMappingContext();
		context.setAutoIndexCreation(autoIndexCreation);
		return context;
	}

	static MappingMongoConverter createConverter(MongoDatabaseFactory factory, MongoMappingContext context,
			MongoCustomConversions conversions) {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
		converter.setCustomConversions(conversions);
		converter.setMapKeyDotReplacement(DotReplacingConverters.DOT_REPLACEMENT);
		context.setSimpleTypeHolder(
				java17SafeSimpleTypes(converter.getCustomConversions().getSimpleTypeHolder()));
		if (context instanceof DeferredInitMongoMappingContext) {
			((DeferredInitMongoMappingContext) context).finishInitialization();
		} else {
			context.afterPropertiesSet();
		}
		converter.afterPropertiesSet();
		return converter;
	}

}
