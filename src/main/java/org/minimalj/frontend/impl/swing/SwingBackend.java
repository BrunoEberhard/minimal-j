package org.minimalj.frontend.impl.swing;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.transaction.Transaction;

public class SwingBackend extends Backend {
	private final Backend delegate;
	
	public SwingBackend(Backend delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> T doExecute(Transaction<T> transaction) {
		if (!SwingFrontend.hasContext()) {
			return delegate.doExecute(transaction);
		}
		
		SwingTab pageBrowser = (SwingTab) SwingFrontend.getInstance().getPageManager();
		ExecuteThread<T> thread = new ExecuteThread<>(pageBrowser, transaction);
		thread.start();
		pageBrowser.lock(); // blocks (and dispatches swing events)

		if (thread.getException() != null) {
			throw new RuntimeException(thread.getException());
		} else {
			return thread.getResult();
		}
	}
	
	@Override
	public boolean isInTransaction() {
		return delegate.isInTransaction();
	}
	
	private class ExecuteThread<T> extends Thread {
		private final SwingTab pageBrowser;
		private final Transaction<T> transaction;
		private T result;
		private Exception exception;

		public ExecuteThread(SwingTab pageBrowser, Transaction<T> transaction) {
			this.pageBrowser = pageBrowser;
			this.transaction = transaction;
		}

		@Override
		public void run() {
			try {
				result = delegate.doExecute(transaction);
			} catch (Exception x) {
				exception = x;
			} finally {
				pageBrowser.unlock();
			}
		}

		public T getResult() {
			return result;
		}

		public Exception getException() {
			return exception;
		}
	}

}
