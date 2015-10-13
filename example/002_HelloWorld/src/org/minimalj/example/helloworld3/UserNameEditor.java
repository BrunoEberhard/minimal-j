package org.minimalj.example.helloworld3;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ImageFormElement;

public class UserNameEditor extends SimpleEditor<User> {

	public UserNameEditor() {
		super("Greet");
	}

	@Override
	protected User createObject() {
		return GreetingApplication.user;
	}
	
	@Override
	protected Form<User> createForm() {
		Form<User> form = new Form<>();
		form.line(User.$.name);
		form.line(new ImageFormElement(User.$.image));
		return form;
	}

	@Override
	protected User save(User user) {
		GreetingApplication.user = user;
		return user;
	}
	
	@Override
	protected void finished(User user) {
		Frontend.getBrowser().show(new GreetingPage(user));
	}

}
