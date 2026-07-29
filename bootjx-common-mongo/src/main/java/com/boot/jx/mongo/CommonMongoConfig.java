package com.boot.jx.mongo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.boot.jx.mongo.CommonMongoTemplate.ReadOnlyMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.TenantDefaultMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.TenantDefaultReadOnlyMongoTemplate;
import com.boot.jx.scope.tnt.TenantDefinations.TenantDefaultQualifier;

@Configuration
@AutoConfigureBefore(MongoDataAutoConfiguration.class)
@PropertySource("classpath:application-mongo.properties")
public class CommonMongoConfig {

	@Value("${spring.data.mongodb.uri}")
	String dataSourceUrl;

	@Autowired
	private CommonMongoSourceProvider commonMongoSourceProvider;

	/**
	 * Overrides Boot's auto-configured {@code mongoMappingContext} so
	 * {@link MongoMappingContext#afterPropertiesSet()} runs only after
	 * {@code SimpleTypeHolder} is wired. Without this, Java 17 blocks reflection
	 * into JDK types (e.g. {@code java.util.regex.Matcher}) during context init.
	 */
	@Bean
	public MongoCustomConversions mongoCustomConversions() {
		return MongoCustomConversions.create(adapter -> adapter.useNativeDriverJavaTimeCodecs(true));
	}

	@Bean
	public MongoMappingContext mongoMappingContext(ApplicationContext applicationContext, MongoProperties mongoProperties,
			MongoCustomConversions conversions) throws ClassNotFoundException {
		MongoMappingContext context = new MongoMappingContext();
		Boolean autoIndexCreation = mongoProperties.isAutoIndexCreation();
		context.setAutoIndexCreation(autoIndexCreation != null ? autoIndexCreation : true);
		context.setInitialEntitySet(new EntityScanner(applicationContext).scan(Document.class));
		if (mongoProperties.getFieldNamingStrategy() != null) {
			context.setFieldNamingStrategy(BeanUtils
					.instantiateClass(mongoProperties.getFieldNamingStrategy(), FieldNamingStrategy.class));
		}
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		context.afterPropertiesSet();
		return context;
	}

	@Bean
	public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context,
			MongoCustomConversions conversions) {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
		converter.setCustomConversions(conversions);
		converter.setMapKeyDotReplacement(DotReplacingConverters.DOT_REPLACEMENT);
		converter.afterPropertiesSet();
		return converter;
	}

	@Bean
	@Primary
	public MongoTemplate mongoTemplate() {
		CommonMongoSource source = commonMongoSourceProvider.getSource();
		MongoDatabaseFactory factory = source.getMongoDbFactory(dataSourceUrl);
		return new MongoTemplateCommonImpl(factory, source.mappingMongoConverter(factory));
	}

	@Bean
	@TenantDefaultQualifier
	public MongoTemplate mongoDefaultTemplate() {
		CommonMongoSource source = commonMongoSourceProvider.getSource();
		MongoDatabaseFactory factory = source.getMongoDbFactory(dataSourceUrl);
		return new MongoTemplateCommonImpl(factory, source.mappingMongoConverter(factory)).onlyDefault(true);
	}

	@Bean
	public TenantDefaultMongoTemplate tenantDefaultMongoTemplate() {
		return new TenantDefaultMongoTemplate();
	}

	@Bean
	@Qualifier("mongoReadOnlyTemplate")
	public MongoTemplate mongoReadOnlyTemplate() {
		CommonMongoSource source = commonMongoSourceProvider.getReadOnlySource();
		MongoDatabaseFactory factory = source.getMongoDbFactory(dataSourceUrl);
		return new MongoTemplateCommonImpl(factory, source.mappingMongoConverter(factory)).readOnly(true);
	}

	@Bean
	public ReadOnlyMongoTemplate readOnlyMongoTemplate() {
		return new ReadOnlyMongoTemplate();
	}

	@Bean
	@Qualifier("mongoTenantDefaultReadOnlyTemplate")
	public MongoTemplate mongoTenantDefaultReadOnlyTemplate() {
		CommonMongoSource source = commonMongoSourceProvider.getReadOnlySource();
		MongoDatabaseFactory factory = source.getMongoDbFactory(dataSourceUrl);
		return new MongoTemplateCommonImpl(factory, source.mappingMongoConverter(factory)).readOnly(true)
				.onlyDefault(true);
	}

	@Bean
	public TenantDefaultReadOnlyMongoTemplate tenantDefaultReadOnlyMongoTemplate() {
		return new TenantDefaultReadOnlyMongoTemplate();
	}

	@Bean
	@Primary
	public MongoStore mongoStore() {
		return new MongoStore();
	}

}
