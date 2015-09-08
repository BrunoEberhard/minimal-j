package org.minimalj.example.petclinic.model;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.MockName;
import org.minimalj.util.mock.MockPrename;
import org.minimalj.util.mock.Mocking;

public class Person implements Rendering, Mocking {

	@NotEmpty @Size(30) @Searched
    public String firstName, lastName;

	public String getName() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "name", String.class);
		
		return firstName + " " + lastName;
	}
	
	@Override
	public String render(RenderType renderType) {
		return firstName + " " + lastName;
	}
	
	@Override
	public void mock() {
		firstName = MockPrename.getFirstName(Math.random() < .5);
		lastName = MockName.officialName();
	}
	
}
