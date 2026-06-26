package com.boot.jx.mongo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.logger.LoggerService;
import com.boot.utils.ArgUtil;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

public class CommonMongoCommandListener implements CommandListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonMongoCommandListener.class);
	private final ConcurrentHashMap<Integer, CommandData> timings = new ConcurrentHashMap<>();

	public static boolean isLocal() {
		return LOGGER.isDebugEnabled() || LoggerService.isLocalDebug() || false;
	}

	public static class CommandData {
		protected Long startTime;
		protected String collection;
	}

	private String getCollection(CommandStartedEvent event) {
		BsonDocument cmd = event.getCommand();
		String command = event.getCommandName();

		String collection = null;

		if (cmd.containsKey("find"))
			collection = cmd.getString("find").getValue();

		else if (cmd.containsKey("insert"))
			collection = cmd.getString("insert").getValue();

		else if (cmd.containsKey("update"))
			collection = cmd.getString("update").getValue();

		else if (cmd.containsKey("delete"))
			collection = cmd.getString("delete").getValue();
		else if (cmd.containsKey("aggregate"))
			collection = cmd.getString("aggregate").getValue();
		else {
			BsonValue cmdStr = cmd.get(command);
			if (ArgUtil.is(cmdStr) && cmdStr.getBsonType() == BsonType.STRING) {
				collection = cmd.getString(command).getValue();
			}
		}
		return collection;
	}

	@Override
	public void commandStarted(CommandStartedEvent event) {
		if (isLocal()) {
			CommandData d = new CommandData();
			d.startTime = System.nanoTime();
			d.collection = getCollection(event);
			if (ArgUtil.is(d.collection)) {
				timings.put(event.getRequestId(), d);
			}
		}
	}

	@Override
	public void commandSucceeded(CommandSucceededEvent event) {
		if (isLocal()) {
			CommandData data = timings.remove(event.getRequestId());
			if (data == null) {
				return;
			}
			if (data.startTime == null) {
				return;
			}
			long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - data.startTime);
			String command = event.getCommandName();

			if (ArgUtil.is(data.collection) && ArgUtil.is(command)) {
				LOGGER.info("{} [{}] : {}", command, data.collection, millis);
			}
		}
	}

	@Override
	public void commandFailed(CommandFailedEvent event) {
		// TODO Auto-generated method stub

	}

}
