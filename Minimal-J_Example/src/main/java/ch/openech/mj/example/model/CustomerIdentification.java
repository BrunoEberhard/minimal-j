package ch.openech.mj.example.model;

import org.joda.time.LocalDate;

import ch.openech.mj.example.ExampleFormats;
import ch.openech.mj.model.annotation.Size;

public class CustomerIdentification {

	@Size(ExampleFormats.NAME)
	public String firstName, name;
	public LocalDate birthDay;
	
	public String display(int columns, int rows) {
		return firstName + " " + name;
	}
	
}
