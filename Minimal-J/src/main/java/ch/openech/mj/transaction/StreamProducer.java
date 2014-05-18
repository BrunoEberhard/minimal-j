package ch.openech.mj.transaction;

import java.io.OutputStream;
import java.io.Serializable;

import ch.openech.mj.backend.Backend;

public interface StreamProducer<T extends Serializable> extends Serializable {

	public T consume(Backend backend, OutputStream stream);
	
}
