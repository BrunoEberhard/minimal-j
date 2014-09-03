package org.minimalj.util;

import java.util.HashMap;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.criteria.Criteria;

public class Codes {

	public static HashMap<Class<?>, CodeCacheItem<?>> cache = new HashMap<>();
	
	public static <T> T findCode(Class<T> clazz, Object code) {
		List<T> codes = get(clazz);
		return CodeUtils.findCode(codes, code);
	}

	@SuppressWarnings("unchecked")
	public synchronized static <T> List<T> get(Class<T> clazz) {
		CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.get(clazz);
		if (cacheItem == null || cacheItem.getTimestamp() - System.currentTimeMillis() > 10) {
			updateCode(clazz);
		}
		cacheItem = (CodeCacheItem<T>) cache.get(clazz);
		List<T> codes = cacheItem.getCodes();
		return codes;
	}
	
	private static <T> void updateCode(Class<T> clazz) {
		List<T> codes = Backend.getInstance().read(clazz, Criteria.all(), Integer.MAX_VALUE);
		CodeCacheItem<T> codeItem = new CodeCacheItem<T>(codes, System.currentTimeMillis());
		cache.put(clazz, codeItem);
	}
	
	private static class CodeCacheItem<S> {
		private final long timestamp;
		private final List<S> codes;
		
		public CodeCacheItem(List<S> codes, long timestamp) {
			this.codes = codes;
			this.timestamp = timestamp;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public List<S> getCodes() {
			return codes;
		}
	}
	
}
