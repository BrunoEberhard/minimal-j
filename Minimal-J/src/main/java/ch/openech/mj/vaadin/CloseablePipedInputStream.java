package ch.openech.mj.vaadin;

import java.io.IOException;
import java.io.PipedInputStream;

public class CloseablePipedInputStream extends PipedInputStream {

	private boolean closed = false;

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	@Override
	public synchronized int available() throws IOException {
		if (!closed) {
			return super.available();
		} else {
			throw new IllegalStateException();
		}
	}
	
}
