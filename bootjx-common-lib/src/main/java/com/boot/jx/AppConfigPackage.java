package com.boot.jx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;

@Component
public class AppConfigPackage implements ApplicationEventPublisherAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigPackage.class);

	private static ApplicationEventPublisher APPLICATION_EVENT_PUBLISHER;

	public interface SharedConfigManager {

		void clear(AppSharedConfigChange change);

		void clear();

	}

	public interface AppCommonConfig {

		public Map<String, Object> configAttributes();

		public Map<String, Object> appAttributes();

	}

	public interface AppSharedConfig {

		default void clear(AppSharedConfigChange change) {
			// DO NOTHING
			LOGGER.info("cleared AppSharedConfig for: {}", name());
		};

		@Deprecated
		default void clear(Map<String, String> data) {
			// DO NOTHING
		};

		default String name() {
			return getClass().getName();
		};

		default Map<String, Object> getExternalConfig(Map<String, Object> config) {
			return config;
		}

		default void publishUpdate() {
			APPLICATION_EVENT_PUBLISHER.publishEvent(SharedConfigChangeBuilder.newChange().name(name()).build());
		}

		default void publishUpdate(AppSharedConfigChange change) {
			change.setName(name());
			APPLICATION_EVENT_PUBLISHER.publishEvent(change);
		}
	}

	public static class AppSharedConfigChange implements Serializable {
		private static final long serialVersionUID = 6496200861213027301L;
		String name;
		String configType;
		String configId;
		Map<String, String> details;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getConfigType() {
			return configType;
		}

		public void setConfigType(String configType) {
			this.configType = configType;
		}

		public String getConfigId() {
			return configId;
		}

		public void setConfigId(String configId) {
			this.configId = configId;
		}

		public Map<String, String> getDetails() {
			return details;
		}

		public void setDetails(Map<String, String> details) {
			this.details = details;
		}

		public Map<String, String> details() {
			if (this.details == null) {
				this.details = new HashMap<String, String>();
			}
			return details;
		}

		public AppSharedConfigChange put(String key, String value) {
			this.details().put(key, value);
			return this;
		}
	}

	public static class SharedConfigChangeBuilder {
		AppSharedConfigChange change;

		public static SharedConfigChangeBuilder newChange() {
			SharedConfigChangeBuilder builder = new SharedConfigChangeBuilder();
			builder.change = new AppSharedConfigChange();
			return builder;
		}

		public SharedConfigChangeBuilder name(String name) {
			this.change.setName(name);
			return this;
		}

		public SharedConfigChangeBuilder type(String type) {
			this.change.setConfigType(type);
			return this;
		}

		public AppSharedConfigChange build() {
			return this.change;
		}
	}

	@Autowired(required = false)
	private List<AppSharedConfig> listAppSharedConfig;

	public void clear(AppSharedConfigChange change) {
		if (ArgUtil.is(listAppSharedConfig)) {
			for (AppSharedConfig appSharedConfig : listAppSharedConfig) {
				if (ArgUtil.is(change) && ArgUtil.is(change.getName())) {
					if (ArgUtil.is(change.getName(), appSharedConfig.name())) {
						appSharedConfig.clear(change);
					}
				} else {
					appSharedConfig.clear(change);
				}
				LOGGER.debug("for class {}", ClazzUtil.getUltimateClassName(appSharedConfig));
			}
		}
	}

	public void clear() {
		if (ArgUtil.is(listAppSharedConfig)) {
			this.clear(new AppSharedConfigChange());
		}
	}

	@Deprecated
	public void clear(Map<String, String> data) {
		if (ArgUtil.is(listAppSharedConfig)) {
			for (AppSharedConfig appSharedConfig : listAppSharedConfig) {
				appSharedConfig.clear(data);
				LOGGER.debug("for class {}", ClazzUtil.getUltimateClassName(appSharedConfig));
			}
		}
	}

	public Map<String, Object> getExternalConfig() {
		Map<String, Object> config = new HashMap<String, Object>();
		if (ArgUtil.is(listAppSharedConfig)) {
			for (AppSharedConfig appSharedConfig : listAppSharedConfig) {
				appSharedConfig.getExternalConfig(config);
			}
		}
		return config;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		APPLICATION_EVENT_PUBLISHER = applicationEventPublisher;
	}

}