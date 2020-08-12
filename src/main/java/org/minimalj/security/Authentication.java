package org.minimalj.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;

public abstract class Authentication implements Serializable {
	private static final long serialVersionUID = 1L;

	private final transient Map<Serializable, Subject> subjectByToken = new HashMap<>();

	public static Authentication create() {
		String userFile = Configuration.get("MjUserFile");
		if (userFile != null) {
			return new TextFileAuthentication(userFile);
		}
		
		if (Configuration.available("MjAuthentication")) {
			return Configuration.getClazz("MjAuthentication", Authentication.class);
		}

		return null;
	}
	
	public abstract void login(LoginListener loginListener);
	
	public interface LoginListener {
		
		public void loginSucceded(Subject subject);
		
		public default void loginCancelled() {
		}
		
	}
	
	public void logout() {
		Backend.execute(new Transaction<Void>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Void execute() {
				subjectByToken.remove(Subject.getCurrent().getToken());
				return null;
			}
		});
	}
	
	public Subject getUserByToken(Serializable token) {
		return subjectByToken.get(token);
	}

	public Subject createSubject(String name, List<String> roleNames) {
		Subject subject = new Subject(name, UUID.randomUUID(), roleNames);
		subjectByToken.put(subject.getToken(), subject);
		return subject;
	}
}
