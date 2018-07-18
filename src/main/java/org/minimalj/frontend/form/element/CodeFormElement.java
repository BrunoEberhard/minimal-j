package org.minimalj.frontend.form.element;

import org.minimalj.model.Code;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Codes;

public class CodeFormElement extends ComboBoxFormElement<Code> {

	public CodeFormElement(PropertyInterface property) {
		super(property, Codes.get((Class<Code>) property.getClazz()));
	}
	
}
