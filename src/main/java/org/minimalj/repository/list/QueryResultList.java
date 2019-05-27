package org.minimalj.repository.list;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.backend.repository.ReadCriteriaTransaction;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.Sortable;

/**
 * A repository may (but is not required to) return a QueryResultList when the
 * find method is called. Only the size of the list is calculated when a
 * QueryResultList is calculated. Every element read with a get(index) is loaded
 * from the backend.
 * <p>
 * 
 * If you know you will use many elements of the List use the subList method to
 * load a complete range of elements in a unmodifiable List. This is how such
 * lists are used by the Frontend
 * <p>
 *
 * A QueryResultList is unmodifiable. Every call of a add or remove method will
 * result in a UnsupportedOperationException.
 * <p>
 * 
 * If the database has changed before you call a get or subList method you may
 * get <code>null</code> as element or a unexpected reduced sub list. You don't
 * get an exception in that cases.
 * <p>
 * 
 * QueryResultList is Sortable. The order of the elements can be changed at any
 * time. The size of the List is not recalculated and the sub List created
 * before a sort are not updated. Of course this is mainly used by a Frontend.
 * Business methods should execute a new find on the Repository / Backend.
 * <p>
 *
 * @param <T>
 *            Class of the Elements
 */
public class QueryResultList<T> extends AbstractList<T> implements Sortable, Serializable {
	private static final long serialVersionUID = 1L;

	private transient Repository repository;

	private final ClassHolder<T> clazz;

	private Query query;
	private final int size;
	
	public QueryResultList(Repository repository, Class<T> clazz, Query query) {
		this.repository = repository;
		this.clazz = new ClassHolder<>(clazz);
		this.query = query;
		
		this.size = (int) repository.count(clazz, query.getCriteria());
	}
	
	@Override
	public T get(int index) {
		Query limtedCriteria = makeOrdered(query).limit(index, 1);
		List<T> result = find(limtedCriteria);
		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		Query limtedCriteria = makeOrdered(query).limit(fromIndex, toIndex - fromIndex);
		return find(limtedCriteria);
	}

	private Query makeOrdered(Query query) {
		if (!(query instanceof RelationCriteria)) {
			// some db (postgresql) change order of elements if no unique sort order is
			// specified
			return query.order(Properties.getProperty(clazz.getClazz(), "id"));
		} else {
			return query;
		}
	}

	private List<T> find(Query limtedCriteria) {
		return repository != null ? repository.find(clazz.getClazz(), limtedCriteria)
				: Backend.execute(new ReadCriteriaTransaction<>(clazz.getClazz(), limtedCriteria));
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
			query = query.order(property, sortDirections[i]);
		}
	}

	@Override
	public boolean canSortBy(Object sortKey) {
		return true;
	}

}
