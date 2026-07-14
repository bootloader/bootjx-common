package com.boot.jx.mongo;

import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class ReadPreferenceMongoDbFactory extends SimpleMongoClientDatabaseFactory {

	private final ReadPreference readPreference;

	public ReadPreferenceMongoDbFactory(MongoClient mongoClient, String databaseName, ReadPreference readPreference) {
		super(mongoClient, databaseName);
		this.readPreference = readPreference;
	}

	@Override
	public MongoDatabase getMongoDatabase() {
		return super.getMongoDatabase().withReadPreference(readPreference);
	}

	@Override
	public MongoDatabase getMongoDatabase(String dbName) {
		return super.getMongoDatabase(dbName).withReadPreference(readPreference);
	}
}
