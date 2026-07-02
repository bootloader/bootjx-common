package com.boot.jx.mongo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ScriptOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
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
import com.boot.jx.mongo.MongoUtils.MongoResultProcessor;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public interface CommonMongoOperations {

	public String getCollectionName(Class<?> entityClass);

	public Document executeCommand(String jsonCommand);

	public Document executeCommand(Document command);

	public Document executeCommand(Document command, ReadPreference readPreference);

	public void executeQuery(Query query, String collectionName, DocumentCallbackHandler dch);

	public <T> T execute(DbCallback<T> action);

	public <T> T execute(Class<?> entityClass, CollectionCallback<T> action);

	public <T> T execute(String collectionName, CollectionCallback<T> action);

	public <T> CloseableIterator<T> stream(Query query, Class<T> entityType);

	public <T> MongoCollection<Document> createCollection(Class<T> entityClass);

	public <T> MongoCollection<Document> createCollection(Class<T> entityClass, CollectionOptions collectionOptions);

	public MongoCollection<Document> createCollection(String collectionName);

	public MongoCollection<Document> createCollection(String collectionName, CollectionOptions collectionOptions);

	public Set<String> getCollectionNames();

	public MongoCollection<Document> getCollection(String collectionName);

	public <T> boolean collectionExists(Class<T> entityClass);

	public boolean collectionExists(String collectionName);

	public <T> void dropCollection(Class<T> entityClass);

	public void dropCollection(String collectionName);

	public IndexOperations indexOps(String collectionName);

	public IndexOperations indexOps(Class<?> entityClass);

	public ScriptOperations scriptOps();

	public BulkOperations bulkOps(BulkMode mode, String collectionName);

	public BulkOperations bulkOps(BulkMode mode, Class<?> entityType);

	public BulkOperations bulkOps(BulkMode mode, Class<?> entityType, String collectionName);

	public <T> List<T> findAll(Class<T> entityClass);

	public <T> List<T> findAll(Class<T> entityClass, String collectionName);

	public <T> GroupByResults<T> group(String inputCollectionName, GroupBy groupBy, Class<T> entityClass);

	public <T> GroupByResults<T> group(Criteria criteria, String inputCollectionName, GroupBy groupBy,
			Class<T> entityClass);

	public <O> AggregationResults<O> aggregate(TypedAggregation<?> aggregation, String collectionName,
			Class<O> outputType);

	public <O> AggregationResults<O> aggregate(TypedAggregation<?> aggregation, Class<O> outputType);

	public <O> AggregationResults<O> aggregate(Aggregation aggregation, Class<?> inputType, Class<O> outputType);

	public <O> AggregationResults<O> aggregate(Aggregation aggregation, String collectionName, Class<O> outputType);

	public <T> MapReduceResults<T> mapReduce(String inputCollectionName, String mapFunction, String reduceFunction,
			Class<T> entityClass);

	public <T> MapReduceResults<T> mapReduce(String inputCollectionName, String mapFunction, String reduceFunction,
			MapReduceOptions mapReduceOptions, Class<T> entityClass);

	public <T> MapReduceResults<T> mapReduce(Query query, String inputCollectionName, String mapFunction,
			String reduceFunction, Class<T> entityClass);

	public <T> MapReduceResults<T> mapReduce(Query query, String inputCollectionName, String mapFunction,
			String reduceFunction, MapReduceOptions mapReduceOptions, Class<T> entityClass);

	public <T> GeoResults<T> geoNear(NearQuery near, Class<T> entityClass);

	public <T> GeoResults<T> geoNear(NearQuery near, Class<T> entityClass, String collectionName);

	public <T> T findOne(Query query, Class<T> entityClass);

	public <T> T findOne(Query query, Class<T> entityClass, String collectionName);

	public boolean exists(Query query, String collectionName);

	public boolean exists(Query query, Class<?> entityClass);

	public boolean exists(Query query, Class<?> entityClass, String collectionName);

	public <T> List<T> find(Query query, Class<T> entityClass);

	public <T> List<T> find(Query query, Class<T> entityClass, String collectionName);

	public <T> T findById(Object id, Class<T> entityClass);

	public <T> T findById(Object id, Class<T> entityClass, String collectionName);

	public <T> T findAndModify(Query query, Update update, Class<T> entityClass);

	public <T> T findAndModify(Query query, Update update, Class<T> entityClass, String collectionName);

	public <T> T findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass);

	public <T> T findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass,
			String collectionName);

	public <T> T findAndRemove(Query query, Class<T> entityClass);

	public <T> T findAndRemove(Query query, Class<T> entityClass, String collectionName);

	public long count(Query query, Class<?> entityClass);

	public long count(Query query, String collectionName);

	public long count(Query query, Class<?> entityClass, String collectionName);

	public void insert(Object objectToSave);

	public void insert(Object objectToSave, String collectionName);

	public void insert(Collection<? extends Object> batchToSave, Class<?> entityClass);

	public void insert(Collection<? extends Object> batchToSave, String collectionName);

	public void insertAll(Collection<? extends Object> objectsToSave);

	public void save(Object objectToSave);

	public void save(Object objectToSave, String collectionName);

	public UpdateResult upsert(Query query, Update update, Class<?> entityClass);

	public UpdateResult upsert(Query query, Update update, String collectionName);

	public UpdateResult upsert(Query query, Update update, Class<?> entityClass, String collectionName);

	public UpdateResult updateFirst(Query query, Update update, Class<?> entityClass);

	public UpdateResult updateFirst(Query query, Update update, String collectionName);

	public UpdateResult updateFirst(Query query, Update update, Class<?> entityClass, String collectionName);

	public UpdateResult updateMulti(Query query, Update update, Class<?> entityClass);

	public UpdateResult updateMulti(Query query, Update update, String collectionName);

	public UpdateResult updateMulti(Query query, Update update, Class<?> entityClass, String collectionName);

	public DeleteResult remove(Object object);

	public DeleteResult remove(Object object, String collection);

	public DeleteResult remove(Query query, Class<?> entityClass);

	public DeleteResult remove(Query query, Class<?> entityClass, String collectionName);

	public DeleteResult remove(Query query, String collectionName);

	public <T> List<T> findAllAndRemove(Query query, String collectionName);

	public <T> List<T> findAllAndRemove(Query query, Class<T> entityClass);

	public <T> List<T> findAllAndRemove(Query query, Class<T> entityClass, String collectionName);

	// Builder operation=====

	public <T> T save(DocQueryBuilder<T> builder);

	public <T> T findOne(IMongoQueryBuilder<T> builder);

	public <T> List<T> find(IMongoQueryBuilder<T> builder);

	public <T> List<T> find(IMongoQueryBuilder<T> builder, Class<T> clazz);

	/**
	 * @param builder
	 * @return
	 * 
	 * @see MongoTemplate#upsert(Query,
	 *      org.springframework.data.mongodb.core.query.Update, Class, String)
	 */
	public <T> UpdateResult upsert(IMongoQueryBuilder<T> builder);

	public <T> UpdateResult update(IMongoQueryBuilder<T> builder);

	public <T> UpdateResult updateFirst(IMongoQueryBuilder<T> builder);

	public <T> UpdateResult updateMulti(IMongoQueryBuilder<T> builder);

	public <T> long count(IMongoQueryBuilder<T> builder);

	public <T> T findByIdString(String id, Class<T> clazz);

	public <T> T findByIdSafeCheck(Object id, Class<T> clazz);

	public <T> T findByIdOrDefault(String id, T defaultValue);

	public MongoResultProcessor<Document> collection(String collection);

	public <TResult> MongoResultProcessor<TResult> collection(String collection, Class<TResult> clazz);

	public <TResult> MongoResultProcessor<TResult> collection(Class<TResult> clazz);

	public <T> T removeAndAudit(T objectToSave);

	public <T> T removeAndAudit(String id, Class<T> clazz);

	public <T extends SimpleDocument> UpdateResult patch(ModelPatches patches, Class<T> clazz)
			throws InstantiationException, IllegalAccessException;
}
