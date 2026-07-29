package com.boot.jx.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.boot.jx.mongo.CommonMongoTemplate.ReadOnlyMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.TenantDefaultMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.TenantDefaultReadOnlyMongoTemplate;
import com.boot.jx.scope.tnt.TenantDefinations.TenantDefaultQualifier;

@Configuration
@PropertySource("classpath:application-mongo.properties")
public class CommonMongoConfig {

	@Value("${spring.data.mongodb.uri}")
	String dataSourceUrl;

	@Autowired
	private CommonMongoSourceProvider commonMongoSourceProvider;

	@Bean
	MongoMappingContextJava17BeanPostProcessor mongoMappingContextJava17BeanPostProcessor(
			MongoCustomConversions conversions) {
		return new MongoMappingContextJava17BeanPostProcessor(conversions);
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
