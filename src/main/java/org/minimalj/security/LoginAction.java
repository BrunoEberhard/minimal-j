package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;

public class LoginAction extends Editor<UserPassword, Subject> {

	private final Subject anonymousSubject;
	private final LoginListener listener;
	
	public LoginAction(LoginListener listener) {
		this(listener, null);
	}
	
	public LoginAction(LoginListener listener, Subject anonymousSubject) {
		this.anonymousSubject = anonymousSubject;
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
		if (anonymousSubject != null && anonymousSubject.isValid()) {
			Action action = new Action() {
				@Override
				public void action() {
					listener.loginSucceded(anonymousSubject);
				}
			};
			return Collections.singletonList(action);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	protected Subject save(UserPassword userPassword) {
		LoginTransaction loginTransaction = new LoginTransaction(userPassword);
		return Backend.getInstance().execute(loginTransaction);
	}

	@Override
	public void cancel() {
		listener.loginCancelled();
//		if (!Frontend.getInstance().getSubject().isValid()) {
//			// some frontends cannot close their PageManager. They have to show an error page.
//			Frontend.show(new AuthenticationFailedPage());
//		}
		super.cancel();
	}
	
	@Override
	protected void finished(Subject subject) {
		listener.loginSucceded(subject);
//		Frontend.getInstance().setSubject(subject);
	}
	
	public static interface LoginListener {
		
		public void loginSucceded(Subject subject);

		public void loginCancelled();

	}
}
