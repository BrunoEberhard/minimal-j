package org.minimalj.util;

import java.util.HashMap;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.model.annotation.Code;
import org.minimalj.transaction.criteria.Criteria;

public class Codes {

	private static HashMap<Class<?>, CodeCacheItem<?>> cache = new HashMap<>();
	
	public static <T> T findCode(Class<T> clazz, String code) {
		return findCode(clazz, (Object) code);
	}

	public static <T> T findCode(Class<T> clazz, Integer code) {
		return findCode(clazz, (Object) code);
	}

	public static <T> T findCode(Class<T> clazz, Object code) {
		List<T> codes = get(clazz);
		return findCode(codes, code);
	}
	
	public static <T> T findCode(List<T> codes, Object value) {
		for (T code : codes) {
			Object codeValue = IdUtils.getId(code);
			if (codeValue == null && value == null || codeValue.equals(value)) {
				return code;
			}
		}
		return null;
	}

	public static boolean isCode(Class<?> clazz) {
		return Code.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	public synchronized static <T> List<T> get(Class<T> clazz) {
		CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.get(clazz);
		if (cacheItem == null || !cacheItem.isValid()) {
			updateCode(clazz);
		}
		cacheItem = (CodeCacheItem<T>) cache.get(clazz);
		List<T> codes = cacheItem.getCodes();
		return codes;
	}
	
	private static <T> void updateCode(Class<T> clazz) {
		List<T> codes = Backend.getInstance().read(clazz, Criteria.all(), Integer.MAX_VALUE);
		CodeCacheItem<T> codeItem = new CodeCacheItem<T>(codes);
		cache.put(clazz, codeItem);
	}
	
	public static class CodeCacheItem<S> {
		private final long timestamp;
		private final List<S> codes;
		
		public CodeCacheItem(List<S> codes) {
			this.codes = codes;
			this.timestamp = System.currentTimeMillis();
		}

		public boolean isValid() {
			return (System.currentTimeMillis() - timestamp) / 1000 < 10;
		}
		
		public List<S> getCodes() {
			return codes;
		}
	}

	public static Object findId(Code value) {
		Object id = IdUtils.getId(value);
		if (id != null) {
			return id;
		}
		List<?> codes = get(value.getClass());
		for (Object code : codes) {
			if (value.equals(code)) {
				return IdUtils.getId(code);
			}
		}
		return null;
	}
	
}
