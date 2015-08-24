package org.minimalj.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;

public interface Authenticated extends Serializable {

	public Serializable getAuthentication();

	public static abstract class AbstractAuthenticated implements Authenticated {
		private static final long serialVersionUID = 1L;

		private final Serializable authentication;

		public AbstractAuthenticated(Serializable authentication) {
			this.authentication = authentication;
		}

		@Override
		public Serializable getAuthentication() {
			return authentication;
		}
	}

	public static class AuthenticatedTransaction<T> extends AbstractAuthenticated implements Transaction<T> {
		private static final long serialVersionUID = 1L;

		private final Transaction<T> delegate;

		public AuthenticatedTransaction(Transaction<T> delegate, Serializable authentication) {
			super(authentication);
			this.delegate = delegate;
		}

		@Override
		public T execute(Persistence persistence) {
			return delegate.execute(persistence);
		}
	}

	public static class AuthenticatedStreamConsumer<T extends Serializable> extends AbstractAuthenticated implements StreamConsumer<T> {
		private static final long serialVersionUID = 1L;

		private final StreamConsumer<T> delegate;

		public AuthenticatedStreamConsumer(StreamConsumer<T> delegate, Serializable authentication) {
			super(authentication);
			this.delegate = delegate;
		}

		@Override
		public T consume(Persistence persistence, InputStream stream) {
			return delegate.consume(persistence, stream);
		}
	}

	public static class AuthenticatedStreamProducer<T extends Serializable> extends AbstractAuthenticated implements StreamProducer<T> {
		private static final long serialVersionUID = 1L;

		private final StreamProducer<T> delegate;

		public AuthenticatedStreamProducer(StreamProducer<T> delegate, Serializable authentication) {
			super(authentication);
			this.delegate = delegate;
		}

		@Override
		public T produce(Persistence persistence, OutputStream stream) {
			return delegate.produce(persistence, stream);
		}
	}

}
