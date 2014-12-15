package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.transaction.criteria.Criteria;

public class Codes {

	private static HashMap<Class<?>, CodeCacheItem<?>> cache = new HashMap<>();
	
	public static <T extends Code> T findCode(Class<T> clazz, String codeId) {
		return findCode(clazz, (Object) codeId);
	}

	public static <T extends Code> T findCode(Class<T> clazz, Integer codeId) {
		return findCode(clazz, (Object) codeId);
	}

	public static <T extends Code> T findCode(Class<T> clazz, Object codeId) {
		List<T> codes = get(clazz);
		return findCode(codes, codeId);
	}
	
	public static <T extends Code> T findCode(List<T> codes, Object codeId) {
		for (T code : codes) {
			Object aCodeId = IdUtils.getId(code);
			if (aCodeId == null && codeId == null || aCodeId.equals(codeId)) {
				return code;
			}
		}
		return null;
	}

	public static boolean isCode(Class<?> clazz) {
		return Code.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	public synchronized static <T extends Code> List<T> get(Class<T> clazz) {
		CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.get(clazz);
		if (cacheItem == null || !cacheItem.isValid()) {
			updateCode(clazz);
		}
		cacheItem = (CodeCacheItem<T>) cache.get(clazz);
		List<T> codes = cacheItem.getCodes();
		return codes;
	}
	
	private static <T> void updateCode(Class<T> clazz) {
		CodeCacheItem<T> codeItem = new CodeCacheItem<T>();
		cache.put(clazz, codeItem);
		List<T> codes = Backend.getInstance().read(clazz, Criteria.all(), Integer.MAX_VALUE);
		codeItem.setCodes(codes);
	}
	
	public static class CodeCacheItem<S> {
		private final long timestamp;
		private List<S> codes;
		
		public CodeCacheItem() {
			this.timestamp = System.currentTimeMillis();
		}

		public boolean isValid() {
			return (System.currentTimeMillis() - timestamp) / 1000 < 10;
		}
		
		public List<S> getCodes() {
			return codes;
		}

		public boolean isLoading() {
			return codes == null;
		}

		public void setCodes(List<S> codes) {
			this.codes = codes;
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

	public static <T extends Code> List<T> getConstants(Class<T> clazz) {
		List<T> constants = new ArrayList<T>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!FieldUtils.isStatic(field)) continue;
			if (!FieldUtils.isFinal(field)) continue;
			if (field.getType() != clazz) continue;
			T constant = FieldUtils.getStaticValue(field);
			if (Keys.isKeyObject(constant)) continue;
			constants.add(constant);
		}
		return constants;
	}
	
}
