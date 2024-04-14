package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.model.Code;
import org.minimalj.model.properties.Property;
import org.minimalj.util.Codes;

@SuppressWarnings("unchecked")
public class CodeFormElement<T extends Code> extends ComboBoxFormElement<T> {

	public CodeFormElement(Property property) {
		super(property, Codes.get((Class<T>) property.getClazz()));
	}
	
	public CodeFormElement(T key, String nullText) {
		super(key, (List<T>) Codes.get(key.getClass()), nullText);
	}
}
