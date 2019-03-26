package org.minimalj.repository.ignite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.cache.Cache;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.processors.cache.CacheEntryImpl;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.model.properties.FlatProperties;
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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class IgniteRepository implements Repository {

	private static Ignite ignite;

	static {
		ignite = Ignition.start();
	}

	public IgniteRepository(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			IgniteCache cache = (IgniteCache) getCache(clazz);
			cache.clear();
		}
	}

	private <T> IgniteCache<Object, T> getCache(Class<T> clazz) {
		CacheConfiguration<Object, T> config = new CacheConfiguration<>(clazz.getSimpleName());
		config.setIndexedTypes(UUID.class, clazz);
		return ignite.getOrCreateCache(config);
	}

	@Override
	public <T> T read(Class<T> clazz, Object id) {
		IgniteCache cache = getCache(clazz);
		return (T) cache.get(id);
	}

	@Override
	public <T> Object insert(T object) {
		if (IdUtils.getId(object) == null) {
			IdUtils.setId(object, UUID.randomUUID());
		}
		check(object);

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

		Object id = IdUtils.getId(object);
		IgniteCache<Object, T> cache = (IgniteCache<Object, T>) getCache(object.getClass());
		cache.put(id, object);
		return id;
	}

	private Object get(Object value) {
		if (IdUtils.hasId(value.getClass())) {
			Object refId = IdUtils.getId(value);
			if (refId == null) {
				value = insert(value);
			} else {
				value = read(value.getClass(), refId);
			}
		}
		return value;
	}

	@Override
	public <T> void update(T object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			throw new IllegalArgumentException();
		}
		check(object);

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

		IgniteCache cache = getCache(object.getClass());

		Object existingObject = cache.get(id);

		boolean lock = FieldUtils.hasValidVersionfield(object.getClass());
		if (lock) {
			int existingVersion = IdUtils.getVersion(existingObject);
			int updateVersion = IdUtils.getVersion(object);
			if (existingVersion > updateVersion) {
				throw new RuntimeException();
			}
			IdUtils.setVersion(object, existingVersion + 1);
			cache.put(id, object);
			IdUtils.setVersion(object, updateVersion);
		} else {
			cache.put(id, object);
		}
	}

	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		IgniteCache cache = getCache(clazz);
		cache.remove(id);
	}

	public void delete(Object object) {
		IgniteCache cache = getCache(object.getClass());
		cache.remove(IdUtils.getId(object));
	}

	@Override
	public <T> List<T> find(Class<T> clazz, Query query) {
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

	public String name(Object classOrKey) {
		if (classOrKey instanceof Class) {
			return ((Class<?>) classOrKey).getName();
		} else {
			return Keys.getProperty(classOrKey).getName();
		}
	}

	public <T> T execute(Class<T> clazz, String string) {
		IgniteCache<Object, T> cache = getCache(clazz);
		CacheEntryImpl<Object, T> entry = (CacheEntryImpl) cache.query(new SqlQuery<>(clazz, string)).getAll().iterator().next();
		return entry.getValue();
	}

	private <T> List find(Class<T> clazz, QueryLimitable query) {
		List<Order> orders = new ArrayList<>();
		while (query instanceof Order) {
			Order order = (Order) query;
			orders.add(order);
			query = order.getQuery();
		}
		List l = find(clazz, (Criteria) query);
		Collections.reverse(orders);
		for (Order order : orders) {
			order(order, l);
		}
		return l;
	}

	private <T> List find(Class<T> clazz, Criteria criteria) {
		Predicate predicate = PredicateFactory.createPredicate(clazz, criteria);
		IgniteBiPredicate filter = (k, v) -> predicate.test(v);

		if (View.class.isAssignableFrom(clazz)) {
			IgniteCache cache = getCache(ViewUtil.getViewedClass(clazz));
			List<Cache.Entry> entries = cache.query(new ScanQuery(filter)).getAll();
			return entries.stream().map(e -> e.getValue()).collect(Collectors.toList());
		} else {
			IgniteCache cache = getCache(clazz);
			List<Cache.Entry> entries = cache.query(new ScanQuery(filter)).getAll();
			return entries.stream().map(e -> e.getValue())
					.map(object -> ViewUtil.view(object, CloneHelper.newInstance(clazz))).collect(Collectors.toList());
		}
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

	@FunctionalInterface
	public interface PropertyInspector {

		public void inspect(Object object, PropertyInterface property, Object value) throws Exception;
	}

	private void check(Object root) {
		apply(root, (object, property, value) -> {
			if (property.getAnnotation(NotEmpty.class) != null) {
				if (value == null) {
					throw new IllegalArgumentException(property.getPath() + " must not be null");
				} else if (value instanceof String && ((String) value).isEmpty()) {
					throw new IllegalArgumentException(property.getPath() + " must not be empty");
				}
			}

			TechnicalField technicalField = property.getAnnotation(TechnicalField.class);
			if (technicalField != null) {
				if (technicalField.value() == TechnicalFieldType.CREATE_DATE && value == null) {
					property.setValue(object, LocalDateTime.now());
				} else if (technicalField.value() == TechnicalFieldType.EDIT_DATE) {
					property.setValue(object, LocalDateTime.now());
				} else if (technicalField.value() == TechnicalFieldType.CREATE_USER && value == null
						|| technicalField.value() == TechnicalFieldType.EDIT_USER) {
					Subject currentSubject = Subject.getCurrent();
					if (currentSubject != null) {
						property.setValue(object, currentSubject.getName());
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
		for (PropertyInterface property : FlatProperties.getProperties(object.getClass()).values()) {
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

}
