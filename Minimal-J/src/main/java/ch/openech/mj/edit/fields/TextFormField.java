package ch.openech.mj.edit.fields;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;


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
