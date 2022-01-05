package org.minimalj.repository;

import java.util.List;

import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Query;

/**
 * The common interface of all types of repositories. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlRepository.
 * 
 * A repository may use an arbitrary class for id fields. But it must be able to handle
 * reads and finds with ids converted to a String.
 *
 */
public interface Repository {
	
	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> find(Class<T> clazz, Query query);
	
	public <T> long count(Class<T> clazz, Criteria criteria);

	public <T> Object insert(T object);

	public <T> void update(T object);

	public <T> void delete(T object);

	public <T> int delete(Class<T> clazz, Criteria criteria);

}
