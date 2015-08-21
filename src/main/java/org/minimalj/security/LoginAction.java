package org.minimalj.security;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.PasswordFormElement;

public class LoginAction extends Editor<Login, MjUser> {

	private PasswordFormElement passwordField;
	
	@Override
	protected Login createObject() {
		return new Login();
	}

	@Override
	protected Form<Login> createForm() {
		Form<Login> form = new Form<>();
		form.line(Login.$.user);
		passwordField = new PasswordFormElement(Login.$.password); 
		form.line(passwordField);
		return form;
	}

	@Override
	protected MjUser save(Login login) {
		LoginTransaction loginTransaction = new LoginTransaction(login);
		return Backend.getInstance().execute(loginTransaction);
	}
	
	@Override
	protected void finished(MjUser user) {
		Frontend.getBrowser().setUser(user);
	}
}
