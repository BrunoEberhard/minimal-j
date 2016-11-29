package org.minimalj.security;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;
import org.minimalj.security.model.User;
import org.minimalj.security.model.UserPassword;
import org.minimalj.transaction.Transaction;

public abstract class UserPasswordAuthentication extends Authentication {

	@Override
	public void login(LoginListener loginListener) {
		new UserPasswordAction(loginListener).action();
	}
	
	public static class UserPasswordAction extends Editor<UserPassword, Subject> {

		private final LoginListener listener;
		
		public UserPasswordAction(LoginListener listener) {
			this.listener = listener;
		}

		@Override
		protected UserPassword createObject() {
			return new UserPassword();
		}

		@Override
		protected Form<UserPassword> createForm() {
			Form<UserPassword> form = new Form<>();
			form.line(UserPassword.$.user);
			form.line(new PasswordFormElement(UserPassword.$.password));
			return form;
		}

		@Override
		protected List<Action> createAdditionalActions() {
			if (!Application.getInstance().isLoginRequired()) {
				Action action = new AnonymousLoginAction();
				return Collections.singletonList(action);
			} else {
				return Collections.emptyList();
			}
		}
		
		private class AnonymousLoginAction extends Action {
			@Override
			public void action() {
				listener.loginSucceded(null);
				UserPasswordAction.super.cancel(); // close dialog but don't call loginCancelled
			}
		}

		@Override
		protected Subject save(UserPassword userPassword) {
			LoginTransaction loginTransaction = new LoginTransaction(userPassword);
			return Backend.execute(loginTransaction);
		}

		@Override
		protected boolean closeWith(Subject subject) {
			if (subject != null) {
				return true;
			} else {
				Frontend.showError("Login failed");
				return false;
			}
		}
		
		@Override
		public void cancel() {
			listener.loginCancelled();
			super.cancel();
		}
		
		@Override
		protected void finished(Subject subject) {
			listener.loginSucceded(subject);
		}
	}
	
	public static class LoginTransaction implements Transaction<Subject> {
		private static final long serialVersionUID = 1L;
		
		private final UserPassword userPassword;
		
		public LoginTransaction(UserPassword userPassword) {
			Objects.nonNull(userPassword);
			Objects.nonNull(userPassword.user);
			
			this.userPassword = userPassword;
		}
		
		public UserPassword getLogin() {
			return userPassword;
		}
		
		@Override
		public Subject execute() {
			User user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(userPassword.user);
			if (!user.password.validatePassword(userPassword.password)) {
				return null;
			}
			Subject subject = Backend.getInstance().getAuthentication().createSubject(userPassword.user);
			List<String> roleNames = user.roles.stream().map((role) -> role.name).collect(Collectors.toList());
			subject.getRoles().addAll(roleNames);
			return subject;
		}
	}
	
	protected abstract User retrieveUser(String userName);

}