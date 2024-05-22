package org.minimalj.rest;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.impl.json.JsonWriter;
import org.minimalj.model.Code;
import org.minimalj.model.Dependable;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.list.RelationCriteria;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Criteria.CompoundCriteria;
import org.minimalj.repository.query.FieldCriteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.rest.openapi.model.OpenAPI.Type;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class EntityJsonWriter {

	public static String write(Object entity) {
		if (entity instanceof List) {
			return write((List<?>) entity);
		} else {
			Map<String, Object> map = convert(entity);
			return new JsonWriter().write(map);
		}
	}

	public static String write(List<?> entities) {
		List<Map<String, Object>> mapList = new ArrayList<>();
		for (Object entity : entities) {
			mapList.add(convert(entity));
		}
		return new JsonWriter().write(mapList);
	}

	private static Map<String, Object> convert(Object entity) {
		Map<String, Object> values = new LinkedHashMap<>();

		if (entity instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) entity;
			if (map.isEmpty()) {
				return null;
			}
			for (Map.Entry<String, Object> e2 : map.entrySet()) {
				values.put(e2.getKey(), convert(e2.getValue()));
			}
			return values;
		}
		
		Map<String, Property> properties = Properties.getProperties(entity.getClass());
		for (Map.Entry<String, Property> e : properties.entrySet()) {
			Property property = e.getValue();
			Object value = property.getValue(entity);
			
			if (value == null) {
				continue;
			} else if (entity instanceof Dependable d && d.getParent() == value) {
				continue;
			} else {
				String propertyName = e.getKey();
				if (propertyName.equals("eNum")) {
					propertyName = "enum";
				}
				
				if (e.getKey().equals("id") && value != null) {
					value = value.toString();
				}
				if (value instanceof String || value instanceof Boolean || value instanceof Number) {
					values.put(propertyName, value);
				} else if (value instanceof BigDecimal bd) {
					values.put(propertyName, bd.doubleValue());
				} else if (value instanceof byte[]) {
					values.put(propertyName, Base64.getEncoder().encodeToString((byte[]) value));
				} else if (FieldUtils.isAllowedPrimitive(property.getClazz())) {
					values.put(propertyName, value.toString());
				} else if (value instanceof Collection) {
					Collection<?> collection = (Collection<?>) value;
					if (collection.isEmpty()) {
						continue;
					}
					List list = new ArrayList<>();
					for (Object element : collection) {
						if (element instanceof String) {
							// List<String> would be not allowed in MJ but 'required' is such a list
							list.add(element);
						} else if (element instanceof Enum) {
							list.add(((Enum<?>) element).name());
						} else {
							list.add(convert(element));
						}
					}
					values.put(propertyName, list);
				} else if (value instanceof Type type) {
					values.put(propertyName, type.name().toLowerCase());
				} else if (value instanceof Enum enuum) {
					values.put(propertyName, enuum.name());
				} else if (value instanceof Code) {
					values.put(propertyName, IdUtils.getId(value));
					values.put(propertyName + "_text", Rendering.render(value));
				} else if (!EmptyObjects.isEmpty(value)) {
					value = convert(value);
					if (value != null) {
						values.put(propertyName, value);
					}
				}
			}
		}
		
		return values;
	}

	public static Map<String, Object> prepare(Object object) {
		if (object instanceof Query) {
			return prepare((Query) object);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
			for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
				if (property.getName().equals("class")) {
					continue;
				}
				Object value = property.getReadMethod().invoke(object);
				if (value != null && !value.getClass().isPrimitive() && !FieldUtils.isAllowedPrimitive(value.getClass())) {
					value = prepare(value);
				}
				result.put(property.getName(), value);
			}
			return result;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public static Map<String, Object> prepare(Query query) {
		Map<String, Object> result = new LinkedHashMap<>();
		if (query instanceof Limit) {
			Limit limit = (Limit) query;
			result.put("Offset", limit.getOffset());
			result.put("Rows", limit.getRows());
			query = limit.getQuery();
		}
		if (query instanceof Order) {
			String orderString = null;
			while (query instanceof Order) {
				Order order = (Order) query;
				orderString = orderString == null ? order.getPath() : orderString + ", " + order.getPath();
				if (!order.isAscending()) {
					orderString += "[desc]";
				}
				query = order.getQuery();
			}
			result.put("Order", orderString);
		}
		if (query instanceof Criteria) {
			result.put(query.getClass().getSimpleName(), prepare((Criteria) query));
		}
		return result;
	}
	
	public static Object prepare(Criteria criteria) {
		if (criteria instanceof AllCriteria) {
			return null;
		} 
		Map<String, Object> result = new LinkedHashMap<>();
		if (criteria instanceof FieldCriteria) {
			FieldCriteria fieldCriteria = (FieldCriteria) criteria;
			result.put(fieldCriteria.getPath(), fieldCriteria.getValue());
			result.put("Operator", fieldCriteria.getOperator().name());
		} else if (criteria instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) criteria;
			if (searchCriteria.getKeys() != null) {
				List<String> paths = new ArrayList<>();
				for (Object key : searchCriteria.getKeys()) {
					paths.add(Keys.getProperty(key).getPath());
				}
				result.put("Keys", String.join(", ", paths));
			}
			result.put("Query", searchCriteria.getQuery());
			if (searchCriteria.isNotEqual()) {
				result.put("NotEqual", searchCriteria.isNotEqual());
			}
		} else if (criteria instanceof RelationCriteria) {
			throw new RuntimeException("Not yet implemented " + criteria.getClass());
		}
		
		if (criteria instanceof CompoundCriteria) {
			CompoundCriteria compoundCriteria = (CompoundCriteria) criteria;
			List<Object> criteriaMapList = new ArrayList<>();
			for (Criteria c : compoundCriteria.getCriterias()) {
				criteriaMapList.add(prepare(c));
			}
			return criteriaMapList;
		}
		return result;
	}
	
}