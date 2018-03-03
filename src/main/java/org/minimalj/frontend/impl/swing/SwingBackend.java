package org.minimalj.frontend.impl.swing;

import javax.swing.SwingUtilities;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.repository.Repository;
import org.minimalj.transaction.Transaction;

public class SwingBackend extends Backend {
	private final Backend delegate;
	
	public SwingBackend(Backend delegate) {
		this.delegate = delegate;
	}

	@Override
	public Repository getRepository() {
		return delegate.getRepository();
	}
	
	@Override
	public <T> T doExecute(Transaction<T> transaction) {
		if (!SwingFrontend.hasContext() || !SwingUtilities.isEventDispatchThread()) {
			return delegate.doExecute(transaction);
		}
		
		SwingTab swingTab = (SwingTab) SwingFrontend.getInstance().getPageManager();
		ExecuteThread<T> thread = new ExecuteThread<>(swingTab, transaction);
		thread.start();
		swingTab.lock(); // blocks (and dispatches swing events)

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
		private final SwingTab swingTab;
		private final Transaction<T> transaction;
		private T result;
		private Exception exception;

		public ExecuteThread(SwingTab swingTab, Transaction<T> transaction) {
			this.swingTab = swingTab;
			this.transaction = transaction;
		}

		@Override
		public void run() {
			try {
				result = delegate.doExecute(transaction);
			} catch (Exception x) {
				exception = x;
			} finally {
				swingTab.unlock();
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
