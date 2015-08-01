package org.minimalj.example.numbers;

import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;

public class NumbersForm extends Form<Numbers> {

	public NumbersForm() {
		for (PropertyInterface property : Properties.getProperties(Numbers.class).values()) {
			Object key = property.getValue(Numbers.$);
			line(key);
		}
	}

}