package com.boot.jx.mongo;

import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

public class CommonMongoCommandListener implements CommandListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonMongoCommandListener.class);

	@Override
	public void commandStarted(CommandStartedEvent event) {
		if (LOGGER.isDebugEnabled()) {
			String command = event.getCommandName();

			String collection = null;

			BsonDocument cmd = event.getCommand();

			if (cmd.containsKey("find"))
				collection = cmd.getString("find").getValue();

			else if (cmd.containsKey("insert"))
				collection = cmd.getString("insert").getValue();

			else if (cmd.containsKey("update"))
				collection = cmd.getString("update").getValue();

			else if (cmd.containsKey("delete"))
				collection = cmd.getString("delete").getValue();

			LOGGER.debug(command + " -> " + collection);
		}
	}

	@Override
	public void commandSucceeded(CommandSucceededEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commandFailed(CommandFailedEvent event) {
		// TODO Auto-generated method stub

	}

}
