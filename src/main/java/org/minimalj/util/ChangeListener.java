package org.minimalj.util;

/**
 * Minimal-J internal. May be moved or removed. Don't use it directly.
 *
 * @param <T> Type of event source
 */
public interface ChangeListener<T> {

	public void changed(T source);
	
}
