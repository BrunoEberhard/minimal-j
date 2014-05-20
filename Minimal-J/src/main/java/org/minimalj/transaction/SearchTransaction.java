package org.minimalj.transaction;

import java.io.Serializable;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.util.SerializationContainer;

public class SearchTransaction<T> implements Transaction<Serializable> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Object[] keys;
	private final String query;
	private final int maxResults;
	
	public SearchTransaction(Class<T> clazz, Object[] keys, String query, int maxResults) {
		this.clazz = clazz;
		this.keys = keys;
		this.query = query;
		this.maxResults = maxResults;
	}

	@Override
	public Serializable execute(Backend backend) {
		List<T>	result;
		if (keys != null) {
			result = backend.search(clazz, keys, query, maxResults);
		} else {
			result = backend.search(clazz, query, maxResults);
		}
		return SerializationContainer.wrap(result); // TODO wrap should return SerializableList

	}

}
