package org.minimalj.repository.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Criteria.AndCriteria;
import org.minimalj.repository.query.Criteria.CompoundCriteria;
import org.minimalj.repository.query.Criteria.OrCriteria;
import org.minimalj.repository.query.FieldCriteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.util.IdUtils;

public class InMemoryRepository implements Repository {

	private Map<Class<?>, Map<Object, Object>> memory = new HashMap<>();
	
	public InMemoryRepository(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			memory.put(clazz, new HashMap<>());
		}
	}
	
	@Override
	public <T> T read(Class<T> clazz, Object id) {
		Map<Object, Object> objects = objects(clazz);
		return (T) objects.get(id);
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
		Predicate predicate = createPredicate(clazz, criteria);
		return (List) objects.values().stream().filter(predicate).collect(Collectors.toList());
	}

	private Predicate createPredicate(Class clazz, Criteria query) {
		if (query instanceof AllCriteria) {
			return (object) -> true;
		}
		if (query instanceof FieldCriteria) {
			FieldCriteria fieldCriteria = (FieldCriteria) query;
			return (object) -> {
				PropertyInterface p = Properties.getProperties(clazz).get(fieldCriteria.getPath());
				Object value = p.getValue(object);
				return Objects.equals(value, fieldCriteria.getValue());
			};
		} else if (query instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) query;
			List<PropertyInterface> searchColumns = findSearchColumns(clazz);
			return (object) -> {
				for (PropertyInterface p : searchColumns) {
					Object value = p.getValue(object);
					if (value instanceof String) {
						String s = ((String) value).toLowerCase();
						if (s.contains(searchCriteria.getQuery().replaceAll("\\*", "").toLowerCase())) {
							return !searchCriteria.isNotEqual();
						}
					}
				}
				return searchCriteria.isNotEqual();
			};
		} else if (query instanceof CompoundCriteria) {
			CompoundCriteria compoundCriteria = (CompoundCriteria) query;
			List<Predicate> predicates = new ArrayList<>();
			for (Criteria c : compoundCriteria.getCriterias()) {
				predicates.add(createPredicate(clazz, c));
			}
			if (query instanceof OrCriteria) {
				return (object) -> {
					for (Predicate p : predicates) {
						if (p.test(object)) {
							return true;
						}
					}
					return false;
				};
			} else if (query instanceof AndCriteria) {
				return (object) -> {
					for (Predicate p : predicates) {
						if (!p.test(object)) {
							return false;
						}
					}
					return true;
				};
			} 
			return (object) -> {
				return true;
			};
		}
		return (object) -> true;
	}

	
	private List<PropertyInterface> findSearchColumns(Class<?> clazz) {
		List<PropertyInterface> searchColumns = new ArrayList<>();
		for (PropertyInterface property : Properties.getProperties(clazz).values()) {
			Searched searchable = property.getAnnotation(Searched.class);
			if (searchable != null) {
				searchColumns.add(property);
			}
		}
		if (searchColumns.isEmpty()) {
			throw new IllegalArgumentException("No fields are annotated as 'Searched' in " + clazz.getName());
		}
		return searchColumns;
	}
	
	@Override
	public <T> long count(Class<T> clazz, Query query) {
		return find(clazz, query).size();
	}

	@Override
	public <T> Object insert(T object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			id = IdUtils.createId();
			IdUtils.setId(object, id);
		}
		Map<Object, Object> objects = objects(object.getClass());
		objects.put(id, object);
		return id;
	}

	@Override
	public <T> void update(T object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			throw new IllegalArgumentException();
		}
		Map<Object, Object> objects = objects(object.getClass());
		objects.put(id, object);
	}

	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		Map<Object, Object> objects = objects(clazz);
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
