package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;


public class TextFormField implements FormField<String> {

	private final PropertyInterface property;
	private final TextField textField;
	
	public TextFormField(PropertyInterface property) {
		this.property = property;
		this.textField = ClientToolkit.getToolkit().createReadOnlyTextField();
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
	public void setObject(String string) {
		textField.setText(string);
	}

	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled);
	}

}
