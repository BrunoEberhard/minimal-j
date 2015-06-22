package org.minimalj.tutorial.view;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.tutorial.domain.Person;

public class AddPersonEditor extends NewObjectEditor<Person> {

	@Override
	protected Form<Person> createForm() {
		Form<Person> form = new Form<>(2);
		form.line(Person.PERSON.firstname, Person.PERSON.lastname);
		form.line(Person.PERSON.age);
		return form;
	}

	@Override
	protected Person save(Person person) {
		return Backend.getInstance().insert(person);
	}

}
