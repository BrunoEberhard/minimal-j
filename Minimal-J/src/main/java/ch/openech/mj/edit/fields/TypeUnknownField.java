package ch.openech.mj.edit.fields;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;

public class TypeUnknownField implements ch.openech.mj.edit.fields.FormField<Object> {

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
