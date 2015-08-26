package org.minimalj.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
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

	public static class AuthenticatedStreamConsumer<T extends Serializable> extends AbstractAuthenticated implements StreamConsumer<T> {
		private static final long serialVersionUID = 1L;

		private final StreamConsumer<T> delegate;

		public AuthenticatedStreamConsumer(StreamConsumer<T> delegate, Subject subject) {
			super(subject);
			this.delegate = delegate;
		}

		@Override
		public T consume(Persistence persistence, InputStream stream) {
			return delegate.consume(persistence, stream);
		}
		
		@Override
		public Class<?> getClazz() {
			return delegate.getClass();
		}
	}

	public static class AuthenticatedStreamProducer<T extends Serializable> extends AbstractAuthenticated implements StreamProducer<T> {
		private static final long serialVersionUID = 1L;

		private final StreamProducer<T> delegate;

		public AuthenticatedStreamProducer(StreamProducer<T> delegate, Subject subject) {
			super(subject);
			this.delegate = delegate;
		}

		@Override
		public T produce(Persistence persistence, OutputStream stream) {
			return delegate.produce(persistence, stream);
		}

		@Override
		public Class<?> getClazz() {
			return delegate.getClass();
		}
	}

}
