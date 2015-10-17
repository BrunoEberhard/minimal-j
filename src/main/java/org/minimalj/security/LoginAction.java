package org.minimalj.security;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;

public class LoginAction extends Editor<UserPassword, Subject> {

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
	protected Subject save(UserPassword userPassword) {
		LoginTransaction loginTransaction = new LoginTransaction(userPassword);
		return Backend.getInstance().execute(loginTransaction);
	}
	
	@Override
	protected void finished(Subject subject) {
		Frontend.getInstance().setSubject(subject);
	}
}
