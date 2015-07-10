package org.minimalj.example.helloworld2;

import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class UserNameEditor extends NewObjectEditor<User> {

	public UserNameEditor() {
		super("Greet");
	}

	@Override
	// this method could left out
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
		ClientToolkit.getToolkit().show(new GreetingPage(user));
	}

}
