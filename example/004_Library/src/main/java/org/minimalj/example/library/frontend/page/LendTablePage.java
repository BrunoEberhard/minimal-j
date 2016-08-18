package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Lend.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.persistence.criteria.By;


public class LendTablePage extends TablePage<Lend> {

	private final Customer customer;
	
	public static final Object[] FIELDS = {
		$.book.title, //
		$.book.author, //
		$.till
	};
	
	public LendTablePage(Customer customer) {
		super(FIELDS);
		this.customer = customer;
	}

	@Override
	protected List<Lend> load() {
		return Backend.read(Lend.class, By.field(Lend.$.customer, customer), 100);
	}

	@Override
	public void action(Lend selectedObject) {
		Frontend.show(new BookPage(selectedObject.book));
	}
	
}
