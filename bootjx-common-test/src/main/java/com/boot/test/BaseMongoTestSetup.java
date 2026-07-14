package com.boot.test;

import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.boot.jx.mongo.CommonMongoSourceProvider;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.utils.ArgUtil;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

public abstract class BaseMongoTestSetup { // Noncompliant

	/** Non-default port to avoid clashing with a locally running MongoDB instance. */
	private static final int PORT = 27018;

	private static MongodExecutable mongodExecutable;

	protected static CommonMongoTemplate mongoTemplate;

	private static volatile boolean embeddedMongoUnavailable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (embeddedMongoUnavailable) {
			assumeTrue("Embedded MongoDB unavailable on this platform", false);
		}

		String userDir = System.getProperty("user.dir");
		File userDirFile = new File(userDir);;
		File userDirFolder = userDirFile.getParentFile();
		System.out.println("userDirFolder:" + userDirFolder);
		System.setProperty("javax.net.ssl.trustStore", userDirFolder + "/../certs/cacerts");

		try {
			MongodStarter starter = MongodStarter.getDefaultInstance();
			// V3_6 avoids the PRODUCTION 32-bit binary issue on macOS ARM (flapdoodle 2.0.3).
			MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.V3_6)
					.net(new Net(PORT, false))
					.build();
			mongodExecutable = starter.prepare(mongodConfig);
			mongodExecutable.start();

			System.setProperty("spring.data.mongodb.uri", "mongodb://localhost:" + PORT + "/testdatabase");
			System.out.println("Embedded MongoDB started on port: " + PORT);
		} catch (Exception e) {
			embeddedMongoUnavailable = true;
			System.err.println("Embedded MongoDB could not start: " + e.getMessage());
			assumeTrue("Embedded MongoDB unavailable on this platform", false);
		}

	}

	@Before
	public void ensureMongoConnection() {
		if (embeddedMongoUnavailable) {
			assumeTrue("Embedded MongoDB unavailable on this platform", false);
		}
		String connectionString = System.getProperty("spring.data.mongodb.uris");
		if (!ArgUtil.is(connectionString)) {
			connectionString = "mongodb://localhost:" + PORT + "/testdatabase";
		}

		if (mongoTemplate == null) {
			System.out.println("connecting " + connectionString);
			CommonMongoSourceProvider commonMongoSourceProvider = new CommonMongoSourceProvider();
			commonMongoSourceProvider.setDataSourceUrl(connectionString);
			commonMongoSourceProvider.setGlobalDataSourceUrl(connectionString);
			commonMongoSourceProvider.setGlobalDBProfix("tnt");
			mongoTemplate = new CommonMongoTemplate().using(commonMongoSourceProvider);
		}
	}

	@After
	public void tearDown() throws Exception {
		if (mongoTemplate != null) {
			mongoTemplate = null;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (mongodExecutable != null) {
			mongodExecutable.stop();
			mongodExecutable = null;
		}
	}
}
