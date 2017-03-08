package org.minimalj.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.Query.QueryLimitable;

public class QueryResultList<T> extends AbstractList<T> implements Sortable, Serializable {
	private static final long serialVersionUID = 1L;

	private transient Repository repository;

	private final ClassHolder<T> clazz;

	private QueryLimitable query;
	private final int size;
	
	public QueryResultList(Repository repository, Class<T> clazz, QueryLimitable query) {
		this.repository = repository;
		this.clazz = new ClassHolder<>(clazz);
		this.query = query;
		
		this.size = (int) repository.count(clazz, query);
	}
	
	private Repository getRepository() {
		return repository != null ? repository : Backend.getInstance().getRepository();
	}
	
	@Override
	public T get(int index) {
		Query limtedCriteria = query.limit(index, 1);
		List<T> result = getRepository().find(clazz.getClazz(), limtedCriteria);
		return result.isEmpty() ? null : result.get(0);
	}
	
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		Query limtedCriteria = query.limit(fromIndex, toIndex - fromIndex);
		List<T> result = getRepository().find(clazz.getClazz(), limtedCriteria);
		return result;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public void sort(Object[] sortKeys, boolean[] sortDirections) {
		while (query instanceof Order) {
			query = ((Order) query).getQuery();
		}
		for (int i = 0; i<sortKeys.length; i++) {
			PropertyInterface property = Keys.getProperty(sortKeys[i]);
			String path = property.getPath();
			query = new Order(query, path, sortDirections[i]);
		}
	}

}
