package org.minimalj.repository.memory;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.minimalj.model.Code;
import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.model.test.ModelTest;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.security.Subject;
import org.minimalj.security.model.UserData;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.CsvReader;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class InMemoryRepository implements Repository {
	private static final Logger logger = Logger.getLogger(InMemoryRepository.class.getName());
	
	private Map<Class<?>, Map<String, Object>> memory = new HashMap<>();
	private Map<Class<?>, Integer> nextId = new HashMap<>();
	private Map<Class<?>, Long> nextLongId = new HashMap<>();
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	public InMemoryRepository(Model model) {
		this(model.getEntityClasses());
	}
	
	public InMemoryRepository(Class<?>... classes) {
		ModelTest modelTest = new ModelTest(classes);
		modelTest.assertValid();
		
		for (Class<?> clazz : classes) {
			if (FieldUtils.hasValidHistorizedField(clazz)) {
				logger.warning(this.getClass().getSimpleName() + " doesn't support historized classes like " + clazz.getSimpleName());
			}
			memory.put(clazz, new HashMap<>());
		}
		
		createCodes(Model.getClassesRecursive(classes, false, false));
	}

	private void createCodes(Collection<Class<?>> modelClasses) {
		createConstantCodes(modelClasses);
		createCsvCodes(modelClasses);
	}
	
	@SuppressWarnings("unchecked")
	private void createConstantCodes(Collection<Class<?>> modelClasses) {
		for (Class<?> clazz : modelClasses) {
			if (Code.class.isAssignableFrom(clazz)) {
				Class<? extends Code> codeClass = (Class<? extends Code>) clazz; 
				List<? extends Code> constants = Codes.getConstants(codeClass);
				for (Code code : constants) {
					insert(code);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createCsvCodes(Collection<Class<?>> modelClasses) {
		for (Class<?> clazz : modelClasses) {
			if (Code.class.isAssignableFrom(clazz)) {
				Class<? extends Code> codeClazz = (Class<? extends Code>) clazz;
				InputStream is = clazz.getResourceAsStream(clazz.getSimpleName() + ".csv");
				if (is != null) {
					CsvReader reader = new CsvReader(is);
					List<? extends Code> values = reader.readValues(codeClazz);
					for (Code value : values) {
						insert(value);
					}
				}
			}
		}
	}

	private <T> T executeWrite(Transaction<T> t) {
		lock.writeLock().lock();
		try {
			return t.execute();
		} finally {
			lock.writeLock().unlock();
		}
	}

	private <T> T executeRead(Transaction<T> t) {
		lock.readLock().lock();
		try {
			return t.execute();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public <T> T read(Class<T> clazz, Object id) {
		return executeRead(() -> {
			return CloneHelper.clone(read_(clazz, id));
		});
	}

	// read without clone
	private <T> T read_(Class<T> clazz, Object id) {
		Map<String, Object> objects = objects(clazz);
		return (T) objects.get(id.toString());
	}

	private <T> Map<String, Object> objects(Class<T> clazz) {
		if (!memory.containsKey(clazz)) {
			memory.put(clazz, new HashMap<>());
		}
		Map<String, Object> objects = memory.get(clazz);
		return objects;
	}

	@Override
	public <T> List find(Class<T> clazz, Query query) {
		return executeRead(() -> {
			if (query instanceof Limit) {
				Limit limit = (Limit) query;
				List l = findAndOrder(clazz, limit.getQuery());
				if (limit.getOffset() == null) {
					return l.subList(0, Math.min(limit.getRows(), l.size()));
				} else {
					return l.subList(limit.getOffset(), Math.min(limit.getOffset() + limit.getRows(), l.size()));
				}
			} else {
				return findAndOrder(clazz, query);
			}
		});
	}

	private <T> List findAndOrder(Class<T> clazz, Query query) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void order(Order order, List l) {
		if (l.isEmpty()) {
			return;
		}
		String path = order.getPath();
		int factor = order.isAscending() ? 1 : -1;
		Property property = Properties.getPropertyByPath(l.get(0).getClass(), path);
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> List find(Class<T> clazz, Criteria criteria) {
		List result = new ArrayList();
		if (View.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = ViewUtils.getViewedClass(clazz);
			Map<String, Object> objects = objects(viewedClass);
			for (Object object : objects.values()) {
				if (criteria.test(object)) {
					result.add(ViewUtils.view(object, CloneHelper.newInstance(clazz)));
				}
			}
			// Cheerpj doesn't work with Collectors.toList()
			// return (List) objects.values().stream().filter(predicate).map(object -> ViewUtil.view(object, CloneHelper.newInstance(clazz))).collect(Collectors.toList());
		} else {
			Map<String, Object> objects = objects(clazz);
			for (Object object : objects.values()) {
				if (criteria.test(object)) {
					result.add(object);
				}
			}
			// Cheerpj doesn't work with Collectors.toList()
			// return (List) objects.values().stream().filter(predicate).collect(Collectors.toList());
		}
		return result;
	}
	
	@Override
	public <T> long count(Class<T> clazz, Criteria criteria) {
		return executeRead(() -> {
			return find(clazz, criteria).size();
		});
	}

	@Override
	public <T> Object insert(T object) {
		return executeWrite(() -> {
			T t = save(object);
			return IdUtils.getId(t);
		});
	}

	private <T> T save(T object) {
		if (memory(object)) {
			return object;
		}
		check(object);
		
		object = CloneHelper.clone(object);
		Object id = IdUtils.getId(object);
		if (id == null) {
			id = createId(object);
			IdUtils.setId(object, id);
		}
		Map<String, Object> objects = objects(object.getClass());
		objects.put(id.toString(), object);
		
		apply(object, (o, property, value) -> {
			if (value != null) {
				if (value instanceof List) {
					List list = (List) value;
					List newList = new ArrayList<>(list.size());
					for (Object element : list) {
						newList.add(get(element));
					}
					value = newList;
				}
				property.setValue(o, get(value));
			}
		});
		return object;
	}

	protected Object createId(Object object) {
		Class<?> clazz = IdUtils.getIdClass(object.getClass());
		if (clazz == Integer.class) {
			int id = nextId.computeIfAbsent(clazz, newClazz -> 1);
			nextId.put(clazz, id + 1);
			return id;
		} else if (clazz == Long.class) {
			long id = nextLongId.computeIfAbsent(clazz, newClazz -> 1L);
			nextLongId.put(clazz, id + 1);
			return id;
		} else {
			return UUID.randomUUID();
		}
	}
	
	private Object get(Object value) {
		if (IdUtils.hasId(value.getClass())) {
			Object refId = IdUtils.getId(value);
			if (refId == null) {
				value = save(value);
			} else {
				value = read_(value.getClass(), refId);
			}
		}
		return value;
	}

	@FunctionalInterface
	public interface PropertyInspector {
		
		public void inspect(Object object, Property property, Object value) throws Exception;
	}
	
	private void check(Object root) {
		apply(root, (object, property, value) -> {
			if (property.getAnnotation(NotEmpty.class) != null) {
				if (value == null) {
					throw new IllegalArgumentException(property.getPath() + " must not be null");
				} else if (value instanceof String && ((String)value).isEmpty()) {
					throw new IllegalArgumentException(property.getPath() + " must not be empty");
				}
			}
			
			TechnicalField technicalField = property.getAnnotation(TechnicalField.class);
			if (technicalField != null) {
				if (technicalField.value() == TechnicalFieldType.CREATE_DATE && value == null) {
					property.setValue(object, LocalDateTime.now());
				} else if (technicalField.value() == TechnicalFieldType.EDIT_DATE) {
					property.setValue(object, LocalDateTime.now());
				} else if (technicalField.value() == TechnicalFieldType.CREATE_USER && value == null || technicalField.value() == TechnicalFieldType.EDIT_USER) {
					Subject subject = Subject.getCurrent();
					if (subject != null) {
						UserData userData = subject.getUser();
						Class<?> propertyClass = property.getClazz();
						if (propertyClass.isAssignableFrom(userData.getClass())) {
							property.setValue(object, userData);
						} else if (propertyClass == String.class) {
							property.setValue(object, userData.getName());
						} else if (View.class.isAssignableFrom(propertyClass)) {
							Object view = ViewUtils.view(userData, CloneHelper.newInstance(propertyClass));
							property.setValue(object, view);
						} else {
							throw new IllegalArgumentException("Not a valid USER field: " + property);
						}
					}
				}
			}
		});
	}

	private void apply(Object root, PropertyInspector consumer) {
		apply(root, consumer, new HashSet<>());
	}
	
	private void apply(Object object, PropertyInspector consumer, Set<Object> checked) {
		if (checked.contains(object)) {
			return;
		} else {
			checked.add(object);
		}
		for (Property property : FlatProperties.getProperties(object.getClass()).values()) {
			try {
				Object value = property.getValue(object);
				consumer.inspect(object, property, value);
				if (value instanceof List) {
					List list = (List) value;
					for (Object element : list) {
						apply(element, consumer, checked);
					}
				} else if (value != null) {
					apply(value, consumer, checked);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private boolean memory(Object object) {
		Object id = IdUtils.getId(object);
		if (id != null) {
			Map<String, Object> objects = objects(object.getClass());
			return objects.containsKey(id.toString());
		} else {
			return false;
		}
	}
	
	@Override
	public <T> void update(T object) {
		executeWrite(() -> {
			Object id = IdUtils.getId(object);
			if (id == null) {
				throw new IllegalArgumentException();
			}
			check(object);
			
			Object existingObject = read_(object.getClass(), id);
			
			boolean lock = FieldUtils.hasValidVersionfield(object.getClass());
			if (lock) {
				int existingVersion = IdUtils.getVersion(existingObject);
				int updateVersion = IdUtils.getVersion(object);
				if (existingVersion > updateVersion) {
					throw new RuntimeException();
				}
				update(object, existingObject);
				IdUtils.setVersion(existingObject, existingVersion + 1);
			} else {
				update(object, existingObject);
			}
			return null;
		});
	}

	private <T> void update(T object, Object existingObject) {
		for (Property property : FlatProperties.getProperties(object.getClass()).values()) {
			try {
				Object value = property.getValue(object);
				if (value instanceof List) {
					List list = (List) value;
					List newList = new ArrayList<>();
					for (Object element : list) {
						newList.add(get(element));
					}
					value = newList;
				} else if (value != null) {
					value = get(value);
				}
				property.setValue(existingObject, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private boolean isReferenced(Object object) {
		Object id = IdUtils.getId(object);
		for (Map<String, Object> m : memory.values()) {
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
		for (Field field : e.getClass().getFields()) {
			if (FieldUtils.isStatic(field)) continue;
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
	public <T> int delete(Class<T> clazz, Criteria criteria) {
		return executeWrite(() -> {
			Map<String, Object> objects = objects(clazz);
			Iterator<Object> iterator = objects.values().iterator();
			int count = 0;
			while (iterator.hasNext()) {
				Object next = iterator.next();
				if (criteria.test(next)) {
					if (isReferenced(next)) {
						throw new IllegalStateException("Referenced objects cannot be deleted");
					}
					iterator.remove();
					count++;
				}
			}
			return count;
		});
	}

	@Override
	public <T> void delete(T object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			throw new IllegalArgumentException();
		}
		delete(object.getClass(), By.field(Properties.getProperty(object.getClass(), "id"), id));
	}

}
