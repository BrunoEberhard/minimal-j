package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;

public class LoginAction extends Editor<UserPassword, Subject> {

	private final LoginListener listener;
	
	public LoginAction(LoginListener listener) {
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
		if (!Application.getApplication().isLoginRequired()) {
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
			LoginAction.super.cancel(); // close dialog but don't call loginCancelled
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

	public static interface LoginListener {
		
		public void loginSucceded(Subject subject);

		public void loginCancelled();

	}
}
