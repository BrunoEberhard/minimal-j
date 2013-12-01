package ch.openech.mj.example;

import static ch.openech.mj.example.model.Book.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.BookIdentification;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.CustomerIdentification;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.example.page.BookTablePage;
import ch.openech.mj.example.page.CustomerTablePage;
import ch.openech.mj.page.EditorPageAction;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.IAction;

public class MjExampleApplication extends MjApplication {

	private final DbPersistence dbPersistence;
	
	public MjExampleApplication() {
		dbPersistence = new DbPersistence(DbPersistence.embeddedDataSource());
		dbPersistence.addImmutableClass(BookIdentification.class);
		dbPersistence.addImmutableClass(CustomerIdentification.class);
		
		dbPersistence.addClass(Book.class);
		dbPersistence.getTable(Book.class).createFulltextIndex(BOOK.bookIdentification.title, BOOK.bookIdentification.author);
		
		dbPersistence.addClass(Customer.class);
		dbPersistence.getTable(Customer.class).createFulltextIndex(Customer.CUSTOMER.customerIdentification.firstName, Customer.CUSTOMER.customerIdentification.name);
		
		dbPersistence.addClass(Lend.class);
		dbPersistence.getTable(Lend.class).createIndex(Lend.LEND.book);
		dbPersistence.getTable(Lend.class).createIndex(Lend.LEND.customer);
	}
	
	public static DbPersistence persistence() {
		return ((MjExampleApplication) getApplication()).dbPersistence;
	}
	
	@Override
	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle("ch.openech.mj.example.Application");
	}

	@Override
	public List<IAction> getActionsNew(PageContext context) {
		List<IAction> items = new ArrayList<>();
		items.add(new EditorPageAction(new AddBookEditor()));
		items.add(new EditorPageAction(new AddCustomerEditor()));
		items.add(new EditorPageAction(new AddLendEditor()));
		return items;
	}

	@Override
	public String getWindowTitle(PageContext pageContext) {
		return "Minimal-J Example Application";
	}

	@Override
	public Class<?>[] getSearchClasses() {
		return new Class<?>[]{BookTablePage.class, CustomerTablePage.class};
	}

	@Override
	public Class<?> getPreferencesClass() {
		return null;
	}
	
}
