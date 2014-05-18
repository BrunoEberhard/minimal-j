package ch.openech.mj.transaction;

import java.io.Serializable;

import ch.openech.mj.backend.Backend;

public interface Transaction<T extends Serializable> extends Serializable {

	public T execute(Backend backend);
	
}
