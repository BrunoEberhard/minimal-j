package org.minimalj.example.minimalclinic.model;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.Mocking;

public class Person implements Rendering, Mocking {

	@NotEmpty @Size(30) @Searched
    public String firstName, lastName;

	public String getName() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "name");
		
		return firstName + " " + lastName;
	}
	
	@Override
	public String render() {
		return firstName + " " + lastName;
	}
	
	@Override
	public void mock() {		
		DataFactory df = new DataFactory();
		firstName = df.getFirstName();
		lastName = df.getLastName();
	}
	
}
