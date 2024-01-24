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
import java.util.Objects;
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

	private static CodeCache codeCache;

	public static void setCache(CodeCache codeCache) {
		Objects.requireNonNull(codeCache);
		if (Codes.codeCache != null && Codes.codeCache != codeCache) {
			throw new IllegalStateException("Not allowed to change instance of " + CodeCache.class.getSimpleName());
		}
		Codes.codeCache = codeCache;
	}

	public static CodeCache getCache() {
		if (Codes.codeCache == null) {
			Codes.codeCache = DefaultCodeCache.instance;
		}
		return Codes.codeCache;
	}

	//

	public static <T extends Code> T get(Class<T> clazz, String codeId) {
		return get(clazz, (Object) codeId);
	}

	public static <T extends Code> T get(Class<T> clazz, Integer codeId) {
		return get(clazz, (Object) codeId);
	}

	public static <T extends Code> T get(Class<T> clazz, Object codeId) {
		return getCache().getCacheItems(clazz).getCode(codeId);
	}

	public static <T extends Code> List<T> get(Class<T> clazz) {
		return getCache().getCacheItems(clazz).getCodes(LocaleContext.getCurrent());
	}

	public static <T extends Code> T getOrInstantiate(Class<T> clazz, Object id) {
		return getCache().getOrInstantiate(clazz, id);
	}

	public static void invalidateCodeCache(Class<? extends Object> clazz) {
		getCache().invalidateCodeCache(clazz);
	}

	//

	public interface CodeCache {

		public <T extends Code> CodeCacheItem<T> getCacheItems(Class<T> clazz);

		public <T extends Code> T getOrInstantiate(Class<T> clazz, Object id);

		public void invalidateCodeCache(Class<? extends Object> clazz);

	}

	private static enum DefaultCodeCache implements CodeCache {
		instance;

		private static Map<Class<?>, CodeCacheItem<?>> cache = Collections.synchronizedMap(new HashMap<>());

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Code> CodeCacheItem<T> getCacheItems(Class<T> clazz) {
			synchronized (clazz) {
				CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.get(clazz);
				if (cacheItem == null || !cacheItem.isValid()) {
					cacheItem = new CodeCacheItem<>();
					cache.put(clazz, cacheItem);
					List<T> codes = Backend.execute(new ReadCodesTransaction<>(clazz), true);
					cacheItem.setCodes(codes);
				}
				return cacheItem;
			}
		}

		public <T extends Code> T getOrInstantiate(Class<T> clazz, Object id) {
			@SuppressWarnings("unchecked")
			CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) cache.computeIfAbsent(clazz, newClazz -> new CodeCacheItem<>());
			return cacheItem.getOrInstantiate(clazz, id);
		}

		public void invalidateCodeCache(Class<? extends Object> clazz) {
			cache.remove(clazz);
		}
	}

	//

	private static class CodeComparator implements Comparator<Code> {
		private final Collator collator;

		public CodeComparator(Locale locale) {
			this.collator = Collator.getInstance(LocaleContext.getCurrent());
		}

		@Override
		public int compare(Code o1, Code o2) {
			String string1 = Rendering.toString(o1);
			String string2 = Rendering.toString(o2);
			return collator.compare(StringUtils.emptyIfNull(string1), StringUtils.emptyIfNull(string2));
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
		
		@Override
		public String toString() {
			return "Read codes " + getEntityClazz().getSimpleName();
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
				codesSortedByLocale = new HashMap<>();
				return code;
			});
		}

		public void setCodes(List<S> codes) {
			this.codeMap = codes.stream().collect(Collectors.toMap(IdUtils::getId, Function.identity()));
			codesSortedByLocale = new HashMap<>();
			timestamp = System.currentTimeMillis();
		}
	}

	// static helper methods

	public static <T extends Code> List<T> getConstants(Class<T> clazz) {
		List<T> constants = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (!FieldUtils.isStatic(field))
				continue;
			if (!FieldUtils.isFinal(field))
				continue;
			if (field.getType() != clazz)
				continue;
			T constant = FieldUtils.getStaticValue(field);
			if (Keys.isKeyObject(constant))
				continue;
			constants.add(constant);
		}
		return constants;
	}

	public static boolean isCode(Class<?> clazz) {
		return Code.class.isAssignableFrom(clazz);
	}
}
