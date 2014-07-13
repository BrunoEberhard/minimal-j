package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;

/**
 * If no field class is found for a property then a TypeUnknownField
 * is created to show the developer what is missing. 
 *
 */
public class TypeUnknownField implements org.minimalj.frontend.edit.fields.FormField<Object> {

	private final PropertyInterface property;
	private final TextField textField;
	
	public TypeUnknownField(PropertyInterface property) {
		this.property = property;
		
		textField = ClientToolkit.getToolkit().createReadOnlyTextField();
		textField.setText("No Field for found:" + property.getFieldName());
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setObject(Object object) {
		// unused
	}

}
