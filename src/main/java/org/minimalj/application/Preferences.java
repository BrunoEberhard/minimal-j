package org.minimalj.application;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.security.Subject;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.CloneHelper;

public abstract class Preferences {
	private static Map<Class<?>, Map<Subject, Object>> preferencesCache = new HashMap<>();
	
	private static Map<Subject, Object> getPreferencesCache(Class<?> clazz) {
		if (!preferencesCache.containsKey(clazz)) {
			preferencesCache.put(clazz, new HashMap<>());
		}
		return preferencesCache.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPreferences(Class<T> clazz) {
		Subject subject = Frontend.getInstance().getSubject();
		Map<Subject, Object> preferencesCache = getPreferencesCache(clazz);
		if (!preferencesCache.containsKey(subject)) {
			Object preferences = loadPreferences(clazz);
			preferencesCache.put(subject, preferences);
		}
		return (T) preferencesCache.get(subject);
	}
	
	private static <T> T loadPreferences(Class<T> clazz) {
		return Backend.getInstance().execute(new LoadPreferencesTransaction<T>(clazz));
	}

	public static void savePreferences(Object preferences) {
		// TODO return Backend.getInstance().execute(new SavePreferencesTransaction(preferences));
		// clear cache to force reload on next access
		getPreferencesCache(preferences.getClass()).remove(Frontend.getInstance().getSubject());
	}

	private static class LoadPreferencesTransaction<T> implements Transaction<T> {
		private static final long serialVersionUID = 1L;
		private final Class<T> clazz;
		
		public LoadPreferencesTransaction(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public T execute() {
			// String subject = Subject.get().getUser();	
			// TODO Backend.read(clazz, criteria, maxResults)
			return CloneHelper.newInstance(clazz);
		}
	}
	
}
