package org.minimalj.rest;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.model.properties.FieldProperty;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.rest.openapi.model.OpenAPI.StringValue;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EntityJsonReader {

	public static <T> T read(Class<T> clazz, String input) {
		if (StringUtils.isEmpty(input)) {
			return null;
		}
		Map<String, Object> values = (Map<String, Object>) JsonReader.read(input);
		return convert(clazz, values);
	}

	public static <T> T read(Class<T> clazz, InputStream inputStream) {
		Map<String, Object> values = (Map<String, Object>) JsonReader.read(inputStream);
		return convert(clazz, values);
	}

	public static <T> T read(T entity, String input) {
		if (StringUtils.isEmpty(input)) {
			return entity;
		}
		Map<String, Object> values = (Map<String, Object>) JsonReader.read(input);
		return convert(entity, values);
	}

	public static <T> T read(T entity, InputStream inputStream) {
		Map<String, Object> values = (Map<String, Object>) JsonReader.read(inputStream);
		return convert(entity, values);
	}

	private static <T> List<T> convertList(Class<T> clazz, List list) {
		List convertedList = new ArrayList<>();
		for (Object item : list) {
			convertedList.add(convert(clazz, item));
		}
		return convertedList;
	}

	// only works with eclipse compiler
	// private static <T extends Enum> void convertEnumSet(Set<T> set, Class<T>
	// clazz, List list) {
	private static void convertEnumSet(Set set, Class clazz, List list) {
		set.clear();
		for (Object item : list) {
			set.add(Enum.valueOf(clazz, (String) item));
		}
	}

	private static <T> T convert(Class<T> clazz, Object values) {
		if (clazz == StringValue.class) {
			var stringValue = new StringValue();
			stringValue.value = (String) values;
			return (T) stringValue;
		} else if (clazz == String.class) {
			return (T) values;
		} else {
			return convert(clazz, (Map<String, Object>) values);
		}
	}

	private static <T> T convert(Class<T> clazz, Map<String, Object> values) {
		T entity = clazz == Map.class ? (T) new LinkedHashMap() : CloneHelper.newInstance(clazz);
		return convert(entity, values);
	}

	private static Map convert(List<Object> genericClasses, Map input) {
		if (genericClasses.get(1) instanceof List innerGenericClasses) {
			return ((Map<String, Map>) input).entrySet().stream() //
					.collect(Collectors.toMap(e -> (String) e.getKey(), e -> convert(innerGenericClasses, e.getValue())));
		} else if (genericClasses.get(1) instanceof Class clazz) {
			return ((Map<String, Map<String, Map>>) input).entrySet().stream() //
					.collect(Collectors.toMap(e -> (String) e.getKey(), e -> convert(clazz, e.getValue())));
		} else {
			throw new IllegalArgumentException("" + genericClasses);
		}
	}

	private static <T> T convert(T entity, Map<String, Object> values) {
		Map<String, Property> properties = FlatProperties.getProperties(entity.getClass());
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key.equals("enum")) {
				key = "eNum";
			}
			Property property = properties.get(key);
			if (property == null) {
				// FlatProperties doesn't support MethodProperties
				property = Properties.getMethodProperty(entity.getClass(), key);
				if (property == null) {
					System.out.println("Not found: " + entity.getClass().getSimpleName() + "." + key + " for " + value);
					continue;
				}
			}
			var propertyClazz = property.getClazz();
			if (IdUtils.hasId(propertyClazz)) {
				if (value instanceof Map map) {
					value = convert(propertyClazz, map);
					property.setValue(entity, value);
				} else {
					var idOnly = CloneHelper.newInstance(propertyClazz);
					value = IdUtils.convertId(propertyClazz, value);
					IdUtils.setId(idOnly, value);
					property.setValue(entity, idOnly);
				}
			} else if (propertyClazz == List.class) {
				if (!(value instanceof List)) {
					System.out.println(property.getDeclaringClass().getSimpleName() + "." + property.getName() + " must be list, but is " + value);
				}
				List list = (List) value;
				value = convertList(property.getGenericClass(), list);
				property.setValue(entity, value);
			} else if (propertyClazz == Set.class) {
				Set set = (Set) property.getValue(entity);
				convertEnumSet(set, property.getGenericClass(), (List) value);
			} else if (value instanceof String s) {
				value = FieldUtils.parse(s, propertyClazz);
				property.setValue(entity, value);
			} else if (value instanceof Double d) {
				if (propertyClazz == BigDecimal.class) {
					property.setValue(entity, BigDecimal.valueOf(d));
				} else if (propertyClazz == Long.class) {
					property.setValue(entity, d.longValue());
				} else if (propertyClazz == Integer.class) {
					property.setValue(entity, d.intValue());
				}
			} else if (value instanceof Long l) {
				if (propertyClazz == Integer.class || propertyClazz == Integer.TYPE) {
					property.setValue(entity, l.intValue());
				} else if (propertyClazz == Long.class) {
					property.setValue(entity, value);
				} else if (propertyClazz == BigDecimal.class) {
					property.setValue(entity, BigDecimal.valueOf(l));
				}
			} else if (value instanceof Boolean) {
				property.setValue(entity, value);
			} else if (value instanceof Map map) {
				if (property instanceof FieldProperty fieldProperty) {
					List<Object> genericClasses = GenericUtils.getGenericClasses(fieldProperty.getDeclaringClass(), fieldProperty.getField());
					if (genericClasses != null) {
						// with this Map<String, Map<String, CLAZZ>> works
						value = convert(genericClasses, map);
					} else {
						Class c2 = propertyClazz;
						value = convert(c2, map);
					}
				}
				property.setValue(entity, value);
			}
		}
		return entity;
	}

}
