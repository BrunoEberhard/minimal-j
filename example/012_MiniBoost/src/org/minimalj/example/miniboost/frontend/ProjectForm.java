package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Project.$;

import java.util.Locale;

import org.minimalj.example.miniboost.model.Country;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.example.miniboost.model.Employee;
import org.minimalj.example.miniboost.model.Project;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ReferenceFormElement;
import org.minimalj.util.LocaleContext;

public class ProjectForm extends Form<Project> {

	public ProjectForm(boolean editable) {
		super(editable, 2);
		
		text("Beschreibung");
		line($.matchcode, $.name1);
		line(new ReferenceFormElement<Customer>($.customer, new Object[]{ Customer.$.matchcode, Customer.$.name1, Customer.$.address.country, Customer.$.address.city }));
		line($.description);
		line($.address.street, $.address.zip);
		boolean german = LocaleContext.getCurrent().getLanguage().equals(new Locale("de").getLanguage());
		line($.address.city, new ReferenceFormElement<>($.address.country, new Object[]{ german ? Country.$.nameDe : Country.$.nameEn, Country.$.id}) );
		
		text("Projektdaten");
		line($.startDate, $.endDate);
		line($.closeDate);

		line(new ReferenceFormElement<>($.crewChief, Employee.$.matchcode, Employee.$.firstname, Employee.$.lastname));
		line($.amount, $.cost); // turnover and costs
	}

}
