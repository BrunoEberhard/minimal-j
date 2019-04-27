package org.minimalj.frontend.form.element;

import org.minimalj.model.properties.PropertyInterface;

/**
 * If no FormElement class is found for a property then a TypeUnknownField
 * is created to show the developer what is missing. 
 *
 */
public class UnknownFormElement extends TextFormElement {

	public UnknownFormElement(PropertyInterface property) {
		super(property);
	}
	
	@Override
	public void setValue(Object object) {
		super.setValue(getProperty().getName() + ": no form element");
	}
}
