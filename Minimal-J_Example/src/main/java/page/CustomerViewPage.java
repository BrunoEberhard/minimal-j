package page;

import java.sql.SQLException;

import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.CustomerForm;
import ch.openech.mj.example.ExamplePersistence;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.ObjectViewPage;
import ch.openech.mj.page.PageContext;

public class CustomerViewPage extends ObjectViewPage<Customer> {

	private final Customer customer;

	public CustomerViewPage(PageContext context, String customerId) {
		super(context);
		customer = lookup(customerId);
	}
	
	private static Customer lookup(String customerId) {
		try {
			return ExamplePersistence.getInstance().customer().read(Integer.valueOf(customerId));
		} catch (SQLException x) {
			throw new RuntimeException("Konnte Buch nicht laden", x);
		}
	}

	@Override
	protected Customer loadObject() {
		return customer;
	}

	@Override
	protected IForm<Customer> createForm() {
		return new CustomerForm(false);
	}
	
	@Override
	public void fillActionGroup(ActionGroup actionGroup) {
		// actionGroup.add(action)
	}
	
}
