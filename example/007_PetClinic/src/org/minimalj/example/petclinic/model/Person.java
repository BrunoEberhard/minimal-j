package org.minimalj.example.petclinic.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class Person {

	@NotEmpty @Size(30) @Searched
    public String firstName, lastName;

	public String getName() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "name", String.class);
		
		return firstName + " " + lastName;
	}
	
}
