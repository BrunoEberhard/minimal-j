package org.minimalj.frontend.impl.swing;

import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.util.logging.Logger;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.transaction.Transaction;

public class SwingBackend extends Backend {
	private static final Logger logger = Logger.getLogger(SwingBackend.class.getName());

	private final Backend delegate;
	
	public SwingBackend(Backend delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> T doExecute(Transaction<T> transaction) {
		if (!SwingFrontend.hasContext()) {
			return delegate.doExecute(transaction);
		}
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		EventQueue eq = tk.getSystemEventQueue();
		SecondaryLoop loop = eq.createSecondaryLoop();

		ExecuteSyncThread<T> thread = new ExecuteSyncThread<>(loop, transaction);
		SwingTab pageBrowser = (SwingTab) SwingFrontend.getInstance().getPageManager();
		try {
			SwingFrontend.browserStack.push(null);
			pageBrowser.lock();
			thread.start();
			if (!loop.enter()) {
				logger.warning("Could not execute background in second thread");
				return delegate.doExecute(transaction);
			}
			if (thread.getException() != null) {
				throw new RuntimeException(thread.getException());
			}
			return thread.getResult();
		} finally {
			SwingFrontend.browserStack.pop();
			pageBrowser.unlock();
		}
	}
	
	@Override
	public boolean isInTransaction() {
		return delegate.isInTransaction();
	}
	
	private class ExecuteSyncThread<T> extends Thread {
		private final SecondaryLoop loop;
		private final Transaction<T> transaction;
		private T result;
		private Exception exception;

		public ExecuteSyncThread(SecondaryLoop loop, Transaction<T> transaction) {
			this.loop = loop;
			this.transaction = transaction;
		}

		@Override
		public void run() {
			try {
				result = delegate.doExecute(transaction);
			} catch (Exception x) {
				exception = x;
			} finally {
				loop.exit();
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
