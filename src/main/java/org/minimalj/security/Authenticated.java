package org.minimalj.security;

import java.io.Serializable;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public interface Authenticated extends Serializable {

	public Serializable getToken();

	public Class<?> getClazz();
	
	public static abstract class AbstractAuthenticated implements Authenticated {
		private static final long serialVersionUID = 1L;

		private final Serializable token;

		public AbstractAuthenticated(Subject subject) {
			this.token = subject.getToken();
		}

		@Override
		public Serializable getToken() {
			return token;
		}
	}

	public static class AuthenticatedTransaction<T> extends AbstractAuthenticated implements Transaction<T> {
		private static final long serialVersionUID = 1L;

		private final Transaction<T> delegate;

		public AuthenticatedTransaction(Transaction<T> delegate, Subject subject) {
			super(subject);
			this.delegate = delegate;
		}

		@Override
		public T execute(Persistence persistence) {
			return delegate.execute(persistence);
		}
		
		@Override
		public Class<?> getClazz() {
			return delegate.getClass();
		}
	}

}
