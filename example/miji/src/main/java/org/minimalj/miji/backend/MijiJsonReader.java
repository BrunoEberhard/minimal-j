package org.minimalj.miji.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.minimalj.miji.model.Jira.Issue;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MijiJsonReader {

	public static <T> T read(Class<T> clazz, Object input) {
		if (input == null) {
			return null;
		} else if (input instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) input;

			if (map.get("fields") instanceof Map) {
				map.putAll((Map<Object, Object>) map.get("fields"));
			}

			T result = CloneHelper.newInstance(clazz);
			Map<String, PropertyInterface> properties = FlatProperties.getProperties(clazz);
			for (Map.Entry<String, PropertyInterface> p : properties.entrySet()) {
				if (StringUtils.equals(p.getKey(), "id", "key")) {
					p.getValue().setValue(result, map.get(p.getKey()));
				} else {
					Object value = map.get(p.getKey());
					Class<?> fieldClass = p.getValue().getClazz();
					if (FieldUtils.isList(fieldClass)) {
						Class<?> elementClass = p.getValue().getGenericClass();
						value = readList(elementClass, value);
					} else if (!FieldUtils.isAllowedPrimitive(fieldClass)) {
						value = read(fieldClass, value);
					}
					p.getValue().setValue(result, value);
				}
			}

			return result;
		} else {
			throw new IllegalArgumentException(input + "");
		}
	}

	public static <T> List<T> readList(Class<T> elementClass, Object input) {
		if (input == null) {
			return null;
		} else if (input instanceof Collection) {
			Collection<Object> collection = (Collection) input;
			List result = new ArrayList();
			for (Object o : collection) {
				result.add(read(elementClass, o));
			}
			return result;
		} else {
			throw new IllegalArgumentException(input + "");
		}
	}

	public static List<Issue> issues(Object input) {
		List<Issue> issues = new ArrayList<Issue>();
		if (input instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) input;
			Object issuesObject = map.get("issues");
			if (issuesObject instanceof Collection) {
				Collection c = (Collection) issuesObject;
				for (Object i : c) {
					issues.add(read(Issue.class, i));
				}
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
		return issues;
	}

}
