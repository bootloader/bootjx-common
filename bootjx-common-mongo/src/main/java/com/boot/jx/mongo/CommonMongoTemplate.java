package com.boot.jx.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.scope.tnt.TenantDefinations.TenantDefaultQualifier;

@Primary
@Component
public class CommonMongoTemplate extends CommonMongoStore<CommonMongoTemplate> {

	public static class TenantDefaultMongoTemplate extends CommonMongoTemplate {

		@Autowired
		@TenantDefaultQualifier
		protected MongoTemplate mongoTemplateTenantDefault;

		@Override
		protected MongoTemplate getCommonMongoTemplate() {
			return mongoTemplateTenantDefault;
		}

	}

	public static class ReadOnlyMongoTemplate extends CommonMongoTemplate {

		@Autowired
		@Qualifier("mongoReadOnlyTemplate")
		protected MongoTemplate mongoReadOnlyTemplate;

		@Override
		protected MongoTemplate getCommonMongoTemplate() {
			return mongoReadOnlyTemplate;
		}

	}

	public static class TenantDefaultReadOnlyMongoTemplate extends CommonMongoTemplate {

		@Autowired
		@Qualifier("mongoTenantDefaultReadOnlyTemplate")
		protected MongoTemplate mongoTenantDefaultReadOnlyTemplate;

		@Override
		protected MongoTemplate getCommonMongoTemplate() {
			return mongoTenantDefaultReadOnlyTemplate;
		}

	}

}
