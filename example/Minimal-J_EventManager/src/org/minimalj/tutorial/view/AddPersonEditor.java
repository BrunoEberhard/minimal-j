package org.minimalj.tutorial.view;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.tutorial.domain.Person;

public class AddPersonEditor extends Editor<Person> {

	@Override
	protected Form<Person> createForm() {
		Form<Person> form = new Form<>(2);
		form.line(Person.PERSON.firstname, Person.PERSON.lastname);
		form.line(Person.PERSON.age);
		return form;
	}

	@Override
	protected Object save(Person person) throws Exception {
		return Backend.getInstance().insert(person);
	}

}
