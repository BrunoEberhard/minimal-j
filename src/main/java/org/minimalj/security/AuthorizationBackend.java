package org.minimalj.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

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
		Serializable authentication = transaction.getAuthentication();
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
		checkPermission(transaction);
		return backend.execute(transaction);
	}

	@Override
	public <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream) {
		checkPermission(streamConsumer);
		return backend.execute(streamConsumer, inputStream);
	}

	@Override
	public <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream) {
		checkPermission(streamProducer);
		return backend.execute(streamProducer, outputStream);
	}

	private void checkPermission(Object input) {
		Role role = input.getClass().getAnnotation(Role.class);
		if (role != null) {
			if (input instanceof Authenticated) {
				Authenticated authenticated = (Authenticated) input;
				if (authenticated.getAuthentication() instanceof UUID) {
					Serializable authentication = authenticated.getAuthentication();
					MjUser user = Authorization.getInstance().getUserByAuthentication(authentication);
					if (user != null) {
						for (String roleName : role.value()) {
							if (user.getRoles().contains(roleName)) {
								return;
							}
						}
					}
				}
			}
			throw new IllegalStateException("Authorization failed for " + input.getClass().getSimpleName());
		}
	}

}
