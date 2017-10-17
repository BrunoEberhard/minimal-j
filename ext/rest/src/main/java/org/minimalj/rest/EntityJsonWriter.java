package org.minimalj.rest;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.minimalj.frontend.impl.json.JsonWriter;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.list.RelationCriteria;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Criteria.CompoundCriteria;
import org.minimalj.repository.query.FieldCriteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;

public class EntityJsonWriter {

	public static String write(Object entity) {
		Map<String, Object> map = convert(entity, new HashSet<>());
		return new JsonWriter().write(map);
	}

	public static String write(List<?> entities) {
		List<Map<String, Object>> mapList = new ArrayList<>();
		TreeSet<String> ids = new TreeSet<>();
		for (Object entity : entities) {
			mapList.add(convert(entity, ids));
		};
		return new JsonWriter().write(mapList);
	}

//	private List<Map<String, Object>> convert(Object entity) {
	private static Map<String, Object> convert(Object entity, Set<String> ids) {
		Map<String, Object> values = new LinkedHashMap<>();

		if (entity instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) entity;
			if (map.isEmpty()) {
				return null;
			}
			for (Map.Entry<String, Object> e2 : map.entrySet()) {
				values.put(e2.getKey(), convert(e2.getValue(), ids));
			}
			return values;
		}
		
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(entity.getClass());
		for (Map.Entry<String, PropertyInterface> e : properties.entrySet()) {
			PropertyInterface property = e.getValue();
			Object value = property.getValue(entity);
			
			if (value == null) {
				continue;
			} else {
				String propertyName = e.getKey();
				// V2 !!!
				if ("eNum".equals(propertyName)) {
					propertyName = "enum";
				}
				
				if (value instanceof Boolean) {
					values.put(propertyName, value);
				} else if (StringUtils.equals(propertyName, "id", "version", "historized") || FieldUtils.isAllowedPrimitive(property.getClazz())) {
					values.put(propertyName, value.toString());
				} else if (value instanceof List) {
					List listValue = (List) value;
					if (listValue.isEmpty()) {
						continue;
					}
					List list = new ArrayList<>();
					for (Object element : listValue) {
						if (element instanceof String) {
							// List<String> would be not allowed in MJ but 'required' is such a list
							list.add(element);
						} else {
							list.add(convert(element, ids));
						}
					}
					values.put(propertyName, list);
				} else if (value instanceof Enum) {
					values.put(propertyName, value.toString().toLowerCase());
				} else if (value != null) {
					value = convert(value, ids);
					if (value != null) {
						values.put(propertyName, value);
					}
				}
			}
		}
		
		/*
		List<PropertyInterface> listProperties = FlatProperties.getListProperties(entity.getClass());
		for (PropertyInterface property : listProperties) {
			Object value = property.getValue(entity);
			
			if (value instanceof List) {
				String propertyName = property.getName();
				List listValue = (List) value;
				if (listValue.isEmpty()) {
					continue;
				}
				List list = new ArrayList<>();
				for (Object element : listValue) {
					if (element instanceof String) {
						// List<String> would be not allowed in MJ but 'required' is such a list
						list.add(element);
					} else {
						list.add(convert(element, ids));
					}
				}
				values.put(propertyName, list);
			}
		}
		*/
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
	
	public static class TestClass {
		public static final TestClass $ = Keys.of(TestClass.class);
		public String a,b;
	}
	
	public static void main(String[] args) {
		Query q = By.field(TestClass.$.a, "xy").and(By.field(TestClass.$.b, "z")).order(TestClass.$.a).order(TestClass.$.b, false).limit(10);
		
		String result = new JsonWriter().write(prepare(q));
		System.out.println(result);
		
		//
		
		q = By.field(TestClass.$.a, "xy").and(By.search("Fasel", TestClass.$.a, TestClass.$.b).negate());
		
		result = new JsonWriter().write(prepare(q));
		System.out.println(result);
		
	}
}
