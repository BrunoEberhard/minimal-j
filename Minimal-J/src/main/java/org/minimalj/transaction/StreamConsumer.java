package org.minimalj.transaction;

import java.io.InputStream;
import java.io.Serializable;

import org.minimalj.backend.Backend;

public interface StreamConsumer<T extends Serializable> extends Serializable {

	public T comsume(Backend backend, InputStream stream);
	
}
