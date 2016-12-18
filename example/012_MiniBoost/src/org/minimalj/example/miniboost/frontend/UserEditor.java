package org.minimalj.example.miniboost.frontend;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.frontend.element.CountryFormElement;
import org.minimalj.example.miniboost.model.User;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class UserEditor extends SimpleEditor<User> {

	@Override
	protected User createObject() {
		return new User();
	}

	@Override
	protected Form<User> createForm() {
		Form<User> form = new Form<>(2);
		form.line(User.$.loginname, User.$.lastname);
		form.line(User.$.firstname, new CountryFormElement(User.$.country));
		form.line(User.$.email, User.$.newsletter);
		return form;
	}

	@Override
	protected User save(User user) {
		return Backend.save(user);
	}
	
	protected List<Action> createAdditionalActions() {
		List<Action> actions = new ArrayList<>();
		actions.add(new PasswordEditor());
		return actions;
	};
	
	public class PasswordEditor extends NewObjectEditor<PlainPassword> {

		@Override
		protected Form<PlainPassword> createForm() {
			Form<PlainPassword> form = new Form<>();
			form.line(PlainPassword.$.plainPassword);
			return form;
		}
		
		@Override
		protected PlainPassword save(PlainPassword changedObject) {
			return changedObject;
		}
	}
	
	public static class PlainPassword {
		
		public static final PlainPassword $ = Keys.of(PlainPassword.class);
		
		@Size(255)
		public String plainPassword;
	}
}
