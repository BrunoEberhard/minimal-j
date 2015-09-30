package org.minimalj.backend;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

public interface Persistence {

	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> read(Class<T> clazz, Predicate<T> predicate, int maxResults);

	public <T> Object insert(T object);

	public <T> T update(T object);

	public <T> void delete(Class<T> clazz, Object id);

	public <T> T execute(Class<T> clazz, String query, Serializable... parameter);

	public <T> List<T> execute(Class<T> clazz, String query, int maxResults, Serializable... parameter);
	
}
