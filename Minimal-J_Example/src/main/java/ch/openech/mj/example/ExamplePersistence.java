package ch.openech.mj.example;

import static ch.openech.mj.example.model.Book.*;
import ch.openech.mj.db.ColumnIndex;
import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.ImmutableTable;
import ch.openech.mj.db.MultiIndex;
import ch.openech.mj.db.Table;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.BookIdentification;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.CustomerIdentification;
import ch.openech.mj.example.model.Lend;

public class ExamplePersistence extends DbPersistence {

	public final ImmutableTable<BookIdentification> bookIdentification;
	public final Table<Book> book;
	public final MultiIndex<Book> bookIndex;
	
	public final ImmutableTable<CustomerIdentification> customerIdentification;
	public final Table<Customer> customer;
	public final MultiIndex<Customer> customerIndex;
	
	public final Table<Lend> lend;
	public final ColumnIndex<Lend> lendByCustomerIndex;
	public final ColumnIndex<Lend> lendByBookIndex;
	
	public ExamplePersistence() {
		super(DbPersistence.embeddedDataSource());
		
		bookIdentification = addImmutableClass(BookIdentification.class);
		customerIdentification = addImmutableClass(CustomerIdentification.class);
		
		book = addClass(Book.class);
		bookIndex = book.createFulltextIndex(BOOK.bookIdentification.title, BOOK.bookIdentification.author);
		
		customer = addClass(Customer.class);
		customerIndex = customer.createFulltextIndex(Customer.CUSTOMER.customerIdentification.firstName, Customer.CUSTOMER.customerIdentification.name);
		
		lend = addClass(Lend.class);
		lendByBookIndex = lend.createIndex(Lend.LEND.book);
		lendByCustomerIndex = lend.createIndex(Lend.LEND.customer);
	}
	
}
