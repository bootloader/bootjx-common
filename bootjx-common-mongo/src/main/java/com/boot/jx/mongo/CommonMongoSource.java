package com.boot.jx.mongo;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
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
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class CommonMongoSource {

	public static final String USE_DEFAULT_DB = "USE_DEFAULT_DB";
	public static final String USE_NO_DB = "USE_NO_DB";
	public static final String READ_ONLY_DB = "READ_ONLY_DB";

	public enum USE_DB {
		USE_DEFAULT_DB, USE_NO_DB, READ_ONLY_DB
	}

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static class MongoClientHolder {
		MongoClient client;
		ConnectionString uri;
	}

	private static final ConcurrentHashMap<String, MongoClientHolder> CLIENTS = new ConcurrentHashMap<>();

	private String dataSourceUrl;
	private String dataSourceUrlMasked;

	private String globalDataSourceUrl;

	private String globalDBProfix;

	private String tenant;
	private String tenantDB;
	private String name;

	public CommonMongoSource(String name) {
		this.name = name;
	}

	public String getDataSourceUrl() {
		return dataSourceUrl;
	}

	public String getDataSourceUrlMasked() {
		if (dataSourceUrlMasked == null && dataSourceUrl != null) {
			dataSourceUrlMasked = dataSourceUrl.replaceAll("(mongodb(?:\\+srv)?://[^:]+:)([^@]+)(@)", "$1******$3");
		}
		return dataSourceUrlMasked;
	}

//	private final Object lockClient = new Object();
//	private static MongoClient sharedMongoClient;

	private static Object lock = new Object();
	MongoDatabaseFactory mongoDbFactory;
	MongoTemplate mongoTemplate;

	private static Object lockNoDb = new Object();
	static MongoDatabaseFactory mongoDbFactoryNoDb;
	static MongoTemplate mongoTemplateNoDb;

	private static Object lockDefault = new Object();
	static MongoDatabaseFactory mongoDbFactoryDefault;
	static MongoTemplate mongoTemplateDefault;

	private boolean readPreferenceSecondary;
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

	private MongoDatabaseFactory mongoDbFactory(String dataSourceUrl, USE_DB useDb) {
		String tnt = tenant;
		String dbtnt = tenantDB;

		MongoClientHolder clientHolder = CLIENTS.computeIfAbsent(name, key -> {
			MongoClientHolder holder = new MongoClientHolder();
			holder.uri = new ConnectionString(dataSourceUrl);
			MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(holder.uri)
					.addCommandListener(new CommonMongoCommandListener(name)).build();
			holder.client = MongoClients.create(settings);
			LOGGER.info("MONGODB: MongoClient:{}:{}   {}", tnt, dbtnt,
					settings.getConnectionPoolSettings().getMaxSize());
			return holder;
		});

//		synchronized (lockClient) {
//			if (sharedMongoClient == null) {
//				sharedMongoClient = new MongoClient(mongoClientURI);
//				MongoClientOptions o = sharedMongoClient.getMongoClientOptions();
//				LOGGER.info("MONGODB: MongoClient:{}:{}   {}", tnt, dbtnt, o.getConnectionsPerHost());
//			}
//		}

		String dataBaseName = (globalDBProfix + "_" + dbtnt);
		if (ArgUtil.is(useDb, USE_DB.USE_NO_DB)) {
			// dataBaseName = "nodb";
			dataBaseName = clientHolder.uri.getDatabase();
		} else if ((!ArgUtil.areEqual(StringUtils.trim(dataSourceUrl), StringUtils.trim(globalDataSourceUrl))
				|| Tenants.isDefault(tnt) || (ArgUtil.is(useDb, USE_DB.USE_DEFAULT_DB)))) {
			dataBaseName = clientHolder.uri.getDatabase();
		}

		if (this.readPreferenceSecondary || isReadOnly()) {
			LOGGER.info("MONGODB[RO]: {}:{}:{}", dataBaseName, Tenants.isDefault(tnt), dbtnt);
			return new ReadPreferenceMongoDbFactory(clientHolder.client, dataBaseName,
					ReadPreference.secondaryPreferred());
		} else {
			LOGGER.info("MONGODB[WR]: {}:{}:{}", dataBaseName, Tenants.isDefault(tnt), dbtnt);
			return new SimpleMongoClientDatabaseFactory(clientHolder.client, dataBaseName);
		}

	}

	public MongoDatabaseFactory getMongoDbFactory(String dataSourceUrl) {
		return this.mongoDbFactory(dataSourceUrl, getRule());
	}

	public MongoDatabaseFactory mongoDbFactory(USE_DB useDb) {
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

	public MongoDatabaseFactory getMongoDbFactory() {
		return this.mongoDbFactory(getRule());
	}

	public MongoDatabaseFactory getMongoDbFactoryReadOnly() {
		return this.mongoDbFactory(USE_DB.READ_ONLY_DB);
	}

	public MongoTemplate mongoTemplate(USE_DB useDb) {

		if (ArgUtil.is(USE_DB.USE_NO_DB, useDb)) {
			if (mongoTemplateNoDb == null) {
				synchronized (lockNoDb) {
					LOGGER.info("mongoTemplateNoDb is NULL So creating One {} {}", getDataSourceUrlMasked());
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
				LOGGER.debug("mongoDbFactoryNoDb = {}", mongoDbFactoryNoDb.getMongoDatabase().getName());
			}
			return mongoTemplateNoDb;
		} else if (ArgUtil.is(USE_DB.USE_DEFAULT_DB, useDb)) {
			if (mongoTemplateDefault == null) {
				synchronized (lockDefault) {
					LOGGER.info("mongoTemplate is NULL So creating One {} {}", getDataSourceUrlMasked());
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
				LOGGER.debug("mongoDbFactoryDefault = {}", mongoDbFactoryDefault.getMongoDatabase().getName());
			}
			return mongoTemplateDefault;
		} else {
			if (mongoTemplate == null) {
				synchronized (lock) {
					LOGGER.debug("mongoTemplate is NULL So creating One {} {}", getDataSourceUrlMasked());
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
				LOGGER.debug("mongoDbFactory = {}", mongoDbFactory.getMongoDatabase().getName());
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

	public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory) {
		// return null;
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MongoCustomConversions conversions = customConversions();

		MongoMappingContext mappingContext = new MongoMappingContext();
		mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		// Spring Data MongoDB flipped auto-index-creation's default from true (2.0.x)
		// to false (3.0+). This mapping context is built by hand rather than via
		// Boot's Mongo auto-configuration, so the spring.data.mongodb.auto-index-creation
		// property has no effect here - set it explicitly to keep the @Indexed/
		// @CompoundIndex annotations across the codebase creating indexes on startup.
		mappingContext.setAutoIndexCreation(true);
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

	public boolean isReadPreferenceSecondary() {
		return readPreferenceSecondary;
	}

	public void setReadPreferenceSecondary(boolean readPreferenceSecondary) {
		this.readPreferenceSecondary = readPreferenceSecondary;
	}

}
