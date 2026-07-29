package com.boot.jx.mongo;

import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Defers {@link #afterPropertiesSet()} until {@link #initialize()} is called.
 * Spring initializes {@code MongoMappingContext} beans as {@code InitializingBean}s
 * before {@link org.springframework.data.mongodb.core.convert.MappingMongoConverter}
 * is wired; on Java 17 that early init reflects into {@code java.util.regex.Matcher}.
 */
class DeferredInitMongoMappingContext extends MongoMappingContext {

	private volatile boolean initialized;

	@Override
	public void afterPropertiesSet() {
		if (initialized) {
			super.afterPropertiesSet();
		}
	}

	void finishInitialization() {
		initialized = true;
		super.afterPropertiesSet();
	}

}
