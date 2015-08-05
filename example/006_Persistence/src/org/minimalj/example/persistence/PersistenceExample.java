package org.minimalj.example.persistence;

import java.util.logging.Level;

import org.minimalj.backend.db.AbstractTable;
import org.minimalj.backend.db.DbPersistence;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class PersistenceExample {

	public static void main(String[] args) {
		AbstractTable.sqlLogger.setLevel(Level.FINEST);
		AbstractTable.sqlLogger.getParent().getHandlers()[0].setLevel(Level.FINEST);
		
		DbPersistence persistence = new DbPersistence(DbPersistence.embeddedDataSource(), ExamplePerson.class);

		ExamplePerson person = new ExamplePerson();
		person.firstName = "Peter";
		person.lastName = "Muster";
		
		persistence.insert(person);
	}
	
	public static class ExamplePerson {
		public static final ExamplePerson $ = Keys.of(ExamplePerson.class);
		public Object id;
		
		@Size(255)
		public String firstName, lastName;
	}
}
