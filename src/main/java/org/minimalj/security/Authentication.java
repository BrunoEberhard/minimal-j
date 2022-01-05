package org.minimalj.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.resources.Resources;

public abstract class Authentication implements Serializable {
	private static final long serialVersionUID = 1L;

	private final transient Map<Serializable, Subject> subjectByToken = new HashMap<>();

	public void showLogin() {
		getLoginAction().run();
	}

	public abstract Action getLoginAction();

	public Action getLogoutAction() {
		return new Action(Resources.getString("LogoutAction")) {
			public void run() {
				forgetMe();
				Subject.setCurrent(null);
				Frontend.getInstance().login(null);
			};
		};
	}

	protected void forgetMe() {
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
