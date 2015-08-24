package org.minimalj.transaction;

import java.io.InputStream;
import java.io.Serializable;

import org.minimalj.backend.Persistence;

public interface StreamConsumer<T extends Serializable> extends Serializable {

	public T consume(Persistence persistence, InputStream stream);
	
}
