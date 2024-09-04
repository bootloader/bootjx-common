package com.boot.jx.mongo;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.boot.jx.AppContextUtil;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

public class CommonMongoSource {

	public static final String USE_DEFAULT_DB = "USE_DEFAULT_DB";
	public static final String USE_NO_DB = "USE_NO_DB";
	public static final String READ_ONLY_DB = "READ_ONLY_DB";

	public enum USE_DB {
		USE_DEFAULT_DB, USE_NO_DB, READ_ONLY_DB
	}

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private String dataSourceUrl;

	private String globalDataSourceUrl;

	private String globalDBProfix;

	private String tenant;
	private String tenantDB;

	public String getDataSourceUrl() {
		return dataSourceUrl;
	}

	private static Object lockClient = new Object();
	private static MongoClient sharedMongoClient;

	private static Object lock = new Object();
	MongoDbFactory mongoDbFactory;
	MongoTemplate mongoTemplate;

	private static Object lockNoDb = new Object();
	static MongoDbFactory mongoDbFactoryNoDb;
	static MongoTemplate mongoTemplateNoDb;

	private static Object lockDefault = new Object();
	static MongoDbFactory mongoDbFactoryDefault;
	static MongoTemplate mongoTemplateDefault;

	boolean ready = false;

	public static boolean hasRule(String useNoDb) {
		ApiRequestDetail apiDetails = AppContextUtil.getApiRequestDetail();
		return ArgUtil.is(apiDetails) && apiDetails.hasRule(useNoDb);
	}

	public static USE_DB getRule() {
		if (hasRule(USE_NO_DB)) {
			return USE_DB.USE_NO_DB;
		} else if (hasRule(USE_DEFAULT_DB)) {
			return USE_DB.USE_DEFAULT_DB;
		} else if (hasRule(READ_ONLY_DB)) {
			return USE_DB.READ_ONLY_DB;
		}
		return null;
	}

	public static boolean isReadOnly() {
		return hasRule(READ_ONLY_DB);
	}

	private MongoDbFactory mongoDbFactory(String dataSourceUrl, USE_DB useDb) {
		String tnt = tenant;
		String dbtnt = tenantDB;
		MongoClientURI mongoClientURI = new MongoClientURI(dataSourceUrl);

		synchronized (lockClient) {
			if (sharedMongoClient == null) {
				sharedMongoClient = new MongoClient(mongoClientURI);
				MongoClientOptions o = sharedMongoClient.getMongoClientOptions();
				LOGGER.info("MONGODB: MongoClient:{}:{}   {}", tnt, dbtnt, o.getConnectionsPerHost());
			}
		}

		String dataBaseName = (globalDBProfix + "_" + dbtnt);
		if (ArgUtil.is(useDb, USE_DB.USE_NO_DB)) {
			// dataBaseName = "nodb";
			dataBaseName = mongoClientURI.getDatabase();
		} else if ((!ArgUtil.areEqual(StringUtils.trim(dataSourceUrl), StringUtils.trim(globalDataSourceUrl))
				|| Tenants.isDefault(tnt) || (ArgUtil.is(useDb, USE_DB.USE_DEFAULT_DB)))) {
			dataBaseName = mongoClientURI.getDatabase();
		}
		LOGGER.info("MONGODB: {}:{}:{}", dataBaseName, Tenants.isDefault(tnt), dbtnt);

		return new SimpleMongoDbFactory(sharedMongoClient, dataBaseName);
	}

	public MongoDbFactory getMongoDbFactory(String dataSourceUrl) {
		return this.mongoDbFactory(dataSourceUrl, getRule());
	}

	public MongoDbFactory mongoDbFactory(USE_DB useDb) {
		if (ArgUtil.is(USE_DB.USE_NO_DB, useDb)) {
			if (mongoDbFactoryNoDb == null && ArgUtil.is(dataSourceUrl)) {
				mongoDbFactoryNoDb = mongoDbFactory(dataSourceUrl, useDb);
				LOGGER.warn("mongoDbFactoryNoDb was NULL So created One");
			}
			return mongoDbFactoryNoDb;
		} else if (ArgUtil.is(USE_DB.USE_DEFAULT_DB, useDb)) {
			if (mongoDbFactoryDefault == null && ArgUtil.is(dataSourceUrl)) {
				mongoDbFactoryDefault = mongoDbFactory(dataSourceUrl, useDb);
				LOGGER.warn("mongoDbFactoryDefault was NULL So created One");
			}
			return mongoDbFactoryDefault;
		} else {
			if (mongoDbFactory == null && ArgUtil.is(dataSourceUrl)) {
				mongoDbFactory = mongoDbFactory(dataSourceUrl, useDb);
				LOGGER.warn("mongoDbFactory was NULL So created One");
				ready = true;
			}
			return mongoDbFactory;
		}
	}

