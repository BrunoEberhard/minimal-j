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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.minimalj.backend.Backend;
import org.minimalj.backend.repository.ReadTransaction;
import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.repository.query.By;
import org.minimalj.transaction.Isolation;
import org.minimalj.transaction.Isolation.Level;

public class Codes {

	private static Map<Class<?>, CodeCacheItem<?>> cache = Collections.synchronizedMap(new HashMap<>());
	
	public static <T extends Code> T findCode(Class<T> clazz, String codeId) {
		return findCode(clazz, (Object) codeId);
	}

	public static <T extends Code> T findCode(Class<T> clazz, Integer codeId) {
		return findCode(clazz, (Object) codeId);
	}

	public static <T extends Code> T findCode(Class<T> clazz, Object codeId) {
		return getCache(clazz).getCode(codeId);
	}

	public static boolean isCode(Class<?> clazz) {
		return Code.class.isAssignableFrom(clazz);
	}

	public static <T extends Code> List<T> get(Class<T> clazz) {
		return getCache(clazz).getCodes(LocaleContext.getCurrent());
	}

	@SuppressWarnings("unchecked")
	private static <T extends Code> CodeCacheItem<T> getCache(Class<T> clazz) {
		synchronized(clazz) {
			CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.get(clazz);
			if (cacheItem == null || !cacheItem.isValid()) {
				CodeCacheItem<T> codeItem = new CodeCacheItem<>();
				cache.put(clazz, codeItem);
				List<T> codes = Backend.execute(new ReadCodesTransaction<T>(clazz));
				codeItem.setCodes(codes);
			}
			return (CodeCacheItem<T>) cache.get(clazz);
		}
	}
	
	private static class CodeComparator implements Comparator<Code> {
		private final Collator collator;
		
		public CodeComparator(Locale locale) {
			this.collator = Collator.getInstance(LocaleContext.getCurrent());
		}
		
		@Override
		public int compare(Code o1, Code o2) {
			String string1 = Rendering.toString(o1);
			String string2 = Rendering.toString(o2);
			return collator.compare(string1, string2);
		}
	}
	

	@Isolation(Level.NONE)
	public static class ReadCodesTransaction<T> extends ReadTransaction<T, List<T>> {
		private static final long serialVersionUID = 1L;

		public ReadCodesTransaction(Class<T> clazz) {
			super(clazz);
		}

		@Override
		public List<T> execute() {
			return repository().find(getEntityClazz(), By.ALL);
		}
	}
	
	public static <T extends Code> T getOrInstantiate(Class<T> clazz, Object id) {
		synchronized(clazz) {
			CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.computeIfAbsent(clazz, newClazz -> new CodeCacheItem<>());
			return cacheItem.getOrInstantiate(clazz, id);
		}

	}
	
	public static class CodeCacheItem<S extends Code> {
		private Long timestamp;
		private Map<Object, S> codeMap = new HashMap<>(100);
		private Map<Locale, List<S>> codesSortedByLocale = new HashMap<>();
		
		public boolean isValid() {
			return timestamp == null || (System.currentTimeMillis() - timestamp) / 1000 < 10;
		}
		
		public List<S> getCodes(Locale locale) {
			synchronized (codesSortedByLocale) {
				return codesSortedByLocale.computeIfAbsent(locale, newLocacle -> {
					List<S> sortedCodes = new ArrayList<>(codeMap.values());
					Collections.sort(sortedCodes, new CodeComparator(newLocacle));
					return sortedCodes;
					
				});
			}
		}

		public S getCode(Object id) {
			return codeMap.get(id);
		}
		
		public S getOrInstantiate(Class<S> clazz, Object id) {
			return codeMap.computeIfAbsent(id, newId -> {
				S code = CloneHelper.newInstance(clazz);
				IdUtils.setId(code, newId);
				return code;
			});
		}
		
		public void setCodes(List<S> codes) {
			this.codeMap = codes.stream().collect(Collectors.toMap(IdUtils::getId, Function.identity()));
			codesSortedByLocale = new HashMap<>();
			timestamp = System.currentTimeMillis();
		}
	}

	public static <T extends Code> List<T> getConstants(Class<T> clazz) {
		List<T> constants = new ArrayList<>();
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

	public static void invalidateCodeCache(Class<? extends Object> clazz) {
		cache.remove(clazz);
	}

	
}
