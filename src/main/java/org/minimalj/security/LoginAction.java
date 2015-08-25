package org.minimalj.security;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;

public class LoginAction extends Editor<UserPassword, Subject> {

	private PasswordFormElement passwordField;
	
	@Override
	protected UserPassword createObject() {
		return new UserPassword();
	}

	@Override
	protected Form<UserPassword> createForm() {
		Form<UserPassword> form = new Form<>();
		form.line(UserPassword.$.user);
		passwordField = new PasswordFormElement(UserPassword.$.password); 
		form.line(passwordField);
		return form;
	}

	@Override
	protected Subject save(UserPassword login) {
		LoginTransaction loginTransaction = new LoginTransaction(login);
		return Backend.getInstance().execute(loginTransaction);
	}
	
	@Override
	protected void finished(Subject user) {
		Frontend.getBrowser().setSubject(user);
	}
}
