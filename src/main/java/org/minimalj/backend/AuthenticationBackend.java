package org.minimalj.backend;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.frontend.Frontend;
import org.minimalj.security.Authenticated;
import org.minimalj.security.LoginTransaction;
import org.minimalj.security.LogoutTransaction;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;

public class AuthenticationBackend extends Backend {

	private final Backend backend;

	public AuthenticationBackend(Backend backend) {
		this.backend = backend;
	}

	@Override
	public Persistence getPersistence() {
		return backend.getPersistence();
	}

	@Override
	public <T> T execute(Transaction<T> transaction) {
		if (!(transaction instanceof LoginTransaction || transaction instanceof LogoutTransaction)) {
			return backend.execute(new Authenticated.AuthenticatedTransaction<T>(transaction, Frontend.getBrowser().getSubject()));
		} else {
			return backend.execute(transaction);
		}
	}

	@Override
	public <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream) {
		return backend.execute(new Authenticated.AuthenticatedStreamConsumer<T>(streamConsumer, Frontend.getBrowser().getSubject()), inputStream);
	}

	@Override
	public <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream) {
		return backend.execute(new Authenticated.AuthenticatedStreamProducer<T>(streamProducer, Frontend.getBrowser().getSubject()), outputStream);
	}

}
