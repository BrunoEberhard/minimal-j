package org.minimalj.repository.memory;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.Repository;
import org.minimalj.repository.list.QueryResultList;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.Query.QueryLimitable;
import org.minimalj.security.Subject;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class InMemoryRepository implements Repository {
	private static final Logger logger = Logger.getLogger(InMemoryRepository.class.getName());
	
	private Map<Class<?>, Map<Object, Object>> memory = new HashMap<>();
	
	public InMemoryRepository(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			if (FieldUtils.hasValidHistorizedField(clazz)) {
				logger.warning(this.getClass().getSimpleName() + " doesn't support historized classes like " + clazz.getSimpleName());
			}
			memory.put(clazz, new HashMap<>());
		}
	}
	
	@Override
	public <T> T read(Class<T> clazz, Object id) {
		Map<Object, Object> objects = objects(clazz);
		return CloneHelper.clone((T) objects.get(id));
	}

	private <T> Map<Object, Object> objects(Class<T> clazz) {
		if (!memory.containsKey(clazz)) {
			memory.put(clazz, new HashMap<>());
		}
		Map<Object, Object> objects = memory.get(clazz);
		return objects;
	}

	@Override
	public <T> List find(Class<T> clazz, Query query) {
		if (query instanceof Limit) {
			Limit limit = (Limit) query;
			List l = find(clazz, limit.getQuery());
			if (limit.getOffset() == null) {
				return l.subList(0, Math.min(limit.getRows(), l.size()));
			} else {
				return l.subList(limit.getOffset(), Math.min(limit.getOffset() + limit.getRows(), l.size()));
			}
		} else if (query instanceof AllCriteria) {
			AllCriteria allCriteria = (AllCriteria) query;
			return find(clazz, allCriteria);
		} else {
			return new QueryResultList(this, clazz, (QueryLimitable) query);
		}
	}

	private <T> List find(Class<T> clazz, QueryLimitable query) {
		List<Order> orders = new ArrayList<>();
		while (query instanceof Order) {
			Order order = (Order) query;
			orders.add(order);
			query = order.getQuery();
		}
		List l = find(clazz, (Criteria) query);
		for (Order order : orders) {
			order(order, l);
		}
		return l;
	}

	private void order(Order order, List l) {
		String path = order.getPath();
		if (path.contains(".")) {
			throw new IllegalArgumentException();
		}
		int factor = order.isAscending() ? 1 : -1;
		PropertyInterface property = Properties.getProperty(l.get(0).getClass(), path);
		Collections.sort(l, (a, b) -> {
			Object value1 = property.getValue(a);
			Object value2 = property.getValue(b);
			if (value1 == null) {
				return value2 == null ? 0 : -factor;
			} else if (value2 == null) {
				return factor;
			} else {
				return ((Comparable) value1).compareTo(value2) * factor;
			}
		});
	}
	
	private <T> List find(Class<T> clazz, Criteria criteria) {
		Predicate predicate = PredicateFactory.createPredicate(clazz, criteria);
		if (View.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(clazz);
			Map<Object, Object> objects = objects(viewedClass);
			return (List) objects.values().stream().filter(predicate).map(object -> ViewUtil.view(object, CloneHelper.newInstance(clazz))).collect(Collectors.toList());
		} else {
			Map<Object, Object> objects = objects(clazz);
			return (List) objects.values().stream().filter(predicate).collect(Collectors.toList());
		}
//		return (List) objects.values().stream().filter(criteria).collect(Collectors.toList());
	}
	
	@Override
	public <T> long count(Class<T> clazz, Query query) {
		if (query instanceof Limit) {
			query = ((Limit) query).getQuery();
		}
		while (query instanceof Order) {
			query = ((Order) query).getQuery();
		}
		return find(clazz, (Criteria) query).size();
	}

	@Override
	public <T> Object insert(T object) {
		T t = save(object, true);
		return IdUtils.getId(t);
	}

	private <T> T save(T object, boolean create) {
		if (memory(object)) {
			return object;
		}
		check(object, create, new HashSet<>());
		
		object = CloneHelper.clone(object);
		Object id = IdUtils.getId(object);
		if (id == null) {
			id = IdUtils.createId();
			IdUtils.setId(object, id);
		}
		Map<Object, Object> objects = objects(object.getClass());
		objects.put(id, object);
		
		for (Field field : object.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field) || FieldUtils.isTransient(field) || !FieldUtils.isPublic(field)) continue;
			try {
				Object value = field.get(object);
				if (value == null) {
					continue;
				}
				if (IdUtils.hasId(value.getClass())) {
					value = save(value, create);
					field.set(object, value);
				} else if (value instanceof List) {
					List list = (List) value;
					List newList = new ArrayList<>(list.size());
					for (Object element : list) {
						if (IdUtils.hasId(element.getClass())) {
							newList.add(save(element, create));
						}
					}
					if (!newList.isEmpty()) {
						field.set(object, newList);
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return object;
	}

	private void check(Object object, boolean create, Set<Object> checked) {
		if (checked.contains(object)) {
			return;
		} else {
			checked.add(object);
		}
		for (Field field : object.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field) || FieldUtils.isTransient(field) || !FieldUtils.isPublic(field)) continue;
			try {
				Object value = field.get(object);
				if (field.getAnnotation(NotEmpty.class) != null) {
					if (value == null) {
						throw new IllegalArgumentException(field.getName() + " must not be null");
					} else if (value instanceof String && ((String)value).isEmpty()) {
						throw new IllegalArgumentException(field.getName() + " must not be empty");
					}
				}
				
				TechnicalField technicalField = field.getAnnotation(TechnicalField.class);
				if (technicalField != null) {
					if (technicalField.value() == TechnicalFieldType.CREATE_DATE && value == null) {
						field.set(object, LocalDateTime.now());
					} else if (technicalField.value() == TechnicalFieldType.EDIT_DATE) {
						field.set(object, LocalDateTime.now());
					} else if (technicalField.value() == TechnicalFieldType.CREATE_USER && value == null || technicalField.value() == TechnicalFieldType.EDIT_USER) {
						Subject currentSubject = Subject.getCurrent();
						if (currentSubject != null) {
							field.set(object, currentSubject.getName());
						}
					}
				}
				
				if (value instanceof List) {
					List list = (List) value;
					for (Object element : list) {
						check(element, create, checked);
					}
				} else if (value != null) {
					check(value, create, checked);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		
	}

	private boolean memory(Object object) {
		Map<Object, Object> objects = objects(object.getClass());
		Object id = IdUtils.getId(object);
		return objects.containsKey(id);
	}
	
	@Override
	public <T> void update(T object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			throw new IllegalArgumentException();
		}
		check(object, false, new HashSet<>());
		
		// don't use read, would clone
		Map<Object, Object> objects = objects(object.getClass());
		Object existingObject = (T) objects.get(id);
		
		boolean lock = FieldUtils.hasValidVersionfield(object.getClass());
		if (lock) {
			int existingVersion = IdUtils.getVersion(existingObject);
			int updateVersion = IdUtils.getVersion(object);
			if (existingVersion > updateVersion) {
				throw new RuntimeException();
			}
			CloneHelper.deepCopy(object, existingObject);
			IdUtils.setVersion(existingObject, existingVersion + 1);
		} else {
			CloneHelper.deepCopy(object, existingObject);
		}
	}

	private boolean isReferenced(Object object) {
		Object id = IdUtils.getId(object);
		for (Map<Object, Object> m : memory.values()) {
			for (Object e : m.values()) {
				if (object != e && isReferenced(id, e)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isReferenced(Object id, Object e) {
		if (e == null) {
			return false;
		} else if (IdUtils.hasId(e.getClass())) {
			if (id.equals(IdUtils.getId(e))) {
				return true;
			}
		}
		for (Field field : e.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field) || !FieldUtils.isPublic(field)) continue;
			try {
				Object value = field.get(e);
				if (isReferenced(id, value)) {
					return true;
				}
				if (value instanceof List) {
					for (Object element : (List) value) {
						if (isReferenced(id, element)) {
							return true;
						}
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}
		return false;
	}
	
	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		Map<Object, Object> objects = objects(clazz);
		Object object = objects.get(id);
		if (object != null && isReferenced(object)) {
			throw new IllegalStateException("Referenced objects cannot be deleted");
		}
		objects.remove(id);
	}

	public <T> void delete(Object object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			throw new IllegalArgumentException();
		}
		delete(object.getClass(), id);
	}

}
