package org.minimalj.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps a InputStream. Delegates all actions except
 * the close which is ignored
 * 
 * @author bruno
 */
public class UnclosingInputStream extends InputStream {

	private final InputStream is;
	
	public UnclosingInputStream(InputStream is) {
		this.is = is;
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return is.skip(n);
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public void mark(int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		is.reset();
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	@Override
	public void close() throws IOException {
		// ignore!
	}
}
