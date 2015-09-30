package org.minimalj.backend;

import java.util.List;
import java.util.function.Predicate;

/**
 * The common interface of all types of persistences. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlPersistence
 *
 */
public interface Persistence {

	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> read(Class<T> clazz, Predicate<T> predicate, int maxResults);

	public <T> Object insert(T object);

	public <T> T update(T object);

	public <T> void delete(Class<T> clazz, Object id);
	
}
