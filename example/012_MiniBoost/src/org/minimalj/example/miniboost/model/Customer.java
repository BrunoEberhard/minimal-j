package org.minimalj.example.miniboost.model;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.Mocking;

public class Customer implements Mocking, Rendering {

	public static final Customer $ = Keys.of(Customer.class);

	public Object id;
	
	@Size(25) @NotEmpty @Searched
	public String matchcode;

	@Size(100) @NotEmpty @Searched
	public String name1;

	@Size(100) @Searched
	public String name2, name3;

	public final Address address = new Address();

	public final Contact contact = new Contact();

	@Size(20) @Searched
	public String debtNo;

	@Override
	public String render() {
		return name1;
	}
	
    @Override
    public void mock() {
		DataFactory df = new DataFactory();
		name1 = df.getName();
		matchcode = name1;
		address.city = df.getCity();
		address.street = df.getStreetName();
		contact.email = df.getEmailAddress();
    }

}