	public MongoDbFactory getMongoDbFactory() {
		return this.mongoDbFactory(getRule());
	}

	public MongoTemplate mongoTemplate(USE_DB useDb) {

		if (ArgUtil.is(USE_DB.USE_NO_DB, useDb)) {
			if (mongoTemplateNoDb == null) {
				synchronized (lockNoDb) {
					LOGGER.info("mongoTemplateNoDb is NULL So creating One {} {}", getDataSourceUrl());
					mongoDbFactoryNoDb = mongoDbFactory(useDb);
					if (ArgUtil.is(mongoDbFactoryNoDb)) {
						mongoTemplateNoDb = new MongoTemplate(mongoDbFactoryNoDb,
								mappingMongoConverter(mongoDbFactoryNoDb));
						LOGGER.debug("mongoTemplateNoDb was NULL So created One");
					} else {
						LOGGER.error("mongoDbFactoryNoDb was NULL So cannot create One");
					}
				}
			} else {
				LOGGER.info("mongoDbFactoryNoDb = {}", mongoDbFactoryNoDb.getDb().getName());
			}
			return mongoTemplateNoDb;
		} else if (ArgUtil.is(USE_DB.USE_DEFAULT_DB, useDb)) {
			if (mongoTemplateDefault == null) {
				synchronized (lockDefault) {
					LOGGER.info("mongoTemplate is NULL So creating One {} {}", getDataSourceUrl());
					mongoDbFactoryDefault = mongoDbFactory(useDb);
					if (ArgUtil.is(mongoDbFactoryDefault)) {
						mongoTemplateDefault = new MongoTemplate(mongoDbFactoryDefault,
								mappingMongoConverter(mongoDbFactoryDefault));
						LOGGER.debug("mongoTemplateDefault was NULL So created One");
						ready = true;
					} else {
						LOGGER.error("mongoDbFactoryDefault was NULL So cannot create One");
					}
				}
			} else {
				LOGGER.info("mongoDbFactoryDefault = {}", mongoDbFactoryDefault.getDb().getName());
			}
			return mongoTemplateDefault;
		} else {
			if (mongoTemplate == null) {
				synchronized (lock) {
					LOGGER.debug("mongoTemplate is NULL So creating One {} {}", getDataSourceUrl());
					mongoDbFactory = getMongoDbFactory();
					if (ArgUtil.is(mongoDbFactory)) {
						mongoTemplate = new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
						LOGGER.debug("mongoTemplate was NULL So created One");
						ready = true;
					} else {
						LOGGER.error("mongoDbFactory was NULL So cannot create One");
					}
				}
			} else {
				LOGGER.info("mongoDbFactory = {}", mongoDbFactory.getDb().getName());
			}
			return mongoTemplate;
		}
	}

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate(getRule());
	}

	public MongoTemplate getDefaultMongoTemplate() {
		return mongoTemplate(USE_DB.USE_DEFAULT_DB);
	}

	public boolean isReady() {
		return ready;
	}

	public String getGlobalDataSourceUrl() {
		return globalDataSourceUrl;
	}

	public void setGlobalDataSourceUrl(String globalDataSourceUrl) {
		this.globalDataSourceUrl = globalDataSourceUrl;
	}

	public String getGlobalDBProfix() {
		return globalDBProfix;
	}

	public void setGlobalDBProfix(String globalDBProfix) {
		this.globalDBProfix = globalDBProfix;
	}

	public void setDataSourceUrl(String dataSourceUrl) {
		this.dataSourceUrl = dataSourceUrl;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getTenantDB() {
		return tenantDB;
	}

	public void setTenantDB(String tenantDB) {
		this.tenantDB = tenantDB;
	}

	public MappingMongoConverter mappingMongoConverter(MongoDbFactory factory) {
		// return null;
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MongoCustomConversions conversions = customConversions();

		MongoMappingContext mappingContext = new MongoMappingContext();
		mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		mappingContext.afterPropertiesSet();

		MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
		converter.setCustomConversions(conversions);
		converter.setMapKeyDotReplacement(DotReplacingConverters.DOT_REPLACEMENT);
		converter.afterPropertiesSet();
		return converter;
	}

	public MongoCustomConversions customConversions() {
		return new MongoCustomConversions(Collections.emptyList());
		// Commentiong code below, below lines causes mongo to not use default convertor
		// and any object(pojo not map) in document faces issues of not able to find
		// codec
//		return new MongoCustomConversions(Arrays.asList(new DotReplacingConverters.DotReplacingWriter(),
//				new DotReplacingConverters.DotReplacingReader()));
	}

}
