package org.minimalj.transaction;

import java.io.Serializable;

import org.minimalj.backend.Backend;

public interface Transaction<T extends Serializable> extends Serializable {

	public T execute(Backend backend);
	
}
