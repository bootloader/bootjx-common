package com.boot.jx.logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerService {

	public static Logger getLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}

	public static boolean isLocalDebug() {
		return false;
	}

	public static class LogEntry implements Serializable {

		private static final long serialVersionUID = 285769445887570214L;

		String label;
		long duration;

		LogEntry(String label, long duration) {
			this.label = label;
			this.duration = duration;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public long getDuration() {
			return duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}
	}

	public static class LogTimer implements Serializable {

		private static final Logger LOGGER = LoggerFactory.getLogger(LogEntry.class);

		public static boolean isLocal() {
			return LOGGER.isDebugEnabled() || isLocalDebug() || false;
		}

		private static final long serialVersionUID = 1L;
		protected long lastTime;
		protected List<LogEntry> logs = new ArrayList<>();

		public LogTimer() {
			this.lastTime = System.currentTimeMillis();
		}

		public void log(String label) {
			long now = System.currentTimeMillis();
			long diff = now - lastTime;

			logs.add(new LogEntry(label, diff));
			lastTime = now;
			if (isLocal())
				LOGGER.info("TIMER: {} {}", label, diff);
		}

		public long getLastTime() {
			return lastTime;
		}

		public void setLastTime(long lastTime) {
			this.lastTime = lastTime;
		}

		public List<LogEntry> getLogs() {
			return logs;
		}

		public void setLogs(List<LogEntry> logs) {
			this.logs = logs;
		}
	}

	public static class LogTimerPrintable extends LogTimer {

		private static final long serialVersionUID = -8990178452772943749L;

		public LogTimerPrintable() {
			super();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (LogEntry entry : this.logs) {
				sb.append(entry.label).append(" : ").append(entry.duration).append(" ms\n");
			}

			return sb.toString();
		}
	}

	public static LogTimer getTimer() {
		return new LogTimerPrintable();
	}

}
