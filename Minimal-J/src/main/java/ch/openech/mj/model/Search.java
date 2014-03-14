package ch.openech.mj.model;

import java.io.Serializable;
import java.util.Arrays;

public final class Search<T> implements Serializable {

	private final Class<T> clazz;
	private final Object[] keys;
	
	@SuppressWarnings("unchecked")
	public Search(Object... keys) {
		this.keys = keys;
		clazz = (Class<T>) Keys.getRootDeclaringClass(keys[0]);
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public Object[] getKeys() {
		return keys;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(keys);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Search other = (Search) obj;
		if (!Arrays.equals(keys, other.keys))
			return false;
		return true;
	}
	
}
