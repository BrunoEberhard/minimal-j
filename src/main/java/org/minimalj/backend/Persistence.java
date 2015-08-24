package org.minimalj.backend;

import java.io.Serializable;
import java.util.List;

import org.minimalj.transaction.criteria.Criteria;

public interface Persistence {

	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults);

	public <T> Object insert(T object);

	public <T> T update(T object);

	public <T> void delete(Class<T> clazz, Object id);

	public <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameter);

	public <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameter);

}
