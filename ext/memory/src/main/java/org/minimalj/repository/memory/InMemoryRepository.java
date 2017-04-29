package org.minimalj.repository.memory;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Query;
import org.minimalj.security.Subject;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class InMemoryRepository implements Repository {

	private Map<Class<?>, Map<Object, Object>> memory = new HashMap<>();
	
	public InMemoryRepository(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			if (FieldUtils.hasValidHistorizedField(clazz)) {
				throw new IllegalArgumentException(this.getClass().getSimpleName() + " doesn't support historized classes like " + clazz.getSimpleName());
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
		Map<Object, Object> objects = memory.get(clazz);
		if (objects == null) {
			throw new IllegalArgumentException();
		}
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
		} else if (query instanceof Criteria) {
			return find(clazz, (Criteria) query);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public <T> List find(Class<T> clazz, Criteria criteria) {
		Map<Object, Object> objects = objects(clazz);
		Predicate predicate = PredicateFactory.createPredicate(clazz, criteria);
		return (List) objects.values().stream().filter(predicate).collect(Collectors.toList());
//		return (List) objects.values().stream().filter(criteria).collect(Collectors.toList());
	}
	
	@Override
	public <T> long count(Class<T> clazz, Query query) {
		return find(clazz, query).size();
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
						throw new IllegalArgumentException();
					} else if (value instanceof String && ((String)value).isEmpty()) {
						throw new IllegalArgumentException();
					}
				}
				
				TechnicalField technicalField = field.getAnnotation(TechnicalField.class);
				if (technicalField != null) {
					if (technicalField.value() == TechnicalFieldType.CREATE_DATE && value == null) {
						field.set(object, LocalDateTime.now());
					} else if (technicalField.value() == TechnicalFieldType.EDIT_DATE) {
						field.set(object, LocalDateTime.now());
					} else if (technicalField.value() == TechnicalFieldType.CREATE_USER && value == null) {
						field.set(object, Subject.getCurrent().getName());
					} else if (technicalField.value() == TechnicalFieldType.EDIT_USER) {
						field.set(object, Subject.getCurrent().getName());
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
