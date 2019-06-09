package org.minimalj.rest;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.model.Code;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.FieldUtils;
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
			convertedList.add(convert(clazz, (Map<String, Object>) item));
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

	private static <T> T convert(Class<T> clazz, Map<String, Object> values) {
		T entity = CloneHelper.newInstance(clazz);
		return convert(entity, values);
	}
	
	private static <T> T convert(T entity, Map<String, Object> values) {
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(entity.getClass());
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			PropertyInterface property = properties.get(entry.getKey());
			if (property == null) {
				continue;
			}
			Object value = entry.getValue();
			if (property.getClazz() == List.class) {
				List list = (List) value;
				value = convertList(property.getGenericClass(), list);
				property.setValue(entity, value);
			} else if (property.getClazz() == Set.class) {
				Set set = (Set) property.getValue(entity);
				convertEnumSet(set, property.getGenericClass(), (List) value);
			} else if (value instanceof String) {
				String string = (String) value;
				if (!"id".equals(property.getName()) || property.getClazz() != Object.class) {
					Class<?> propertyClazz = property.getClazz();
					if (propertyClazz != String.class) {
						if (Code.class.isAssignableFrom(propertyClazz)) {
							value = Codes.findCode((Class) propertyClazz, value);
						} else {
							value = FieldUtils.parse(string, propertyClazz);
						}
					}
				}
				property.setValue(entity, value);
			} else if (value instanceof Double) {
				if (property.getClazz() == BigDecimal.class) {
					property.setValue(entity, BigDecimal.valueOf((Double) value));
				} else if (property.getClazz() == Long.class) {
					property.setValue(entity, ((Double) value).longValue());
				} else if (property.getClazz() == Integer.class) {
					property.setValue(entity, ((Double) value).intValue());
				}
			} else if (value instanceof Long) {
				// Integer.Type for version
				if (property.getClazz() == Integer.class || property.getClazz() == Integer.TYPE) {
					property.setValue(entity, ((Long) value).intValue());
				} else if (property.getClazz() == Long.class) {
					property.setValue(entity, value);
				} else if (property.getClazz() == BigDecimal.class) {
					property.setValue(entity, BigDecimal.valueOf((Long) value));
				}   		
			} else if (value instanceof Boolean) {
				property.setValue(entity, value);
			} else if (value instanceof Map) {
				Map map = (Map) value;
				Class c2 = property.getClazz();
				value = convert(c2, map);
				property.setValue(entity, value);
			}
		}
		return entity;
	}
	
}
