package ch.openech.mj.model;

import java.io.Serializable;
import java.util.Arrays;

public class Search<T> implements Serializable {

	private Class<T> clazz;
	private final Object[] keys;
	
	public Search(Object... keys) {
		this.keys = keys;
	}

	/**
	 * I'm pretty sure that the clazz could be evaluated somehow from the keys.
	 * But that would make my mind explode. So the class is set by
	 * the findIndex method in the AbstractTable. Please don't use this setter
	 * in any other class.
	 * 
	 * @param clazz
	 */
	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
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
