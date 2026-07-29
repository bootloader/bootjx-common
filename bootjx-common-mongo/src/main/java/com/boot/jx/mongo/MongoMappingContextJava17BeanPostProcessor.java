package com.boot.jx.mongo;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Applies {@link MongoMappingContextSupport#java17SafeSimpleTypes} to Boot's
 * auto-configured {@link MongoMappingContext} before entity metadata is built.
 */
class MongoMappingContextJava17BeanPostProcessor implements BeanPostProcessor {

	private final MongoCustomConversions conversions;

	MongoMappingContextJava17BeanPostProcessor(MongoCustomConversions conversions) {
		this.conversions = conversions;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (bean instanceof MongoMappingContext) {
			MongoMappingContext context = (MongoMappingContext) bean;
			context.setSimpleTypeHolder(MongoMappingContextSupport
					.java17SafeSimpleTypes(conversions.getSimpleTypeHolder()));
		}
		return bean;
	}

}
