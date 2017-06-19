package org.minimalj.backend.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class EntityJsonReader extends JsonReader {

	public Object read(String input) {
		Map<String, Object> values = (Map<String, Object>) super.read(input);
		
		return null;
	}
	
	
//	private List<Map<String, Object>> convert(Object entity) {
	private Map<String, Object> convert(Object entity, Set<String> ids) {
		Map<String, Object> values = new HashMap<>();
		
		Map<String, PropertyInterface> properties = Properties.getProperties(entity.getClass());
		for (Map.Entry<String, PropertyInterface> e : properties.entrySet()) {
			PropertyInterface property = e.getValue();
			Object value = property.getValue(entity);
			
			if (value == null) {
				values.put(e.getKey(), null);
			} else if (StringUtils.equals(e.getKey(), "id", "version", "historized") || FieldUtils.isAllowedPrimitive(property.getClazz())) {
				values.put(e.getKey(), value.toString());
			} else if (value instanceof List) {
				List listValue = (List) value;
				List list = new ArrayList<>();
				for (Object element : listValue) {
					list.add(convert(element, ids));
				}
				values.put(e.getKey(), list);
			} else if (value != null && IdUtils.hasId(value.getClass())) {
				String id = IdUtils.getId(value).toString();
				if (ids.contains(id)) {
					values.put(e.getKey(), id);
				} else {
					ids.add(id);
					value = convert(value, ids);
					values.put(e.getKey(), value);
				}
			} else {
				values.put(e.getKey(), convert(value, ids));
			}
		}
		return values;
	}
	
}
