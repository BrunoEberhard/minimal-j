package ch.openech.mj.example;

import java.sql.SQLException;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.example.persistence.BookTable;
import ch.openech.mj.example.persistence.CustomerTable;

public class ExamplePersistence extends DbPersistence {

	private final BookTable bookTable;
	private final CustomerTable customerTable;
	private static ExamplePersistence instance;
	
	public ExamplePersistence() throws SQLException {
		bookTable = new BookTable(this);
		add(bookTable);
		
		customerTable = new CustomerTable(this);
		add(customerTable);
		
		addClass(Lend.class);
		
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

	public CustomerTable customer() {
		return customerTable;
	}
	
}
