package org.minimalj.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;

public class EntityJsonReader extends JsonReader {

	public <T> T read(Class<T> clazz, String input) {
		if (StringUtils.isEmpty(input)) {
			return null;
		}
		Map<String, Object> values = (Map<String, Object>) super.read(input);
		return convert(clazz, values);
	}
	
	public <T> T read(Class<T> clazz, InputStream inputStream) {
		Map<String, Object> values = (Map<String, Object>) super.read(inputStream);
		return convert(clazz, values);
	}

	private <T> List<T> convertList(Class<T> clazz, List list) {
		List convertedList = new ArrayList<>();
		for (Object item : list) {
			convertedList.add(convert(clazz, (Map<String, Object>) item));
		}
		return convertedList;
	}

	private <T extends Enum> void convertEnumSet(Set<T> set, Class<T> clazz, List list) {
		set.clear();
		for (Object item : list) {
			set.add(Enum.valueOf(clazz, (String) item));
		}
	}

	private <T> T convert(Class<T> clazz, Map<String, Object> values) {
		T entity = CloneHelper.newInstance(clazz);

		Map<String, PropertyInterface> properties = FlatProperties.getProperties(clazz);
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			PropertyInterface property = properties.get(entry.getKey());
			if (property == null) {
				continue;
			}
			Object value = entry.getValue();
			if (property.getClazz() == List.class) {
				List list = (List) values;
				value = convertList((Class) property.getType(), list);
			} else if (property.getClazz() == Set.class) {
				Set set = (Set) property.getValue(entity);
				convertEnumSet(set, (Class) property.getType(), (List)value);
			} else if (value instanceof String) {
				String string = (String) value;
				if ("version".equals(property.getName())) {
					value = Integer.parseInt(string);
				} else if (!"id".equals(property.getName()) || property.getClazz() != Object.class) {
					Class<?> propertyClazz = property.getClazz();
					if (propertyClazz != String.class) {
						value = FieldUtils.parse(string, propertyClazz);
					}
				}
				property.setValue(entity, value);
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
