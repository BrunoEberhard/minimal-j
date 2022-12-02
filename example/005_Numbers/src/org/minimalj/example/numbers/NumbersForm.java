package org.minimalj.example.numbers;

import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;

public class NumbersForm extends Form<Numbers> {

	public NumbersForm() {
		super(2);
		for (Property property : Properties.getProperties(Numbers.class).values()) {
			Object key = property.getValue(Numbers.$);
			line(key);
		}
	}

}