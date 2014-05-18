package ch.openech.mj.transaction;

import java.io.InputStream;
import java.io.Serializable;

import ch.openech.mj.backend.Backend;

public interface StreamConsumer<T extends Serializable> extends Serializable {

	public T comsume(Backend backend, InputStream stream);
	
}
