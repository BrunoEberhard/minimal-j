package org.minimalj.example.helloworld2;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;

public class UserNameEditor extends SimpleEditor<User> {

	public UserNameEditor() {
		super("Greet");
	}

	@Override
	protected User createObject() {
		return new User();
	}
	
	@Override
	protected Form<User> createForm() {
		Form<User> form = new Form<>();
		form.line(User.$.name);
		return form;
	}

	@Override
	protected User save(User user) {
		return user;
	}
	
	@Override
	protected void finished(User user) {
		Frontend.getBrowser().show(new GreetingPage(user));
	}

}
