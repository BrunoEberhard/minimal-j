package ch.openech.mj.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps a OutputStream. Delegates all actions except
 * the close which is ignored
 * 
 * @author bruno
 */
public class UnclosingOoutputStream extends OutputStream {

	private final OutputStream os;
	
	public UnclosingOoutputStream(OutputStream os) {
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		os.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		// ignore!
	}
	
	
}
