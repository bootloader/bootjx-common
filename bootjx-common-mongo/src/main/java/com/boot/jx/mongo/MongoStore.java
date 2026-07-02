package com.boot.jx.mongo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ScriptOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;

import com.boot.jx.model.ModelPatch.ModelPatches;
import com.boot.jx.mongo.CommonDocInterfaces.IMongoQueryBuilder;
import com.boot.jx.mongo.CommonDocInterfaces.SimpleDocument;
import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.jx.mongo.CommonMongoStore.PaginatedQuery;
import com.boot.jx.mongo.CommonMongoTemplate.ReadOnlyMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.TenantDefaultMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.TenantDefaultReadOnlyMongoTemplate;
import com.boot.jx.mongo.MongoUtils.MongoResultProcessor;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoStore implements CommonMongoOperations {

	@Autowired
	protected CommonMongoTemplate commonMongoTemplate;

	@Autowired
	protected TenantDefaultMongoTemplate tenantDefaultMongoTemplate;

	@Autowired
	protected ReadOnlyMongoTemplate readOnlyMongoTemplate;

	@Autowired
	protected TenantDefaultReadOnlyMongoTemplate tenantDefaultReadOnlyMongoTemplate;

	public CommonMongoTemplate db(boolean readonly, boolean tenantDefault) {
		if (readonly) {
			if (tenantDefault) {
				return tenantDefaultReadOnlyMongoTemplate;
			}
			return readOnlyMongoTemplate;
		} else if (tenantDefault) {
			return tenantDefaultMongoTemplate;
		}
		return commonMongoTemplate;
	}

	public boolean isReadOnly() {
		return false;
	}

	public boolean isTenantDefault() {
		return false;
	}

	public CommonMongoTemplate db() {
		return db(isReadOnly(), isTenantDefault());
	}

	public CommonMongoTemplate readOnly() {
		return db(true, isTenantDefault());
	}

	public CommonMongoTemplate readOnly(boolean tenantDefault) {
		return db(true, tenantDefault);
	}

	public CommonMongoTemplate tenantDefault() {
		return db(isReadOnly(), true);
	}

	public CommonMongoTemplate tenantDefault(boolean readOnly) {
		return db(readOnly, true);
	}

	public String getCollectionName(Class<?> entityClass) {
		return db().getCollectionName(entityClass);
	}

	public Document executeCommand(String jsonCommand) {
		return db().executeCommand(jsonCommand);
	}

	public Document executeCommand(Document command) {
		return db().executeCommand(command);
	}

	public Document executeCommand(Document command, ReadPreference readPreference) {
		return db().executeCommand(command, readPreference);
	}

	public void executeQuery(Query query, String collectionName, DocumentCallbackHandler dch) {
		db().executeQuery(query, collectionName, dch);

	}

	public <T> T execute(DbCallback<T> action) {
		return db().execute(action);
	}

	public <T> T execute(Class<?> entityClass, CollectionCallback<T> action) {
		return db().execute(entityClass, action);
	}

	public <T> T execute(String collectionName, CollectionCallback<T> action) {
		return db().execute(collectionName, action);
	}

	public <T> CloseableIterator<T> stream(Query query, Class<T> entityType) {
		return db().stream(query, entityType);
	}

	public <T> MongoCollection<Document> createCollection(Class<T> entityClass) {
		return db().createCollection(entityClass);
	}

	public <T> MongoCollection<Document> createCollection(Class<T> entityClass, CollectionOptions collectionOptions) {
		return db().createCollection(entityClass, collectionOptions);
	}

	public MongoCollection<Document> createCollection(String collectionName) {
		return db().createCollection(collectionName);
	}

	public MongoCollection<Document> createCollection(String collectionName, CollectionOptions collectionOptions) {
		return db().createCollection(collectionName, collectionOptions);
	}

	public Set<String> getCollectionNames() {
		return db().getCollectionNames();
	}

	public MongoCollection<Document> getCollection(String collectionName) {
		return db().getCollection(collectionName);
	}

	public <T> boolean collectionExists(Class<T> entityClass) {
		return db().collectionExists(entityClass);
	}

	public boolean collectionExists(String collectionName) {
		return db().collectionExists(collectionName);
	}

	public <T> void dropCollection(Class<T> entityClass) {
		db().dropCollection(entityClass);
	}

	public void dropCollection(String collectionName) {
		db().dropCollection(collectionName);

	}

	public IndexOperations indexOps(String collectionName) {
		return db().indexOps(collectionName);
	}

	public IndexOperations indexOps(Class<?> entityClass) {
		return db().indexOps(entityClass);
	}

	public ScriptOperations scriptOps() {
		return db().scriptOps();
	}

	public BulkOperations bulkOps(BulkMode mode, String collectionName) {
		return db().bulkOps(mode, collectionName);
	}

	public BulkOperations bulkOps(BulkMode mode, Class<?> entityType) {
		return db().bulkOps(mode, entityType);
	}

	public BulkOperations bulkOps(BulkMode mode, Class<?> entityType, String collectionName) {
		return db().bulkOps(mode, entityType, collectionName);
	}

	public <T> List<T> findAll(Class<T> entityClass) {
		return db().findAll(entityClass);
	}

	public <T> List<T> findAll(Class<T> entityClass, String collectionName) {
		return db().findAll(entityClass, collectionName);
	}

	public <T> GroupByResults<T> group(String inputCollectionName, GroupBy groupBy, Class<T> entityClass) {
		return db().group(inputCollectionName, groupBy, entityClass);
	}

	public <T> GroupByResults<T> group(Criteria criteria, String inputCollectionName, GroupBy groupBy,
			Class<T> entityClass) {
		return db().group(criteria, inputCollectionName, groupBy, entityClass);
	}

	public <O> AggregationResults<O> aggregate(TypedAggregation<?> aggregation, String collectionName,
			Class<O> outputType) {
		return db().aggregate(aggregation, outputType);
	}

	public <O> AggregationResults<O> aggregate(TypedAggregation<?> aggregation, Class<O> outputType) {
		return db().aggregate(aggregation, outputType);
	}

	public <O> AggregationResults<O> aggregate(Aggregation aggregation, Class<?> inputType, Class<O> outputType) {
		return db().aggregate(aggregation, inputType, outputType);
	}

	public <O> AggregationResults<O> aggregate(Aggregation aggregation, String collectionName, Class<O> outputType) {
		return db().aggregate(aggregation, collectionName, outputType);
	}

	public <T> MapReduceResults<T> mapReduce(String inputCollectionName, String mapFunction, String reduceFunction,
			Class<T> entityClass) {
		return db().mapReduce(inputCollectionName, mapFunction, reduceFunction, entityClass);
	}

	public <T> MapReduceResults<T> mapReduce(String inputCollectionName, String mapFunction, String reduceFunction,
			MapReduceOptions mapReduceOptions, Class<T> entityClass) {
		return db().mapReduce(inputCollectionName, mapFunction, reduceFunction, mapReduceOptions, entityClass);
	}

	public <T> MapReduceResults<T> mapReduce(Query query, String inputCollectionName, String mapFunction,
			String reduceFunction, Class<T> entityClass) {
		return db().mapReduce(query, inputCollectionName, mapFunction, reduceFunction, entityClass);
	}

	public <T> MapReduceResults<T> mapReduce(Query query, String inputCollectionName, String mapFunction,
			String reduceFunction, MapReduceOptions mapReduceOptions, Class<T> entityClass) {
		return mapReduce(query, inputCollectionName, mapFunction, reduceFunction, mapReduceOptions, entityClass);
	}

	public <T> GeoResults<T> geoNear(NearQuery near, Class<T> entityClass) {
		return db().geoNear(near, entityClass);
	}

	public <T> GeoResults<T> geoNear(NearQuery near, Class<T> entityClass, String collectionName) {
		return db().geoNear(near, entityClass, collectionName);
	}

	public <T> T findOne(Query query, Class<T> entityClass) {
		return db().findOne(query, entityClass);
	}

	public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {
		return db().findOne(query, entityClass, collectionName);
	}

	public boolean exists(Query query, String collectionName) {
		return db().exists(query, collectionName);
	}

	public boolean exists(Query query, Class<?> entityClass) {
		return db().exists(query, entityClass);
	}

	public boolean exists(Query query, Class<?> entityClass, String collectionName) {
		return db().exists(query, entityClass, collectionName);
	}

	public <T> List<T> find(Query query, Class<T> entityClass) {
		return db().find(query, entityClass);
	}

	public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
		return db().find(query, entityClass, collectionName);
	}

	public <T> T findById(Object id, Class<T> entityClass) {
		return db().findById(id, entityClass);
	}

	public <T> T findById(Object id, Class<T> entityClass, String collectionName) {
		return db().findById(id, entityClass, collectionName);
	}

	@Override
	public <T> T findByIdString(String id, Class<T> clazz) {
		return db().findByIdString(id, clazz);
	}

	@Override
	public <T> T findByIdSafeCheck(Object id, Class<T> entityClass) {
		return db().findByIdSafeCheck(id, entityClass);
	}

	@Override
	public <T> T findByIdOrDefault(String id, T defaultValue) {
		return db().findByIdOrDefault(id, defaultValue);
	}

	public <T> T findAndModify(Query query, Update update, Class<T> entityClass) {
		return db().findAndModify(query, update, entityClass);
	}

	public <T> T findAndModify(Query query, Update update, Class<T> entityClass, String collectionName) {
		return db().findAndModify(query, update, entityClass, collectionName);
	}

	public <T> T findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass) {
		return db().findAndModify(query, update, options, entityClass);
	}

	public <T> T findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass,
			String collectionName) {
		return db().findAndModify(query, update, options, entityClass, collectionName);
	}

	public <T> T findAndRemove(Query query, Class<T> entityClass) {
		return db().findAndRemove(query, entityClass);
	}

	public <T> T findAndRemove(Query query, Class<T> entityClass, String collectionName) {
		return db().findAndRemove(query, entityClass, collectionName);
	}

	public long count(Query query, Class<?> entityClass) {
		return db().count(query, entityClass);
	}

	public long count(Query query, String collectionName) {
		return db().count(query, collectionName);
	}

	public long count(Query query, Class<?> entityClass, String collectionName) {
		return db().count(query, entityClass, collectionName);
	}

	public void insert(Object objectToSave) {
		db().insert(objectToSave);
	}

	public void insert(Object objectToSave, String collectionName) {
		db().insert(objectToSave, collectionName);
	}

	public void insert(Collection<? extends Object> batchToSave, Class<?> entityClass) {
		db().insert(batchToSave, entityClass);
	}

	public void insert(Collection<? extends Object> batchToSave, String collectionName) {
		db().insert(batchToSave, collectionName);
	}

	public void insertAll(Collection<? extends Object> objectsToSave) {
		db().insertAll(objectsToSave);
	}

	public void save(Object objectToSave) {
		db().save(objectToSave);
	}

	public void save(Object objectToSave, String collectionName) {
		db().save(objectToSave, collectionName);
	}

	public UpdateResult upsert(Query query, Update update, Class<?> entityClass) {
		return db().upsert(query, update, entityClass);
	}

	public UpdateResult upsert(Query query, Update update, String collectionName) {
		return db().upsert(query, update, collectionName);
	}

	public UpdateResult upsert(Query query, Update update, Class<?> entityClass, String collectionName) {
		return db().upsert(query, update, entityClass, collectionName);
	}

	public UpdateResult updateFirst(Query query, Update update, Class<?> entityClass) {
		return db().updateFirst(query, update, entityClass);
	}

	public UpdateResult updateFirst(Query query, Update update, String collectionName) {
		return db().updateFirst(query, update, collectionName);
	}

	public UpdateResult updateFirst(Query query, Update update, Class<?> entityClass, String collectionName) {
		return db().updateFirst(query, update, entityClass, collectionName);
	}

	public UpdateResult updateMulti(Query query, Update update, Class<?> entityClass) {
		return db().updateMulti(query, update, entityClass);
	}

	public UpdateResult updateMulti(Query query, Update update, String collectionName) {
		return db().updateMulti(query, update, collectionName);
	}

	public UpdateResult updateMulti(Query query, Update update, Class<?> entityClass, String collectionName) {
		return db().updateMulti(query, update, entityClass, collectionName);
	}

	public DeleteResult remove(Object object) {
		return db().remove(object);
	}

	public DeleteResult remove(Object object, String collection) {
		return db().remove(object, collection);
	}

	public DeleteResult remove(Query query, Class<?> entityClass) {
		return db().remove(query, entityClass);
	}

	public DeleteResult remove(Query query, Class<?> entityClass, String collectionName) {
		return db().remove(query, entityClass, collectionName);
	}

	public DeleteResult remove(Query query, String collectionName) {
		return db().remove(query, collectionName);
	}

	public <T> List<T> findAllAndRemove(Query query, String collectionName) {
		return db().findAllAndRemove(query, collectionName);
	}

	public <T> List<T> findAllAndRemove(Query query, Class<T> entityClass) {
		return db().findAllAndRemove(query, entityClass);
	}

	public <T> List<T> findAllAndRemove(Query query, Class<T> entityClass, String collectionName) {
		return db().findAllAndRemove(query, entityClass, collectionName);
	}

	public MongoConverter getConverter() {
		return db().getConverter();
	}

	public MongoDatabase getDb() {
		return db().getDb();
	}

	public <T> T save(DocQueryBuilder<T> builder) {
		return db().save(builder);
	}

	public <T> T findOne(IMongoQueryBuilder<T> builder) {
		return db().findOne(builder);
	}

	@Override
	public <T> List<T> find(IMongoQueryBuilder<T> builder) {
		return db().find(builder);
	}

	@Override
	public <T> List<T> find(IMongoQueryBuilder<T> builder, Class<T> clazz) {
		return db().find(builder, clazz);
	}

	@Override
	public <T> UpdateResult upsert(IMongoQueryBuilder<T> builder) {
		return db().update(builder);
	}

	@Override
	public <T> UpdateResult update(IMongoQueryBuilder<T> builder) {
		return db().update(builder);
	}

	@Override
	public <T> UpdateResult updateFirst(IMongoQueryBuilder<T> builder) {
		return db().updateFirst(builder);
	}

	@Override
	public <T> long count(IMongoQueryBuilder<T> builder) {
		return db().count(builder);
	}

	public <T> T removeAndAudit(T objectToSave) {
		return db().removeAndAudit(objectToSave);
	}

	@Override
	public <T> T removeAndAudit(String id, Class<T> clazz) {
		return db().removeAndAudit(id, clazz);
	}

	@Override
	public MongoResultProcessor<Document> collection(String collection) {
		return db().collection(collection);
	}

	@Override
	public <TResult> MongoResultProcessor<TResult> collection(String collection, Class<TResult> clazz) {
		return db().collection(collection, clazz);
	}

	@Override
	public <TResult> MongoResultProcessor<TResult> collection(Class<TResult> clazz) {
		return db().collection(clazz);
	}

	@Override
	public <T> UpdateResult updateMulti(IMongoQueryBuilder<T> builder) {
		return db().updateMulti(builder);
	}

	@Override
	public <T extends SimpleDocument> UpdateResult patch(ModelPatches patches, Class<T> clazz)
			throws InstantiationException, IllegalAccessException {
		return db().patch(patches, clazz);
	}

	public <T> PaginatedQuery<T> getPages(PaginatedQuery<T> query) {
		return db().getPages(query);
	}

}
