package org.minimalj.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;

public class AuthorizationBackend extends Backend {

	private final Backend backend;

	public AuthorizationBackend(Backend backend) {
		this.backend = backend;
	}

	private void execute(LogoutTransaction transaction) {
		Serializable authentication = transaction.getToken();
		Authorization.getInstance().logout(authentication);
	}

	@Override
	public Persistence getPersistence() {
		return backend.getPersistence();
	}

	@Override
	public <T> T execute(Transaction<T> transaction) {
		if (transaction instanceof LoginTransaction) {
			LoginTransaction loginTransaction = (LoginTransaction) transaction;
			return (T) Authorization.getInstance().login(loginTransaction.getLogin());
		} else if (transaction instanceof LogoutTransaction) {
			execute((LogoutTransaction) transaction);
			return null;
		}
		hasPermission(transaction);
		return backend.execute(transaction);
	}

	@Override
	public <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream) {
		hasPermission(streamConsumer);
		return backend.execute(streamConsumer, inputStream);
	}

	@Override
	public <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream) {
		hasPermission(streamProducer);
		return backend.execute(streamProducer, outputStream);
	}

	private boolean hasPermission(Object input) {
		Class<?> clazz = input instanceof Authenticated ? ((Authenticated) input).getClazz() : input.getClass();
		Role role = clazz.getAnnotation(Role.class);
		if (role != null) {
			if (input instanceof Authenticated) {
				Authenticated authenticated = (Authenticated) input;
				Subject subject = Authorization.getInstance().getUserByToken(authenticated.getToken());
				if (subject != null) {
					return subject.hasPermission(role.value());
				}
			}
			return false;
		}
		return true;
	}

}
