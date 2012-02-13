package ch.openech.mj.example;

import java.sql.SQLException;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.example.persistence.BookTable;

public class ExamplePersistence extends DbPersistence {

	private final BookTable bookTable;
	private static ExamplePersistence instance;
	
	public ExamplePersistence() throws SQLException {
		ExampleFormats.initialize();
		bookTable = new BookTable(this);
		add(bookTable);
		
		connect();
	}
	
	public static ExamplePersistence getInstance() {
		if (instance == null) {
			try {
				instance = new ExamplePersistence();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}
		return instance;
	}
	
	public BookTable book() {
		return bookTable;
	}
	
}
