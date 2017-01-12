package org.minimalj.example.persistence;

import java.util.logging.Level;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.persistence.sql.AbstractTable;
import org.minimalj.persistence.sql.SqlRepository;

public class PersistenceExample {

	public static void main(String[] args) {
		AbstractTable.sqlLogger.setLevel(Level.FINEST);
		AbstractTable.sqlLogger.getParent().getHandlers()[0].setLevel(Level.FINEST);
		
		SqlRepository repository = new SqlRepository(SqlRepository.embeddedDataSource(), ExamplePerson.class);

		ExamplePerson person = new ExamplePerson();
		person.firstName = "Peter";
		person.lastName = "Muster";
		
		repository.insert(person);
	}
	
	public static class ExamplePerson {
		public static final ExamplePerson $ = Keys.of(ExamplePerson.class);
		public Object id;
		
		@Size(255)
		public String firstName, lastName;
	}
}
