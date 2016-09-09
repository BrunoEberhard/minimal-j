package org.minimalj.util;

import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.minimalj.backend.Backend;
import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.persistence.criteria.By;

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
		List<T> codes = cacheItem.getCodes(LocaleContext.getCurrent());
		return codes;
	}

	private static class CodeComparator implements Comparator<Code> {
		private final Collator collator;
		
		public CodeComparator(Locale locale) {
			this.collator = Collator.getInstance(LocaleContext.getCurrent());
		}
		
		@Override
		public int compare(Code o1, Code o2) {
			String string1 = Rendering.render(o1, Rendering.RenderType.PLAIN_TEXT);
			String string2 = Rendering.render(o2, Rendering.RenderType.PLAIN_TEXT);
			return collator.compare(string1, string2);
		}
	}
	
	private static <T extends Code> void updateCode(Class<T> clazz) {
		CodeCacheItem<T> codeItem = new CodeCacheItem<T>();
		cache.put(clazz, codeItem);
		List<T> codes = Backend.read(clazz, By.all(), Integer.MAX_VALUE);
		codeItem.setCodes(codes);
	}
	
	public static class CodeCacheItem<S extends Code> {
		private final long timestamp;
		private List<S> codes;
		private Map<Locale, List<S>> codesSortedByLocale;
		
		public CodeCacheItem() {
			this.timestamp = System.currentTimeMillis();
		}

		public boolean isValid() {
			return (System.currentTimeMillis() - timestamp) / 1000 < 10;
		}
		
		public List<S> getCodes() {
			return codes;
		}

		public List<S> getCodes(Locale locale) {
			if (codes != null) {
				if (!codesSortedByLocale.containsKey(locale)) {
					List<S> sortedCodes = new ArrayList<>(codes);
					Collections.sort(sortedCodes, new CodeComparator(locale));
					codesSortedByLocale.put(locale, sortedCodes);
				}
				return codesSortedByLocale.get(locale);
			} else {
				return null;
			}
		}

		public boolean isLoading() {
			return codes == null;
		}

		public void setCodes(List<S> codes) {
			this.codes = codes;
			codesSortedByLocale = new HashMap<>();
		}
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
