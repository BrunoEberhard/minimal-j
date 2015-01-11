package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Customer.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.StringUtils;


public class CustomerTablePage extends TablePage<Customer> {

	private final String text;
	
	public static final Object[] FIELDS = {
		$.company, //
		$.firstname, //
		$.surname, //
		$.email, //
		$.address, //
		$.zip, //
		$.city, //
		$.customersince, //
		$.title, //
		$.salutation, //
		$.customerNr, //
	};
	
	public CustomerTablePage() {
		this(null);
	}
	
	public CustomerTablePage(String text) {
		super(FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected List<Customer> load(String searchText) {
		if (!StringUtils.isBlank(searchText)) {
			return Backend.getInstance().read(Customer.class, Criteria.search(searchText), 100);
		} else {
			return Backend.getInstance().read(Customer.class, Criteria.all(), 100);
		}
	}

	@Override
	protected void clicked(Customer selectedObject, List<Customer> selectedObjects) {
		showDetail(CustomerPage.class, selectedObject);
	}

	@Override
	public String getTitle() {
		return text;
	}

}
