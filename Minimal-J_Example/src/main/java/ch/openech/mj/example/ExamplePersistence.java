package ch.openech.mj.example;

import static ch.openech.mj.example.model.Book.*;

import java.sql.SQLException;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.MultiIndex;
import ch.openech.mj.db.Table;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.Lend;

public class ExamplePersistence extends DbPersistence {

	private final Table<Book> bookTable;
	private final MultiIndex<Book> bookIndex;
	private final Table<Customer> customerTable;
	private final MultiIndex<Customer> customerIndex;
	private static ExamplePersistence instance;
	
	public ExamplePersistence() throws SQLException {
		bookTable = addClass(Book.class);
		bookIndex = bookTable.createFulltextIndex(BOOK.title, BOOK.author);
		
		customerTable = addClass(Customer.class);
		customerIndex = customerTable.createFulltextIndex(Customer.CUSTOMER.firstName, Customer.CUSTOMER.name);
		
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
	
	public Table<Book> book() {
		return bookTable;
	}

	public MultiIndex<Book> bookIndex() {
		return bookIndex;
	}

	public Table<Customer> customer() {
		return customerTable;
	}

	public MultiIndex<Customer> customerIndex() {
		return customerIndex;
	}

}
