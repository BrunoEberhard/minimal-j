package org.minimalj.util;

import java.io.Serializable;

/**
 * Class cannot be serialized only its name. This helper holds the class
 * or at least it's name if the holder is serialized and then deserialized.
 *
 */
public class ClassHolder<CLASS> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient Class<CLASS> clazz;
	private final String className;

	public ClassHolder(Class<CLASS> clazz) {
		this.clazz = clazz;
		this.className = clazz.getName();
	}

	@SuppressWarnings("unchecked")
	public Class<CLASS> getClazz() {
		if (clazz == null) {
			try {
				clazz = (Class<CLASS>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return clazz;
	}

	@Override
	public String toString() {
		return className.substring(className.lastIndexOf('.') + 1);
	}
}
