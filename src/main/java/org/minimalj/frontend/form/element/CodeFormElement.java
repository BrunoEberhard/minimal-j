package org.minimalj.frontend.form.element;

import org.minimalj.model.Code;
import org.minimalj.model.properties.Property;
import org.minimalj.util.Codes;

public class CodeFormElement extends ComboBoxFormElement<Code> {

	public CodeFormElement(Property property) {
		super(property, Codes.get((Class<Code>) property.getClazz()));
	}
	
}
