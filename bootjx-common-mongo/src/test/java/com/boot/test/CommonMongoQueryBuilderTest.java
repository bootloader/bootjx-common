package com.boot.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.QA;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;

/**
 * Regression coverage for Mongo query/update builders — pins BSON generation
 * behavior across Spring Data MongoDB and driver upgrades.
 */
public class CommonMongoQueryBuilderTest {

	@Test
	public void commonMongoQueryBuilder_matchesManualQueryAndUpdate() {
		List<String> ids = CollectionUtil.asList("1", "2");
		String status = "QUEUD";
		String reason = "Faltoo";

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
		builder.set("status", status);
		builder.set("stamps." + status, 1L);
		if (reason != null) {
			builder.update().push("logs", reason);
		}
		builder.where("_id").in(ids);

		Update expectedUpdate = new Update().set("status", status).set("stamps." + status, 1L).push("logs", reason);
		Query expectedQuery = Query.query(Criteria.where("_id").in(ids));

		assertNotNull(builder.build().query());
		assertNotNull(builder.build().update());
		assertTrue(builder.build().query().toString().contains("_id"));
		assertTrue(builder.build().update().toString().contains("status"));
		assertTrue(expectedQuery.toString().contains("_id"));
		assertTrue(expectedUpdate.toString().contains("status"));
	}

	@Test
	public void mongoQueryBuilder_buildsRegexCriteriaQuery() {
		MongoQueryBuilder<CommonMongoQueryBuilderTest> qa = CommonMongoQueryBuilder
				.collection(CommonMongoQueryBuilderTest.class).where(Criteria.where("category").regex("^test$", "i"));
		String query = qa.query().toString();
		assertTrue(query.contains("category"));
		assertTrue(query.contains("test"));
	}

	@Test
	public void qa_aggregationPipeline_serializesToJsonArray() {
		QA list = new QA().add(
				org.springframework.data.mongodb.core.aggregation.Aggregation
						.match(Criteria.where("bulkSessionId").is("XXXXXX")),
				org.springframework.data.mongodb.core.aggregation.Aggregation.group("status").count().as("count"));
		String json = JsonUtil.toJson(list.piplines());
		assertNotNull(json);
		assertTrue(json.contains("bulkSessionId"));
		assertTrue(json.contains("XXXXXX"));
	}

	@Test
	public void qa_projectAndUnwindPipeline_containsExpectedStages() {
		QA list = new QA()
				.add(org.springframework.data.mongodb.core.aggregation.Aggregation
						.match(Criteria.where("bulkSessionId").is("BULK_SESSION_ID")),
						QA.project().append("statuss", QA.objectToArray("stamps"))
								.append("firstLog", QA.arrayElemAt("log", 0)).build(),
						org.springframework.data.mongodb.core.aggregation.Aggregation.unwind("statuss"),
						org.springframework.data.mongodb.core.aggregation.Aggregation.group("statuss.k").count()
								.as("count"));
		String json = JsonUtil.toJson(list.piplines());
		assertTrue(json.contains("$project"));
		assertTrue(json.contains("$unwind"));
		assertTrue(json.contains("$group"));
	}
}
