package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;

public class LoginAction extends Editor<UserPassword, Subject> {

	private final Subject anonymousSubject;

	public LoginAction() {
		this(null);
	}
	
	public LoginAction(Subject anonymousSubject) {
		this.anonymousSubject = anonymousSubject;
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
					Frontend.getInstance().setSubject(anonymousSubject);
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
		if (!Frontend.getInstance().getSubject().isValid()) {
			// some frontends cannot close their PageManager. They have to show an error page.
			Frontend.show(new AuthenticationFailedPage());
		}
		super.cancel();
	}
	
	@Override
	protected void finished(Subject subject) {
		Frontend.getInstance().setSubject(subject);
	}
}
