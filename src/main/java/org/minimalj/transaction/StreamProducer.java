package org.minimalj.transaction;

import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.backend.Persistence;

public interface StreamProducer<T extends Serializable> extends Serializable {

	public T produce(Persistence persistence, OutputStream stream);
	
}
