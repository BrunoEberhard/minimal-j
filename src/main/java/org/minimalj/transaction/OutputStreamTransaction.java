package org.minimalj.transaction;

import java.io.OutputStream;
import java.io.Serializable;

public abstract class OutputStreamTransaction<T extends Serializable> implements Transaction<T> {
	private static final long serialVersionUID = 1L;
	
	private transient OutputStream stream;
	
	public OutputStreamTransaction(OutputStream outputStream) {
		this.stream = outputStream;
	}
	
	public void setStream(OutputStream stream) {
		this.stream = stream;
	}
	
	public OutputStream getStream() {
		return stream;
	}
}
