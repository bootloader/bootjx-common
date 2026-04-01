package com.boot.jx.logger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerService {

	public static Logger getLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}

	private static class LogEntry {
		String label;
		long duration;

		LogEntry(String label, long duration) {
			this.label = label;
			this.duration = duration;
		}
	}

	public static class LogTimer {

		private long lastTime;
		private final List<LogEntry> logs = new ArrayList<>();

		public LogTimer() {
			this.lastTime = System.currentTimeMillis();
		}

		public void log(String label) {
			long now = System.currentTimeMillis();
			long diff = now - lastTime;

			logs.add(new LogEntry(label, diff));
			lastTime = now;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (LogEntry entry : logs) {
				sb.append(entry.label).append(" : ").append(entry.duration).append(" ms\n");
			}

			return sb.toString();
		}
	}

	public static LogTimer getTimer() {
		return new LogTimer();
	}

}
