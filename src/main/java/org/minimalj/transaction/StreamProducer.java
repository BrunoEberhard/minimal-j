package org.minimalj.transaction;

import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.backend.Backend;

public interface StreamProducer<T extends Serializable> extends Serializable {

	public T produce(Backend backend, OutputStream stream);
	
}
