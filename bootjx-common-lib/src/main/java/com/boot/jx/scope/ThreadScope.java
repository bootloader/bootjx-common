package com.boot.jx.scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.boot.utils.ContextUtil;

public class ThreadScope implements Scope {

	public static final ThreadScope SCOPE = new ThreadScope();

	private Map<String, Runnable> destructionCallbacks = Collections.synchronizedMap(new HashMap<String, Runnable>());

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> map = ContextUtil.map();
//		if (!map.containsKey(name)) {
//			map.put(name, objectFactory.getObject());
//		}
//		return map.get(name);
		// System.out.println("get : " + name);
		return map.computeIfAbsent(name, k -> objectFactory.getObject());
	}

	@Override
	public Object remove(String name) {
//		destructionCallbacks.remove(name);
//		return ContextUtil.map().remove(name);
		// System.out.println("remove : " + name);
		Runnable callback = destructionCallbacks.remove(getNameKey(name));
		if (callback != null) {
			callback.run();
		}
		return ContextUtil.map().remove(name);

	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		// System.out.println("registerDestructionCallback : " + name);
		String nameKey = getNameKey(name);
		destructionCallbacks.put(nameKey, callback);
	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

	private String getNameKey(String name) {
		return getConversationId() + ":" + name;
	}

	@Override
	public String getConversationId() {
		String id = ContextUtil.getTraceId(false);
		return id.isEmpty() ? null : id;
	}

	public void clear() {
		String prefix = getConversationId() + ":";
		// System.out.println("destroyCurrentScope : " + prefix);
		destructionCallbacks.entrySet().removeIf(e -> {
			// System.out.println("destroyCurrentScope : " + e.getKey());
			if (e.getKey().startsWith(prefix)) {
				e.getValue().run(); // <-- this calls @PreDestroy
				// System.out.println("destroyCurrentScope : " + e.getKey());
				return true;
			}
			return false;
		});
	}
}